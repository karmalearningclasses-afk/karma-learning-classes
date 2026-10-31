package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.EducationLevel
import com.example.data.model.LanguagePreference
import com.example.data.model.UserRole
import org.json.JSONArray
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = runCatching { UserRole.valueOf(value) }.getOrDefault(UserRole.STUDENT)

    @TypeConverter
    fun fromEducationLevel(level: EducationLevel): String = level.name

    @TypeConverter
    fun toEducationLevel(value: String): EducationLevel = runCatching { EducationLevel.valueOf(value) }.getOrDefault(EducationLevel.COMPETITIVE_EXAM)

    @TypeConverter
    fun fromLanguagePreference(pref: LanguagePreference): String = pref.name

    @TypeConverter
    fun toLanguagePreference(value: String): LanguagePreference = runCatching { LanguagePreference.valueOf(value) }.getOrDefault(LanguagePreference.BILINGUAL)
}

object JsonUtils {
    fun stringListToJson(list: List<String>): String {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        return arr.toString()
    }

    fun jsonToStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
            list
        }.getOrDefault(emptyList())
    }

    fun intMapToJson(map: Map<String, Int>): String {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k, v) }
        return obj.toString()
    }

    fun jsonToIntMap(json: String?): Map<String, Int> {
        if (json.isNullOrBlank()) return emptyMap()
        return runCatching {
            val obj = JSONObject(json)
            val map = mutableMapOf<String, Int>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.getInt(key)
            }
            map
        }.getOrDefault(emptyMap())
    }
}
