package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.anime

import com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo

class YummyVideo(
        @SerializableField("video_id") public var videoId: Int = 0,
        @SerializableField("number") public var epNumber: String = ".",
        @SerializableField("iframe_url") public var frameSrc: String = "",
        @SerializableField("data") public var `data`: VideoPlayerData=VideoPlayerData(),
        @SerializableField("date") public var date: Long = 0,
        @SerializableField("index") public var index: Int = 0,
) {
    fun toOneVideo(): OneVideo = OneVideo(this.epNumber).also{ video->
        video.urlFrame = this.frameSrc
        video.voiceStudio = this.data.dubbing
        video.downloaded = false
        video.loadOneVideoPlayerFromUrl()
        video.id = this.videoId
    }

    class VideoPlayerData(
            @SerializableField("player") public var player: String = "",
            @SerializableField("dubbing") public var dubbing: String = ""
    )

}