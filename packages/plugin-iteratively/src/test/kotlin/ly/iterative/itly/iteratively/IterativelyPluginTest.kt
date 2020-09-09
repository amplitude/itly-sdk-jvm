package ly.iterative.itly.iteratively

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ly.iterative.itly.*
import ly.iterative.itly.core.Options
import okhttp3.mockwebserver.*
import org.junit.jupiter.api.*
import java.util.concurrent.TimeUnit
import ly.iterative.itly.test.*

val MS = TimeUnit.MILLISECONDS

object TIMEOUTS {
    const val BACKOFF_INITIAL_MS: Long = 100
    const val BACKOFF_MAXIMUM_MS: Long = 500
    const val FLUSH_INTERVAL_MS: Long = 500
}

val JSONObjectMapper: ObjectMapper = jacksonObjectMapper().configure(
    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false
)

class IterativelyPluginTest {
    // User info
    private var user = User()

    // Tracker Endpoint
    private var trackerUrl = TrackerUrl(user)
    private val requestContentType = "application/json"

    // Test objects
    private lateinit var iterativelyPlugin: IterativelyPlugin
    private lateinit var mockWebServer: MockWebServer

    val ITLY_OPTIONS_DEFAULT = Options(
        logger = Logger.STD_OUT_AND_ERR
    )

    val RETRY_OPTIONS_DEFAULT = RetryOptions(
        maxRetries = 10,
        delayInitialMillis = TIMEOUTS.BACKOFF_INITIAL_MS,
        delayMaximumMillis = TIMEOUTS.BACKOFF_MAXIMUM_MS
    )

    val ITERATIVELY_OPTIONS_DEFAULT = IterativelyOptions(
        url = trackerUrl.url,
        environment = Environment.DEVELOPMENT,
        batchSize = 1,
        flushQueueSize = 1,
        flushIntervalMs = TIMEOUTS.FLUSH_INTERVAL_MS,
        retryOptions = RETRY_OPTIONS_DEFAULT
    )

