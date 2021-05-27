package ly.iterative.itly.iteratively

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ly.iterative.itly.*
import ly.iterative.itly.Options
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

    val PLUGIN_OPTIONS_DEFAULT = PluginLoadOptions(ITLY_OPTIONS_DEFAULT)

    val RETRY_OPTIONS_DEFAULT = RetryOptions(
        maxRetries = 10,
        delayInitialMillis = TIMEOUTS.BACKOFF_INITIAL_MS,
        delayMaximumMillis = TIMEOUTS.BACKOFF_MAXIMUM_MS
    )

    val ITERATIVELY_OPTIONS_DEFAULT = IterativelyOptions(
        url = trackerUrl.url,
        batchSize = 1,
        flushQueueSize = 1,
        flushIntervalMs = TIMEOUTS.FLUSH_INTERVAL_MS,
        retryOptions = RETRY_OPTIONS_DEFAULT
    )

    val TEST_EVENT = Event(
        name = "Test event",
        properties = mapOf(
            "prop" to "A property value",
            "anotherProp" to true
        )
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
        iterativelyPlugin.load(PLUGIN_OPTIONS_DEFAULT)
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
        iterativelyPlugin.postTrack(user.id, TEST_EVENT, listOf())

        val request: RecordedRequest = mockWebServer.takeRequest()
        val body = request.body.readUtf8()
        val trackModelJson = body
                .replace("{\"objects\":[", "")
                .substringBeforeLast("]}")

        val trackModel: TrackModel = JSONObjectMapper.readValue(trackModelJson, TrackModel::class.java)

        // Path & Method
        Assertions.assertEquals("Test event", trackModel.eventName)
        Assertions.assertEquals("A property value", trackModel.properties!!["prop"])
        Assertions.assertEquals(true, trackModel.properties!!["anotherProp"])
    }

    @Test
    fun postTrack_event_makesValidRequest() {
        iterativelyPlugin.postTrack(user.id, TEST_EVENT, listOf())
        assertValidTrackerRequest(TrackType.track, TEST_EVENT)
    }

    @Test
    fun postTrack_eventWithVersionAndBranch_makesValidRequest() {
        val options = ITERATIVELY_OPTIONS_DEFAULT.copy(IterativelyOptions(
            branch = "main",
            version = "1.0.0"
        ))

        iterativelyPlugin = IterativelyPlugin(user.apiKey, options)
        iterativelyPlugin.load(PLUGIN_OPTIONS_DEFAULT)
        iterativelyPlugin.postTrack(user.id, TEST_EVENT, listOf())

        assertValidTrackerRequest(TrackType.track, TEST_EVENT, options)
    }

    @Test
    fun trackerUrl_noUrlInOptions_usesDefaultDataplaneUrl() {
        iterativelyPlugin = IterativelyPlugin(user.apiKey)
        Assertions.assertEquals("https://data.us-east2.iterative.ly/t", iterativelyPlugin.config.url)
    }

    @Test
    fun trackerUrl_urlSetInOptions_usesSetUrl() {
        val testUrl = "https://localhost:1234/t"
        iterativelyPlugin = IterativelyPlugin(user.apiKey, IterativelyOptions(url = testUrl))
        Assertions.assertEquals(testUrl, iterativelyPlugin.config.url)
    }

    @Test
    fun enabled_inDevelopmentByDefault_true() {
        iterativelyPlugin = IterativelyPlugin(user.apiKey)
        iterativelyPlugin.load(PluginLoadOptions(Environment.DEVELOPMENT, Logger.NONE));

        Assertions.assertEquals(false, iterativelyPlugin.disabled())
    }

    @Test
    fun enabled_inProductionByDefault_false() {
        iterativelyPlugin = IterativelyPlugin(user.apiKey)
        iterativelyPlugin.load(PluginLoadOptions(Environment.PRODUCTION, Logger.NONE));

        Assertions.assertEquals(true, iterativelyPlugin.disabled())
    }

    @Test
    fun enabled_inProductionWithDisabledFalse_true() {
        iterativelyPlugin = IterativelyPlugin(user.apiKey, IterativelyOptions(disabled = false))
        iterativelyPlugin.load(PluginLoadOptions(Environment.PRODUCTION, Logger.NONE));

        Assertions.assertEquals(false, iterativelyPlugin.disabled())
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
            ITERATIVELY_OPTIONS_DEFAULT.copy(IterativelyOptions(
                retryOptions = RETRY_OPTIONS_DEFAULT.copy(
                    maxRetries = 1
                )
            ))
        )
        iterativelyPlugin.load(PLUGIN_OPTIONS_DEFAULT)

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
            ITERATIVELY_OPTIONS_DEFAULT.copy(IterativelyOptions(
                disabled = true
            ))
        )
        iterativelyPlugin.load(PLUGIN_OPTIONS_DEFAULT)

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
        iterativelyPlugin = IterativelyPlugin(
            user.apiKey,
            ITERATIVELY_OPTIONS_DEFAULT.copy(IterativelyOptions(
                disabled = true
            ))
        )
        iterativelyPlugin.load(PLUGIN_OPTIONS_DEFAULT)

        Assertions.assertDoesNotThrow {
            iterativelyPlugin.shutdown()
        }
    }

    private fun assertValidTrackerRequest(
        trackType: TrackType,
        event: Event? = null,
        options: IterativelyOptions? = null
    ) {
        val request: RecordedRequest = mockWebServer.takeRequest()
        Asserts.assertValidTrackerRequest(
            request = request,
            trackType = trackType,
            event = event,
            options = options
        )
    }

    private fun setMockWebserver(dispatcher: Dispatcher) {
        mockWebServer = MockWebServer()
        mockWebServer.dispatcher = dispatcher
        mockWebServer.start(trackerUrl.port)
    }
}
