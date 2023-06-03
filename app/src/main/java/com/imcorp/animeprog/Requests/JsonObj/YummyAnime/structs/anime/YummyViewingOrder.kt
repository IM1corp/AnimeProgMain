package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.anime

import android.content.res.Resources
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField

class YummyViewingOrder(
        @SerializableField("title") public var title: String = "",
        @SerializableField("status") public var status: Int = 0,
        @SerializableField("anime_url") public var animeUrl: String = "",
        @SerializableField("year") public var year: Int = 0,
        @SerializableField("poster") public var poster: YummyPoster = YummyPoster(),
        @SerializableField("anime_id") public var animeId: Int = 0,
        @SerializableField("data") public var data: Data = Data(),
        @SerializableField("user") public var user: User = User(),
        @SerializableField("type_int") public var typeInt: Int = 0,
) {
    fun toOneAnime(resource: Resources): OneAnime = OneAnime(Config.HOST_YUMMY_ANIME).also{
        it.path = this.animeUrl
        it.year = this.year
        it.title = this.title
        it.cover = this.poster.fullSizePhoto
        it.description = getTypeString(resource) + ", "+ this.data.text
        it.animeId = this.animeId
    }
    fun getTypeString(resource: Resources): String = resource.getString(when(typeInt){
        1-> R.string.TV
        2-> R.string.PF
        3-> R.string.KF
        4-> R.string.OVA
        5-> R.string.SPECIAL
        6->R.string.MC
        7->R.string.ONA
        else->R.string.undefined
    })
    class Data(
            @SerializableField("text") public var text: String = "",
            @SerializableField("id") public var id: Int = 0,
            @SerializableField("index") public var index: Int = 0,
    )
    class User(@SerializableField("list") public var list: Int? = null)
}