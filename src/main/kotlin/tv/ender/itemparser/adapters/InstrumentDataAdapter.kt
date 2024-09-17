package tv.ender.itemparser.adapters

import com.google.gson.*
import tv.ender.itemparser.modal.data.InstrumentData
import java.lang.reflect.Type

class InstrumentDataAdapter : JsonSerializer<InstrumentData>, JsonDeserializer<InstrumentData> {

    override fun serialize(src: InstrumentData, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject().apply {
            addProperty("instrument", src.instrument)
        }
        return jsonObject
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): InstrumentData {
        val jsonObject = json.asJsonObject
        val instrument = jsonObject.get("instrument").asString
        return InstrumentData(instrument)
    }
}
