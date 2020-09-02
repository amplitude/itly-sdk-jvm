package ly.iterative.itly

class EventMetadata(
    val itly: ItlyEventMetadata = ItlyEventMetadata()
): HashMap<String, Map<String, Any>>()
