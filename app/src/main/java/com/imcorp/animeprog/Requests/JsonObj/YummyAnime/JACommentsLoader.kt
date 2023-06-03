package com.imcorp.animeprog.Requests.JsonObj.YummyAnime

import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException
import com.imcorp.animeprog.Requests.Http.ReqBuild
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.Requests.JsonObj.comments.OneComment
import org.json.JSONArray
import org.json.JSONObject

class JACommentsLoader(private val activity: MyApp, private val defaultUrl: String, private val refererUrl: String?) {
    public var hasMore: Boolean = false
    private lateinit var response: JSONArray
    public fun loadComments(offset: Int, count: Int): List<OneComment>{
        val url = if(defaultUrl.startsWith("/")) Config.YUMMY_ANIME_URL+defaultUrl else defaultUrl
        val reqBuild = ReqBuild(activity, url, false)
                .add("skip", offset)
                .addHeader("Referer", refererUrl)
                .addXRequestsWithHeader()
        val jsonResponse = reqBuild.SendRequest().toJson()
        if(jsonResponse?.has("comments")==true){
            response = jsonResponse.getJSONArray("comments")
            hasMore = response.length() >= 20
        }
        else throw InvalidHtmlFormatException(InvalidHtmlFormatException.getHtmlError("No comments found"))

        return this.loadData()
    }
    private fun loadData(): List<OneComment> = (0 until response.length()).map {
        parseOneComment(response.getJSONObject(it))
    }

    private fun parseOneComment(el: JSONObject): OneComment = OneComment().apply{
        this.commentId = el.getLong("id")
        this.userCoverUrl = el.getString("avatar")
        if(userCoverUrl?.startsWith("/")==true) userCoverUrl = Config.YUMMY_ANIME_URL+userCoverUrl
        this.user = OneAnime.Link(el.getString("name"), "/users/id${el.getInt("user_id")}")

        //this.dataTextHtml = formatHtml(el.getString("text"))

        this.likes = el.getInt("likes")
        this.dislikes = el.getInt("dislikes")
        this.likeDislikes + this.likes-this.dislikes
        val html = HtmlReplaceRegex.replace(el.getString("text")) { match ->
            val tag = when (match.groupValues[1]) {
                "ж" -> "b"
                "и" -> "i"
                "п" -> "u"
                "з" -> "s"
                else -> "p"
            }
            "<$tag>${match.groupValues[2]}</$tag>"
        }
        this.dataTextHtml = html
//        val commentObj = parseOneComment(childComment)
//        this.subComments.addLast(commentObj)
    }
    companion object{
        val HtmlReplaceRegex = Regex("\\[([жкпз])](.+?)\\[/\\1]")
//        private val dateFormater by lazy {
//            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
//        }
//        private fun formatHtml(t: String): Spannable {
//            val html = Html.fromHtml(t, Html.FROM_HTML_SEPARATOR_LINE_BREAK_DIV or Html.FROM_HTML_OPTION_USE_CSS_COLORS, )
//            return html
//        }
    }
}