package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.anime

import com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField

class YummyAnimeUserData() {
    @SerializableField("rating") public val rating: Double = 0.0
    @SerializableField("list") public val list: List = List()
    class List{
        @SerializableField("is_vav") public val isFav: Boolean = false
        @SerializableField("value") public val userList: Int = -1
    }
}