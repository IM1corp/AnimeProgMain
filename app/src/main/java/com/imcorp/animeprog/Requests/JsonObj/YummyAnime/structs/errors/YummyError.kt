package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.errors

import com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField
import com.imcorp.animeprog.Requests.JsonObj.Serialization.convertTo
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.errors.all.ArgumentsApiError
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.errors.all.CaptchaError
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.errors.all.FloodControlError
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.errors.all.InvalidPasswordError
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.errors.all.NotAuthorizedError
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.errors.all.NotFoundError
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.errors.all.PermissionError
import org.json.JSONObject

open class YummyError(
    cause: Throwable?=null
): Exception(cause) {
    @SerializableField("error") public var error: String="Undefined";
    @SerializableField("error_code") public open var errorCode: ErrorCodes = ErrorCodes.Undefined
    @SerializableField("error_title") public open var errorTitle: String=""
    @SerializableField("error_name") public open var errorName: String=""
    override fun toString(): String {
        return "${this::class.simpleName}(code=$errorCode, error='$error', title='$errorTitle', name=$errorName)"
    }

    override fun getLocalizedMessage() = this.message

    companion object{
        fun getError(data: JSONObject) : YummyError = when(data.getInt("error_code")){
            ErrorCodes.NotAuthorizedException.value -> data.convertTo(NotAuthorizedError::class)
            ErrorCodes.ArgumentsError.value -> data.convertTo(ArgumentsApiError::class)
            ErrorCodes.CaptchaError.value -> data.convertTo(CaptchaError::class)
            ErrorCodes.NotFountError.value -> data.convertTo(NotFoundError::class)
            ErrorCodes.FloodControlException.value -> data.convertTo(FloodControlError::class)
            ErrorCodes.InvalidPasswordException.value -> data.convertTo(InvalidPasswordError::class)
            ErrorCodes.PermissionDeniedException.value -> data.convertTo(PermissionError::class)
            else -> data.convertTo(YummyError::class)
        }
    }
}