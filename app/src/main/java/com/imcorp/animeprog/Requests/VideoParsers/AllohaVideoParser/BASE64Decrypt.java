package com.imcorp.animeprog.Requests.VideoParsers.AllohaVideoParser;

import android.util.Base64;

import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;

import static com.imcorp.animeprog.Requests.VideoParsers.AllohaVideoParser.AllohaVideoParser.FILE_SEPARATOR;

public class BASE64Decrypt implements SimpleDataDecrypter {
    private final static HashMap<String,String> v = new HashMap<String, String>() {{
        put("bk0", "?|;^^|*>*>??>^|^<|>|?!*№(|;!?^№>");
        put("bk1","?;>)!(*;||>|*<^|*|^*`>?|(|*>||~][|>|*^*");
        put("bk2","<`^*`*>|№**№]?[*;||>|*№;^*`№*>");
        put("bk3","|[>*№>^?[;||>|*<**№]||^<**|");
        put("bk4",";!?^№>*^*`||^<*№||^*`^**|№*~][|>|");
    }};
    @Override
    public boolean checkIf(String i) {
        return i.startsWith("#9");
    }
    @Override
    public String decrypt(final String data) throws UnsupportedEncodingException, InvalidHtmlFormatException {
        String a = data.substring(2);
        for (byte i = 4; i >=0; i--) {
            String thing_to_replace = FILE_SEPARATOR + b1(v.get("bk" + i));

            a = a.replace(thing_to_replace,"");
        }
        return b2(a);
    }
    private static String encodeURIComponent(String s) {
        String result = null;

        try {
            result = URLEncoder.encode(s, Config.coding)
                    .replaceAll("\\+", "%20")
                    .replaceAll("%21", "!")
                    .replaceAll("%27", "'")
                    .replaceAll("%28", "(")
                    .replaceAll("%29", ")")
                    .replaceAll("%7E", "~");
        }
        catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }

    private String b1(String str) throws UnsupportedEncodingException {
        final String chars = "0123456789ABCDEF";
        String thing = encodeURIComponent(str);
        StringBuilder answer = new StringBuilder();
        for(int i=0;i<thing.length();i++){
            char ch = thing.charAt(i);
            if(ch=='%') {
                char next = thing.charAt(i + 1);
                int val = 0;
                if ((val = chars.indexOf(next)*16)!=-16) {//0-9A-F
                    int next_index = chars.indexOf(thing.charAt(i + 2));
                    if(next_index!=-1){
                        val+=next_index;
                        ch = (char)val;
                        answer.append(ch);
                        i+=2;
                        continue;
                    }
                }
            }
            answer.append(ch);
        }
        //return DatatypeConverter.printBase64Binary(answer.toString().getBytes("latin1"));
        String responce = android.util.Base64.encodeToString(answer.toString().getBytes("latin1"), Base64.DEFAULT );
        if(responce.endsWith("\n")){
            responce = responce.substring(0,responce.length()-1);
        }
        return responce;
    }
    private String b2(String str) throws UnsupportedEncodingException, InvalidHtmlFormatException {
        try {

            char[] splited = new String(fromBase64(str), Config.coding).toCharArray();
            StringBuilder answer_encoded = new StringBuilder();
            for (char c:splited) {
                String append_thing = "00"+Integer.toString((int)c,16);
                answer_encoded.append("%")
                        .append(
                                append_thing.substring(append_thing.length()-2)
                        );
            }
            return URLDecoder.decode(answer_encoded.toString(),Config.coding);
        }catch (Exception e){
            throw new InvalidHtmlFormatException("Can not base64 decode str");
        }
    }
    public static byte[] fromBase64(String str) throws UnsupportedEncodingException {
        return android.util.Base64.decode(str.getBytes(Config.coding),Base64.DEFAULT);
        //return new byte[1];
        //return DatatypeConverter.parseBase64Binary(str);
    }

}
