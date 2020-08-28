package ly.iterative.example

import ly.iterative.itly.*
import ly.iterative.itly.iteratively.*
import ly.iterative.itly.jvm.*
import ly.iterative.itly.test.*
import ly.iterative.itly.test.events.*

object AppKotlin {
    @JvmStatic
    fun main(args: Array<String>) {
        val logger = Logger.STD_OUT_AND_ERR
        val user = User()
        val trackerUrl = TrackerUrl(user)

        val schemaValidatorPlugin = SchemaValidatorPlugin(Schemas.DEFAULT)

        val iterativelyPlugin = IterativelyPlugin(
            user.apiKey,
            ly.iterative.itly.iteratively.IterativelyOptions(
                url = trackerUrl.url,
                batchSize = 1,
                flushQueueSize = 1
            )
        )

        val itly = Itly()

        itly.load(Options(
            plugins = arrayListOf<Plugin>(
                    schemaValidatorPlugin,
                    iterativelyPlugin
            ),
            logger = logger,
            validation = ValidationOptions(
                trackInvalid = true,
                errorOnInvalid = false
            )
        ))


        itly.identify(user.id, Identify(
            requiredNumber = 42.0
        ))

        itly.group(user.id, user.groupId, Group(
            requiredBoolean = true
        ))

        itly.track(user.id, EventNoProperties())

        // FIXME: This is a hack to let the requests finish before the process ends
        Thread.sleep(2000)

        itly.shutdown()

        val output = iterativelyPlugin.id()
        println(output)
    }
}
