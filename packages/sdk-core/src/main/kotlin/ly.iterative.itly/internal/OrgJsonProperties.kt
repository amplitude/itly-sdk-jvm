package ly.iterative.itly.internal

import ly.iterative.itly.Properties
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class OrgJsonProperties {
    companion object {
        @JvmStatic
        fun toOrgJson(properties: Properties?): JSONObject? {
            if (properties === null) {
                return null
            }

            val json = JSONObject()

            properties.properties.entries.forEach { eventPropertyEntry ->
                val key = eventPropertyEntry.key
                val value = eventPropertyEntry.value

                try {
                    value?.let {
                        json.put(key, if (value.javaClass.isArray) JSONArray(value) else value)
                    } ?: run {
                        json.put(key, JSONObject.NULL)
                    }
                } catch (e: JSONException) {
                    System.err.println("Error converting properties to JSONObject: ${e.message}")
                }
            }

            return json
        }
    }
}
