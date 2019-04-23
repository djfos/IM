package com.djfos.im.model

import androidx.room.TypeConverter
import com.djfos.im.filter.AbstractFilter
import com.djfos.im.filter.FilterType
import com.djfos.im.filter.filterInfos
import com.google.gson.Gson
import com.google.gson.JsonParser


class Converter {
    @TypeConverter
    fun historyToString(list: MutableList<AbstractFilter>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun stringToHistoryTo(list: String): MutableList<AbstractFilter> {
        val parser = JsonParser()
        val res = mutableListOf<AbstractFilter>()
        for (element in parser.parse(list).asJsonArray) {
            val typeElement = element.asJsonObject.get("type")
                    ?: throw Exception("field type not found")
            val type = FilterType.valueOf(typeElement.asString)
            val info = filterInfos[type] ?: throw Exception("no such type: $type")
            val filter = Gson().fromJson(element, info.cls.java)
            if (filter is AbstractFilter)
                res.add(filter)
        }
        return res
    }
}



