package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.anime

import com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField

class YummyMinAge() {
    @SerializableField("value") public var valueInt: Int = 0
    @SerializableField("title") public var title: String? = null
}