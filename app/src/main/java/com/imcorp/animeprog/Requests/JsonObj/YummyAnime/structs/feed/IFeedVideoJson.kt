package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.feed

import android.content.res.Resources
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.YummyIAnimeJson

class IFeedVideoJson(
        @SerializableField("date") public var date: Long = 0,
        @SerializableField("ep_title") public var epTitle: String = "",
        @SerializableField("player_title") public var playerTitle: String = "",
        @SerializableField("dub_title") public var dubbingTitle: String = "",
): YummyIAnimeJson() {
    override fun toOneAnime(resources: Resources) =super.toOneAnime(resources).also{anime->
        anime.dataPosterText.mainTitle = anime.title
        anime.dataPosterText.subTitle = "$epTitle ${resources.getString(R.string.episodes)} — $dubbingTitle $playerTitle"
    }
}