    object TestDispatchers {
        // Returns success for all 't/version/company-id' call, 404 otherwise
        val DEFAULT = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                when (request.path) {
                    "/t/version/company-id" -> return MockResponse().setResponseCode(200)
                }
                return MockResponse().setResponseCode(404)
            }
        }

        val ALWAYS_404 = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().setResponseCode(404)
            }
        }

        val ALWAYS_500 = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().setResponseCode(500)
            }
        }


        fun FAIL_UNTIL_REQUEST_N(successOnTry: Int): Dispatcher {
            return object : Dispatcher() {
                var tries = 0
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    tries += 1
                    return if (tries < successOnTry) MockResponse().setResponseCode(500)
                    else MockResponse().setResponseCode(200)
                }
            }
        }
    }

    @BeforeEach
    fun beforeEach() {
        setMockWebserver(TestDispatchers.DEFAULT)
        iterativelyPlugin = IterativelyPlugin(
            user.apiKey,
            ITERATIVELY_OPTIONS_DEFAULT
        )
        iterativelyPlugin.load(ITLY_OPTIONS_DEFAULT)
    }

    @AfterEach
    fun afterEach() {
        iterativelyPlugin.shutdown()
        mockWebServer.shutdown()
    }

    @Test
    fun tracker_identity_madeValidRequest() {
        iterativelyPlugin.postIdentify(user.id, null, listOf())
        assertValidTrackerRequest(TrackType.identify)
    }

    @Test
    fun tracker_group_madeValidRequest() {
        iterativelyPlugin.postGroup(user.id, user.groupId, null, listOf())
        assertValidTrackerRequest(TrackType.group)
    }

    @Test
    fun tracker_track_madeValidJson() {
        val eventName = "Dummy event"
        val props = mapOf(
            "prop" to "A property value",
            "anotherProp" to true
        )

        val event = Event(
            name = eventName,
            properties = props
        )
        iterativelyPlugin.postTrack(user.id, event, listOf())

        val request: RecordedRequest = mockWebServer.takeRequest()
        val body = request.body.readUtf8()
        val trackModelJson = body
                .replace("{\"objects\":[", "")
                .substringBeforeLast("]}")

        val trackModel: TrackModel = JSONObjectMapper.readValue(trackModelJson, TrackModel::class.java)

        // Path & Method
        Assertions.assertEquals(
            trackModel.eventName,
            "Dummy event"
        )
        Assertions.assertEquals(
            trackModel.properties!!["prop"],
            "A property value"
        )
        Assertions.assertEquals(
            trackModel.properties!!["anotherProp"],
            true
        )
    }

    @Test
    fun tracker_track_madeValidRequest() {
        val event = Event(
            name = "Dummy event",
            properties = mapOf(
                "prop" to "A property value",
                "anotherProp" to true
            )
        )
        iterativelyPlugin.postTrack(user.id, event, listOf())
        assertValidTrackerRequest(TrackType.track, event)
    }

    @Test
    fun tracker_retryUploadOnError_retryUntilSuccess() {
        val successOnTry = 3
        mockWebServer.dispatcher = TestDispatchers.FAIL_UNTIL_REQUEST_N(successOnTry)

        iterativelyPlugin.postIdentify(user.id, null, listOf())
        for (i in 1..successOnTry) {
            mockWebServer.takeRequest(TIMEOUTS.BACKOFF_MAXIMUM_MS, MS)
        }

        Assertions.assertEquals(
            successOnTry, mockWebServer.requestCount,
            "should auto-retry until upload succeeds"
        )
    }

    @Test
    fun tracker_retryUploadOnError_retryUntilMaxAttempts() {
        mockWebServer.dispatcher = TestDispatchers.ALWAYS_500

        iterativelyPlugin = IterativelyPlugin(
            user.apiKey,
            ITERATIVELY_OPTIONS_DEFAULT.copy(
                retryOptions = RETRY_OPTIONS_DEFAULT.copy(
                    maxRetries = 1
                )
            )
        )
        iterativelyPlugin.load(ITLY_OPTIONS_DEFAULT)

        iterativelyPlugin.postIdentify(user.id, null, listOf())
        for (i in 1..4) {
            mockWebServer.takeRequest(TIMEOUTS.BACKOFF_MAXIMUM_MS, MS)
        }

        Assertions.assertEquals(
            2, mockWebServer.requestCount,
            "should abort upload after RetryOptions.maxRetries attempts"
        )
    }

    @Test
    fun tracker_whenDisabled_doesntMakeRequests() {
        iterativelyPlugin = IterativelyPlugin(
            user.apiKey,
            ITERATIVELY_OPTIONS_DEFAULT.copy(
                disabled = true
            )
        )
        iterativelyPlugin.load(ITLY_OPTIONS_DEFAULT)

        val dummyProps = Properties()
        iterativelyPlugin.postIdentify(user.id, null, listOf())
        iterativelyPlugin.postGroup(user.id, user.groupId, dummyProps, listOf())
        iterativelyPlugin.postAlias(user.id, null)
        iterativelyPlugin.postTrack(user.id, Event("event"), listOf())
        for (i in 1..4) {
            mockWebServer.takeRequest(TIMEOUTS.BACKOFF_MAXIMUM_MS, MS)
        }

        Assertions.assertEquals(
            0, mockWebServer.requestCount,
            "should not make any requests when disabled"
        )
    }

    @Test
    fun tracker_shutdownWhenDisabled_doesntCrash() {
//        val itly: Itly = TestUtil.getItly(ITLY_OPTIONS_DEFAULT.copy(
//                plugins = arrayListOf(iterativelyPlugin)
//        ))

        iterativelyPlugin = IterativelyPlugin(
            user.apiKey,
            ITERATIVELY_OPTIONS_DEFAULT.copy(
                disabled = true
            )
        )
        iterativelyPlugin.load(ITLY_OPTIONS_DEFAULT)

        Assertions.assertDoesNotThrow {
            iterativelyPlugin.shutdown()
        }
    }

    private fun assertValidTrackerRequest(trackType: TrackType, event: Event? = null) {
        val request: RecordedRequest = mockWebServer.takeRequest()
        Asserts.assertValidTrackerRequest(
            request = request,
            trackType = trackType,
            event = event
        )
    }

    private fun setMockWebserver(dispatcher: Dispatcher) {
        mockWebServer = MockWebServer()
        mockWebServer.dispatcher = dispatcher
        mockWebServer.start(trackerUrl.port)
    }
}
