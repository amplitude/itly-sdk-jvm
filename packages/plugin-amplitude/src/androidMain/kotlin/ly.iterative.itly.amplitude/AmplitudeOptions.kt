package ly.iterative.itly.amplitude

import android.content.Context

actual open class AmplitudeOptions @JvmOverloads constructor(
    val androidContext: Context,
    /**
     * Tracking plan branch name (e.g. feature/demo).
     */
    var planBranch: String? = null,
    /**
     * Tracking plan source.
     */
    var planSource: String? = null,
    /**
     * Tracking plan version number (e.g. 1.0.0).
     */
    var planVersion: String? = null
) {
    /**
     * Returns new Options with the given overrides
     */
    @JvmOverloads
    fun withOverrides(
        planBranch: String? = null,
        planSource: String? = null,
        planVersion: String? = null
    ): AmplitudeOptions {
        return AmplitudeOptions(
            androidContext = this.androidContext,
            planBranch = planBranch ?: this.planBranch,
            planSource = planSource ?: this.planSource,
            planVersion = planVersion ?: this.planVersion
        )
    }
}
