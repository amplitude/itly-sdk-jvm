package ly.iterative.itly.android

import ly.iterative.itly.Event

/**
 * Minimal Itly interface for Audit SDK integration
 */
interface IItly {
    fun track(event: Event)
    fun reset()
    fun flush()
    fun shutdown()
}
