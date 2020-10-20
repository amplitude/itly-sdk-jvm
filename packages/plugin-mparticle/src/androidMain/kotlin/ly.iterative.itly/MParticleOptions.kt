package ly.iterative.itly

import android.content.Context

actual class MParticleOptions(
    apiSecret: String,
    androidContext: Context
) : ly.iterative.itly.mparticle.MParticleOptions(
    apiSecret,
    androidContext
) {
    companion object {
        @JvmStatic
        fun builder(): IApiSecret<MParticleOptions> {
            return Builder()
        }
    }

    private constructor(builder: Builder): this(builder.apiSecret, builder.androidContext)

    // Inner Builder class with required properties
    class Builder internal constructor() : IApiSecret<MParticleOptions>, IAndroidContext<MParticleOptions>, IBuild<MParticleOptions> {
        internal lateinit var apiSecret: String
        internal lateinit var androidContext: Context
        override fun apiSecret(apiSecret: String): IAndroidContext<MParticleOptions> {
            this.apiSecret = apiSecret
            return this
        }

        override fun androidContext(androidContext: Context): IBuild<MParticleOptions> {
            this.androidContext = androidContext
            return this
        }

        override fun build(): MParticleOptions {
            return MParticleOptions(this)
        }
    }
}
