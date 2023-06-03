package com.imcorp.animeprog.Requests.Http;

import java.io.IOException;

public class InvalidStatusException extends IOException {
    public final String response;
    public int status;
    public InvalidStatusException(String error, int status, String errorMessage){
        super(error);
        this.response = errorMessage;
        this.status=status;
    }
}
