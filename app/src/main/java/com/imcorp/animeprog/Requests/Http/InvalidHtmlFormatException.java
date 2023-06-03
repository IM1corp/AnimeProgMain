package com.imcorp.animeprog.Requests.Http;


import java.io.IOException;

public class InvalidHtmlFormatException extends IOException {
    public InvalidHtmlFormatException(String i){
        super(i);
    }
    public static String getHtmlError(final String err){
        return "Html format exception - "+err;
    }
    public static class NoVideosFoundException extends InvalidHtmlFormatException{
        public NoVideosFoundException(String i) {
            super(i);
        }
    }
    public static class InvalidUrl extends InvalidHtmlFormatException{
        public final String url;
        public InvalidUrl(String url, String i) {
            super(i);
            this.url = url;
        }
    }
}
