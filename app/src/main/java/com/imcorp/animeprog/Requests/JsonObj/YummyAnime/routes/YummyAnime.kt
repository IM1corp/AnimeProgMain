package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.routes

import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.YummyRestApi
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.anime.YummyOneAnime

class YummyAnime(restApi: YummyRestApi) : SimpleYummyApi(restApi) {

    public fun getById(id: Int, needVideos: Boolean = true): YummyOneAnime =
            restApi.method("/anime/$id", data=buildGetArgs(needVideos))
    public fun getByUrl(url: String, needVideos: Boolean = true): YummyOneAnime =
            restApi.method("/anime/${url.split('/', '\\').last()}", data=buildGetArgs(needVideos))
    companion object{
        private fun buildGetArgs(needVideos: Boolean) = HashMap<String, String>(1).apply{
            if(needVideos) this["need_videos"] = "1"
        }
    }
}