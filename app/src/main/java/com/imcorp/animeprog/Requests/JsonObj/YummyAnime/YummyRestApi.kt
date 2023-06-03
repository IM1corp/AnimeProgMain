package com.imcorp.animeprog.Requests.JsonObj.YummyAnime

import com.imcorp.animeprog.Config
import com.imcorp.animeprog.DB.DataBase
import com.imcorp.animeprog.DB.Settings
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.Requests.Http.InvalidStatusException
import com.imcorp.animeprog.Requests.Http.ReqResponse
import com.imcorp.animeprog.Requests.Http.Request
import com.imcorp.animeprog.Requests.JsonObj.Serialization.convertTo
import com.imcorp.animeprog.Requests.JsonObj.Serialization.toJSONObject
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.routes.YummyAnime
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.routes.YummyComments
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.routes.YummyFeeds
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.routes.YummyUsers
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.errors.YummyError
import org.json.JSONException
import org.json.JSONObject
import kotlin.reflect.KClass


public class YummyRestApi(val request: Request, val db: DataBase?=null) {
    val animes by lazy{ YummyAnime(this); }
    val comments by lazy{ YummyComments(this); }
    val feed by lazy{ YummyFeeds(this) }
    val users by lazy{ YummyUsers(this) }
    inline fun <reified T: Any> method(path: String, method: String="GET", data: Any?=JSONObject()): T{
        try {
            val params = (data?.toJSONObject() ?: JSONObject()) as JSONObject
            val headers = HashMap<String, String>().apply {
                db?.settings?.yummyToken?.let { token ->
                    put("Authorization", "YummyAuth $token")
                }
            }
            val ans: ReqResponse = if (method == "GET") {
                request.loadTextFromUrl(Config.REST_API_YUMMYANIME + path, params.run {
                    HashMap<String, String>(this.length()).also { dict ->
                        for (i in this.keys())
                            dict[i] = this[i].toString()
                    }
                }, headers)
            } else {
                request.method(method, Config.REST_API_YUMMYANIME + path, params, headers)
            }

            return ans.toJson()?.run {
                getJSONObject("response").convertTo(T::class)
            } ?: throw JSONException("Invalid answer: $ans")
        }catch (e: InvalidStatusException){
            throw YummyError.getError(JSONObject(e.response))
        }
    }
}