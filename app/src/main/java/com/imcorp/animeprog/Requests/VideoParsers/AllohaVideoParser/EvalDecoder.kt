package com.imcorp.animeprog.Requests.VideoParsers.AllohaVideoParser

import android.util.Log
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException
import com.imcorp.animeprog.Requests.JsonObj.System.JsonParser
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.regex.Pattern

internal class EvalDecoder {
    private val builder:StringBuilder = StringBuilder()
    @Throws(JSONException::class, InvalidHtmlFormatException::class)
    fun decodeFile(code: String): EvalDecoder {
        var index=0

        while(true){
            val nextIndex = code.indexOf("eval", startIndex = index)
            if(nextIndex==-1) {
                builder.append(code.substring(index))
                break
            }
            else{
                builder.append(code.substring(index, nextIndex))
                val evalThing = JsonParser.findArgs(code.substring(nextIndex), false)//eval(.....)
                index = nextIndex + evalThing.length
                val decryptedCode = decryptEVAL("eval" + evalThing)
                builder.append(decryptedCode)
            }
        }
        builder.append(code)
        return this
    }
    private fun decryptEVAL(code: String) : String{
        var code=code
        while (code.contains("eval(")) {
            val params_str = JsonParser.findArgs(code, true)
            val params = ArrayList<String>(6)
            //region split
            var in_s = false
            var last_index = 1
            var in_s_ch = '_'
            for (i in 1 until params_str.length - 1) {
                val ch = params_str[i]
                if ((ch == '"' || ch == '\'') && (params_str[i - 1] != '\\' || params_str[i - 2] == '\\')) {
                    if (!in_s || ch == in_s_ch) {
                        in_s = !in_s
                        in_s_ch = ch
                    }
                } else if (ch == ',' && !in_s) {
                    params.add(params_str.substring(last_index, i).trim(' '))
                    last_index = i + 1
                }
            }
            params.add(params_str.substring(last_index, params_str.length - 1).trim(' '))
            if(params.size!=6) {
                if (Config.NEED_LOG) Log.e(Config.ALLOHA_VIDEO_PARSER_LOG, "Invalid params length: got ${params.size} ( need 6 )\n")
                break
            }
            //assert(params.size == 6) { "Invalid params length" }
            code =( if (isBase62Func.matcher(code).find()) {
                TestClass.base62Func(
                        sParam(params[0]),
                        iParam(params[1]),
                        iParam(params[2]),
                        splitedParam(params[3]),
                        iParam(params[4]),
                        hashMapParam(params[5])
                )
            } else {
                evalFunc(
                        sParam(params[0]),
                        iParam(params[1]),
                        sParam(params[2]),
                        iParam(params[3]),
                        iParam(params[4]),
                        iParam(params[5])
                )
            })
        }
        return code
    }
    override fun toString(): String = builder.toString()
    private fun funcInEval(d: String, e: Int, f: Int): String {
        val g = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ+/"
        val h = g.substring(0, e)
        val i = g.substring(0, f)
        val j_ = StringBuilder(d).reverse().toString().toCharArray()
        var j = 0
        for (c in j_.indices) {
            val ch = j_[c]
            val index = h.indexOf(ch)
            if (index != -1) {
                j += (index * Math.pow(e.toDouble(), c.toDouble())).toInt()
            } else {
                j = -1
                break
            }
        }
        val k = StringBuilder()
        while (j > 0) {
            //k.insert(0,i.charAt(j % f));
            k.append(i[j % f])
            j = (j - j % f) / f
        }
        val answer = k.reverse().toString()
        return if (!answer.isEmpty()) answer else "0"
    }

    private fun evalFunc(h: String, u: Int, n: String, t: Int, e: Int, r_: Int): String {
        //Последний параметр игнорим
        val r = StringBuilder()
        var i = 0
        val len = h.length
        while (i < len) {
            val sB = StringBuilder()
            while (h[i] != n[e]) {
                sB.append(h[i])
                i++
            }
            var s = sB.toString()
            for (j in n.indices) {
                s = s.replace(n[j].toString(), j.toString())
            }
            //r.append(String.fromCharCode(_0xe78c(s, e, 10) - t));
            r.append((funcInEval(s, e, 10).toInt() - t).toChar())
            i++
        }
        return r.toString()
    }

    companion object {
        private val isBase62Func by lazy {Pattern.compile("(p, ?a, ?c, ?k)")}
        private val integer by lazy{Pattern.compile("(\\d+)")}
        private val base64Pattern by lazy { Pattern.compile("\\b\\w+\\b")}
        private fun sParam(string: String): String {
            val answer = StringBuilder()
            val aa = string.substring(1, string.length - 1)
            var i = 0
            var c = 0
            while (i < aa.length) {
                if (aa[i] != '\\') {
                    if (c != 0) {
                        if (c > 1) for (b in 0 until c / 2) answer.append('\\')
                        if (c % 2 != 0 && (aa[i] == '\'' || aa['\"'.toInt()] == '"')) answer.append('\'') else answer.append(aa[i])
                        c = 0
                    } else answer.append(aa[i])
                } else c++
                i++
            }
            return answer.toString()
        }
        private fun iParam(integer_: String): Int {
            val m = integer.matcher(integer_)
            if (!m.find()) return 0
            val value = m.group(1)
            return value!!.toInt()
        }

        @Throws(JSONException::class)
        private fun hashMapParam(array: String): HashMap<String, String> {
            val answer = HashMap<String, String>()
            val json = JSONObject(array)
            val it = json.keys()
            while (it.hasNext()) {
                val key = it.next()
                answer[key] = json.getString(key)
            }
            return answer
        }

        private fun splitedParam(splited: String): Array<String> {
            var splited = splited
            val index_a = splited.lastIndexOf("split")
            if (index_a >= 0) splited = splited.substring(1, index_a)
            for (i in splited.length - 1 downTo 0) {
                if (splited[i] == '\'' || splited[i] == '"') {
                    splited = splited.substring(0, i)
                    break
                }
            }
            return splited.split("|").toTypedArray()
        }
    }
}