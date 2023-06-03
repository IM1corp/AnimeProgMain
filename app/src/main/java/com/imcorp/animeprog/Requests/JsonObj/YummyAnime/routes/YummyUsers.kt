package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.routes

import androidx.annotation.Keep
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.YummyRestApi
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.profile.YummyUser

class YummyUsers(restApi: YummyRestApi) : SimpleYummyApi(restApi) {
    fun login(username: String, password: String, captchaResponse: String? = null): YummyAuthResponse =
            restApi.method("/profile/login", "POST", ProfileParams(
                    username, password, captchaResponse
            ))
    fun profile(): YummyUser = restApi.method("/profile", "GET")
    public class ProfileParams(
            @Keep @SerializableField("login") public var login: String,
            @Keep @SerializableField("password") public var password: String,
            @Keep @SerializableField("recaptcha_response") public var captchaString: String?=null,
            @Keep @SerializableField("need_json") public var needJson: Boolean = true
    )
    public class YummyAuthResponse(
            @Keep @SerializableField("token") public var token: String="",
            @Keep @SerializableField("success") public var success: Boolean=false
    )
}