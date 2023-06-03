package com.imcorp.animeprog.Requests.JsonObj.YummyAnime

import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.Requests.JsonObj.OneAnime.OneAnimeWithId
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.jvm.Throws

object YSearchLoader {
    @JvmStatic
    @Throws(JSONException::class)
    fun fromJson(response: JSONObject?): ArrayList<OneAnimeWithId> {

        fun b(obj:Any): Boolean = when(obj){
            is Int -> obj!=0
            is String -> obj.isNotEmpty()
            is Boolean -> obj
            else -> false
        }
        return response?.run {
            val animes = getJSONObject("animes").getJSONArray("data")
            val list = ArrayList<OneAnimeWithId>(animes.length())
            for (i in 0 until animes.length()) {
                val item = animes.getJSONObject(i)
                val s = OneAnimeWithId(
                        if (item.has("id")) item.getInt("id") else i,
                        OneAnime(Config.HOST_YUMMY_ANIME).apply {
                            title = item.getString("name")
                            path = "catalog/item/${item.getString("alias")}"
                            year = item.getInt("year")
                            cover = item.getString("image").also { setBigCover(it) }
                            if (b(item["is_rating"]) && item.has("rating"))
                                this.attrs.rating = item.getString("rating")
                            if (item.has("type_title"))
                                this.attrs.serialType = item.getString("type_title")
                        }
                )
                list.add(s)
            }
            list
        } ?: throw IOException("Invalid response - try using a proxy server")
    }
}