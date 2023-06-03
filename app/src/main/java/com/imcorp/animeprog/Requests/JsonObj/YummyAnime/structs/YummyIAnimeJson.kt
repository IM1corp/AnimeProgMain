package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs

import android.content.res.Resources
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.anime.YummyPoster
import java.io.Serializable

open class YummyIAnimeJson(
        @SerializableField("anime_id") public var animeId: Int = 0,
        @SerializableField("anime_url") public var animeUri: String = "",
        @SerializableField("poster") public var poster: YummyPoster = YummyPoster(),
        @SerializableField("title") public var title: String = "",
        @SerializableField("description") public var description: String = ""
) {

    open fun toOneAnime(resources: Resources) = OneAnime(Config.HOST_YUMMY_ANIME).also{ anime->
        anime.animeId = this.animeId
        anime.path = "/catalog/item/"+this.animeUri
        anime.cover = this.poster.fullSizePhoto
        anime.title = this.title
        anime.description = this.description
    }

//    @SerializableField("anime_id1") public var animeId1: Int = 0
}