package ly.iterative.itly.mixpanel

import ly.iterative.itly.*

expect class MixpanelOptions

expect class MixpanelPlugin(
    token: String,
    options: MixpanelOptions
)
