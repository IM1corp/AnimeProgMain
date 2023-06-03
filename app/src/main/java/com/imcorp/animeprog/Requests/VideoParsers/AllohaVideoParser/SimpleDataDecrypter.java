package com.imcorp.animeprog.Requests.VideoParsers.AllohaVideoParser;

import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

public interface SimpleDataDecrypter {
    public boolean checkIf(String i);
    abstract public String decrypt(String input) throws GeneralSecurityException, UnsupportedEncodingException, InvalidHtmlFormatException;
}
