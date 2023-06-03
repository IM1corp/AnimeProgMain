package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.anime

import com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField

class YummyStudio() {
    @SerializableField("url") public var url: String = "";
    @SerializableField("title") public var title: String = "";
    @SerializableField("id") public var id: Int = 0;
}