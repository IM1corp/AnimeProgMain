package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.errors.all

import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.errors.ErrorCodes
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.errors.YummyError

class ArgumentsApiError(cause: Throwable?=null) : YummyError(cause){
}