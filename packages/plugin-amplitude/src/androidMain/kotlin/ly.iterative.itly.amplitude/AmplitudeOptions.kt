package ly.iterative.itly.amplitude

import android.content.Context

actual open class AmplitudeOptions(
    val androidContext: Context,
    var branch: String? = null,
    var source: String? = null,
    var version: String? = null
)
