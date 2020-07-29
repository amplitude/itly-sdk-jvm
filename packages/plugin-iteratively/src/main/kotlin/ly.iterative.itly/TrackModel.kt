package ly.iterative.itly

import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

enum class TrackType {
    group,
    identify,
    //    page,
    track,
    POISON;

    companion object {
        @JvmStatic
        fun fromEvent(event: Event): TrackType {
            var eventType = track
            try {
                eventType = valueOf(event.name.toLowerCase())
            } catch (error: Error) {
            }
            return eventType
        }
    }
}

data class TrackModel(
    val type: TrackType,
    val dateSent: String = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
    val eventId: String?,
    val eventSchemaVersion: String?,
    val eventName: String?,
    // FIXME: properties aren't optional in JS/TS
    val properties: Properties?,
    val valid: Boolean,
    val validation: Validation
) {
    companion object {
        @JvmStatic
        fun newPoisonPill(): TrackModel {
            return TrackModel(
                type = TrackType.POISON,
                eventName = "POISON",
                eventId = "POISON",
                eventSchemaVersion = "POISON",
                properties = null,
                valid = false,
                validation = Validation("POISON")
            )
        }
    }
}

data class Validation (val details: String)
