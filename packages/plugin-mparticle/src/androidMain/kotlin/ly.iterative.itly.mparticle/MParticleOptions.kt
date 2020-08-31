package ly.iterative.itly.mparticle

import android.content.Context

actual class MParticleOptions(
    val apiSecret: String,
    val androidContext: Context
) {
    companion object {
        @JvmStatic
        fun builder(): IApiSecret {
            return Builder()
        }
    }

    private constructor(builder: Builder) : this(builder.apiSecret, builder.androidContext)

    // Inner Builder class with required properties
    class Builder internal constructor() : IApiSecret, IAndroidContext, IBuild {
        internal lateinit var apiSecret: String
        internal lateinit var androidContext: Context
        override fun apiSecret(apiSecret: String): IAndroidContext {
            this.apiSecret = apiSecret
            return this
        }

        override fun androidContext(androidContext: Context): IBuild {
            this.androidContext = androidContext
            return this
        }

        override fun build(): MParticleOptions {
            return MParticleOptions(this)
        }
    }

    interface IApiSecret {
        fun apiSecret(apiSecret: String): IAndroidContext
    }

    interface IAndroidContext {
        fun androidContext(androidContext: Context): IBuild
    }

    interface IBuild {
        fun build(): MParticleOptions
    }
}
