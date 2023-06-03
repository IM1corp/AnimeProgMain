package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.errors

import java.sql.Types

enum class ErrorCodes(val value: Int) {
    Undefined(0),
    NotAuthorizedException(1),
    PermissionDeniedException(2),
    ArgumentsError(3),
    NotFountError(4),
    CaptchaError(5),
    FloodControlException(6),
    InvalidPasswordException(7);
    companion object {
        fun fromInt(value: Int) = ErrorCodes.values().first { it.value == value }
    }
}