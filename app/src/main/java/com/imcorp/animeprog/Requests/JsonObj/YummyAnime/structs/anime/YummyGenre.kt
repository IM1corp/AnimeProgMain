package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.anime

import com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField

class YummyGenre(
        @SerializableField("title") public var title: String = "",
        @SerializableField("url") public var url: String = "",
        @SerializableField("id") public var id: Number = 0,

)