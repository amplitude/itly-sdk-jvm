package ly.iterative.example

import ly.iterative.itly.*
import ly.iterative.itly.iteratively.*
import ly.iterative.itly.test.*
import okhttp3.mockwebserver.*
import org.junit.jupiter.api.*
import java.io.*

class AppKotlinTests {
    private val user = User()
    private val trackerUrl = TrackerUrl(user)
    private val outContent = ByteArrayOutputStream()
    private val originalOut = System.out
    private lateinit var mockWebServer: MockWebServer

    @BeforeEach
    fun setUpStreams() {
        System.setOut(PrintStream(outContent))

        mockWebServer = MockWebServer()
        mockWebServer.dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                when (request.path) {
                    trackerUrl.path -> return MockResponse().setResponseCode(200)
                }
                return MockResponse().setResponseCode(404)
            }
        }
        mockWebServer.start(trackerUrl.port)
    }

    @AfterEach
    fun restoreStreams() {
        mockWebServer.shutdown()
        System.setOut(originalOut)
    }

    // FIXME: Re-add Test after 2.0.0 is published
    @Test
    fun appKotlin_runApplication_makesValidateTrackerRequest() {
        AppKotlin.main(arrayOf("test-param"))

        val output = outContent.toString()
        originalOut.println(output)

        // Assert identify() call was made
        assertValidTrackerRequest(TrackType.identify)

        // Assert group() call was made
        assertValidTrackerRequest(TrackType.group)

        // Assert track() call was made
        assertValidTrackerRequest(TrackType.track, Event(
            name = "Event No Properties"
        ))
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
