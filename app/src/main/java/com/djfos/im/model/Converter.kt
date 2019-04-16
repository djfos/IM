package com.djfos.im.model

import androidx.room.TypeConverter
import com.djfos.im.filter.FilterType
import com.djfos.im.filter.IFilter
import com.djfos.im.filter.typeMap
import com.google.gson.Gson
import com.google.gson.JsonParser


class Converter {
    @TypeConverter
    fun historyToString(list: MutableList<IFilter>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun stringToHistoryTo(list: String): MutableList<IFilter> {
        val parser = JsonParser()
        val res = mutableListOf<IFilter>()
        for (element in parser.parse(list).asJsonArray) {
            val typeElement = element.asJsonObject.get("type")
                    ?: throw Exception("field type not found")
            val type = FilterType.valueOf(typeElement.asString)
            val cls = typeMap[type] ?: throw Exception("no such type: $type")
            val filter = Gson().fromJson(element,cls.java)
            if (filter is IFilter)
                res.add(filter)
        }
        return res
    }
}



