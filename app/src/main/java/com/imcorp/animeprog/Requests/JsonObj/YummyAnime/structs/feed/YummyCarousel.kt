package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.feed

import com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.YummyIAnimeJson

class YummyCarousel(
        @SerializableField("season") public var season: Int = 0,
        @SerializableField("year") public var year: Int = 0,
        @SerializableField("items") public var items: ArrayList<YummyIAnimeJson> = arrayListOf(),
){

}
