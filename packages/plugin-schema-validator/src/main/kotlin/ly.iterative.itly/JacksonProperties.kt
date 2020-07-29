package ly.iterative.itly

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

class JacksonProperties {
    companion object {
        @JvmStatic
        fun toJackson(properties: Properties?): JsonNode? {
            if (properties === null) {
                return null
            }

            val json = JsonNodeFactory.instance.objectNode()

            properties.properties.entries.forEach { eventPropertyEntry ->
                val key = eventPropertyEntry.key
                val value = eventPropertyEntry.value

                value?.let {
                    when (value) {
                        is Int -> {
                            json.put(key, value)
                        }
                        is Long -> {
                            json.put(key, value)
                        }
                        is Float -> {
                            json.put(key, value)
                        }
                        is Double -> {
                            json.put(key, value)
                        }
                        is Boolean -> {
                            json.put(key, value)
                        }
                        is String -> {
                            json.put(key, value)
                        }
                        is IntArray -> {
                            val array = json.putArray(key)
                            for (item in value) {
                                array.add(item)
                            }
                        }
                        is LongArray -> {
                            val array = json.putArray(key)
                            for (item in value) {
                                array.add(item)
                            }
                        }
                        is FloatArray -> {
                            val array = json.putArray(key)
                            for (item in value) {
                                array.add(item)
                            }
                        }
                        is DoubleArray -> {
                            val array = json.putArray(key)
                            for (item in value) {
                                array.add(item)
                            }
                        }
                        is BooleanArray -> {
                            val array = json.putArray(key)
                            for (item in value) {
                                array.add(item)
                            }
                        }
                        is Array<*> -> {
                            val array = json.putArray(key)
                            for (item in value) {
                                when (item) {
                                    is Int -> {
                                        array.add(item)
                                    }
                                    is Long -> {
                                        array.add(item)
                                    }
                                    is Float -> {
                                        array.add(item)
                                    }
                                    is Double -> {
                                        array.add(item)
                                    }
                                    is Boolean -> {
                                        array.add(item)
                                    }
                                    is String -> {
                                        array.add(item)
                                    }
                                }
                            }
                        }
                        else -> {
                            System.err.println("Error converting properties to JSONObject: invalid data type encountered ($value)")
                        }
                    }
                } ?: run {
                    json.putNull(key)
                }
            }

            return json
        }
    }
}
