package ly.iterative.itly

import okhttp3.mockwebserver.*
import org.junit.jupiter.api.*
import java.util.concurrent.TimeUnit

class IterativelyPluginTest {
    // User info
    private var user = User()

    // Tracker Endpoint
    private var trackerUrl = TrackerUrl(user)
    private val requestContentType = "application/json"

    // Test objects
    private lateinit var iterativelyPlugin: IterativelyPlugin
    private lateinit var mockWebServer: MockWebServer

    @BeforeEach
    fun beforeEach() {
        mockWebServer = MockWebServer()
        mockWebServer.dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                when (request.path) {
                    "/t/version/company-id" -> return MockResponse().setResponseCode(200)
                }
                return MockResponse().setResponseCode(404)
            }
        }
        mockWebServer.start(trackerUrl.port)

        iterativelyPlugin = IterativelyPlugin(
            user.apiKey,
            IterativelyOptions(
                url = trackerUrl.url,
                environment = Environment.DEVELOPMENT,
                batchSize = 1,
                flushQueueSize = 1,
                flushIntervalMs = 100,
                retryOptions = RetryOptions(
                    maxRetries = 10,
                    delayInitialSeconds = 1,
                    delayMaximumSeconds = 2
                )
            )
        )
        iterativelyPlugin.load(OptionsCore(
            logger = Logger.STD_OUT_AND_ERR
        ))
    }

    @AfterEach
    fun afterEach() {
        iterativelyPlugin.shutdown()
        mockWebServer.shutdown()
    }

    @Test
    fun tracker_identity_madeValidRequest() {
        iterativelyPlugin.identify(user.id)
        assertValidTrackerRequest(TrackType.identify)
    }

    @Test
    fun tracker_group_madeValidRequest() {
        iterativelyPlugin.group(user.id, user.groupId)
        assertValidTrackerRequest(TrackType.group)
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
        iterativelyPlugin.track(user.id, event)
        assertValidTrackerRequest(TrackType.track, event)
    }

    @Test
    fun tracker_retryUploadOnError_retryUntilSuccess() {
        val successOnTry = 3

        mockWebServer.dispatcher = object : Dispatcher() {
            var tries = 0
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                tries += 1
                return if (tries < successOnTry) MockResponse().setResponseCode(404)
                    else MockResponse().setResponseCode(200)
            }
        }
        iterativelyPlugin.identify(user.id)

        val timeout: Long = 5000
        val MS = TimeUnit.MILLISECONDS
        for (i in 1..successOnTry) {
            mockWebServer.takeRequest(timeout, MS)
        }

        Assertions.assertEquals(
            successOnTry, mockWebServer.requestCount,
            "should auto-retry until upload succeeds"
        )
    }

    private fun assertValidTrackerRequest(trackType: TrackType, event: Event? = null) {
        val request: RecordedRequest = mockWebServer.takeRequest()
        Asserts.assertValidTrackerRequest(
            request = request,
            trackType = trackType,
            event = event
        )
    }
}
