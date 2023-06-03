package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.routes

import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.YummyRestApi
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.feed.YummyMainPage
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.profile.YummyUser

class YummyFeeds(restApi: YummyRestApi) : SimpleYummyApi(restApi) {
    public fun getMainPage(): YummyMainPage = restApi.method("/feed")
}