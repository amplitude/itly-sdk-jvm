package ly.iterative.itly.iteratively

import ly.iterative.itly.Event
import ly.iterative.itly.Properties
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat

enum class TrackType {
    group,
    identify,
    //    page,
    track,
    POISON;

    companion object {
        @JvmStatic
        fun fromEvent(event: Event): TrackType {
            return when(event.name.toLowerCase()) {
                "identify" -> identify
                "group" -> group
                else -> track
            }
        }
    }
}

data class TrackModel(
    val type: TrackType,
    val dateSent: String = ISODateTimeFormat.dateTime().print(
        DateTime().withZone(DateTimeZone.UTC)
    ),
    val eventId: String?,
    val eventSchemaVersion: String?,
    val eventName: String?,
    // FIXME: properties aren't optional in JS/TS
    val properties: Map<String, Any?>?,
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
