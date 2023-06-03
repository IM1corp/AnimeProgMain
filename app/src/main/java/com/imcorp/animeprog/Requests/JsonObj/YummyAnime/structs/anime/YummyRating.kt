package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.anime

import com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField

class YummyRating() {
    @SerializableField("counters")
    public var counters: Int = 0
    @SerializableField("average")
    public var average: Double = 0.0
}