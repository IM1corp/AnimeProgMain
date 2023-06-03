package com.imcorp.animeprog.Requests.VideoParsers.AllohaVideoParser;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestClass {
    private static String base62FuncE(int c,final int a){
        return (c < a ? "" : base62FuncE(c / a,a)) + ((c = c % a) > 35 ? (char)(c + 29) : Integer.toString(c,36));
    }
    private static final Pattern base64Pattern = Pattern.compile("\\b\\w+\\b");
    static String base62Func(final String p, final int a, int c, final String[] k, final int e_, final HashMap<String, String> d){
        while (c--!=0) {
            String key = base62FuncE(c,a);
            d.put(key,k[c].isEmpty()?key:k[c]);
        }
        Matcher matcher = base64Pattern.matcher(p);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()){
            String replacement = d.get(matcher.group(0));
            matcher.appendReplacement(buffer,replacement!=null?replacement:matcher.group(0));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

}
