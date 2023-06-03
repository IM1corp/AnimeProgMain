package com.imcorp.animeprog.Default

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.util.regex.Pattern

fun Element.find(cssQuery:String) = this.select(cssQuery)!!
inline fun <reified T> isString():Boolean{
    return (T::class==String::class)
}

object HtmlUtils {
    private val regexNewLine:Regex = "\\\\n+".toRegex()
    private val findStringInString:Regex = "[\"'](.+?)[\"']".toRegex();
    fun getTextWithNewLines(el: Element) : String {
        for (i in el.getElementsByTag("br")) i.replaceWith(TextNode("\\n"))
        return regexNewLine.replace(el.text(),"\n")
    }
    fun getImageFromStyles(el:Element) : String ?{
        el.attr("data-original").let{
            if(!it.isNullOrBlank())return it;
        }
        el.attr("data-src").let{
            if(!it.isNullOrBlank())return it
        }
        val styles = el.attr("style")
        for(i in styles.split(';')){
            val data = i.split(':',limit = 2)
            val key = data[0]
            var value = data[1]
            if(key.contains("background")){
                value = value.trimStart()
                value = if(value.startsWith("image"))
                        value.substring(5)
                    else if(value.startsWith("url"))
                        value.substring(3)
                    else break
                value = value.trim()
                if(!value.startsWith('(')||!value.endsWith(')'))break
                value = value.substring(1,value.length-1)
                if(value[0] in "'\"" && value[value.length-1] in "'\"")
                    value = value.substring(1,value.length-1)
                return value
            }
        }
        return null
    }

    fun parseHrefFromElement(element: Element) : String?{
        element.attr("onclick")?.let{onclickStr->
            if(onclickStr.isNotEmpty()){
                val matches:MatchResult? = findStringInString.find(onclickStr)
                matches?.groups?.get(1)?.value?.let{
                    return it;
                }
            }
        }
        element.attr("href").let{
            if(!it.isNullOrBlank()) return it;
        }
        return null;
    }

}

fun String.toHtml(location:String?=null) = Jsoup.parse(this,location?:"")
val Element.backgroundFromCss: String get() {
    return FIND_URL.matcher(this.attr("style")?:"url( )").run {
        if (find()) group(1)?:"" else ""
    }
}
val FIND_URL by lazy { Pattern.compile("url\\(['\"]?(.+?)['\"]?\\)")
}

