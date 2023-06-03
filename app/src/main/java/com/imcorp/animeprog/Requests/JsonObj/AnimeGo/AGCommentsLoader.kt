package com.imcorp.animeprog.Requests.JsonObj.AnimeGo

import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.Default.toHtml
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException
import com.imcorp.animeprog.Requests.Http.ReqBuild
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.Requests.JsonObj.comments.OneComment
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AGCommentsLoader(private val activity: MyApp, private val defaultUrl: String, private val refererUrl: String?) {
    public var hasMore: Boolean = false
    private lateinit var response: Document
    public fun loadComments(offset: Int, count: Int): ArrayList<OneComment>{
        val url = if(defaultUrl.startsWith("/")) Config.ANIMEGO_URL+defaultUrl else defaultUrl
        val reqBuild = ReqBuild(activity, url, false)
                .add("view", "list")
                .add("page", (offset / count) + 1)
                .addHeader("Referer", refererUrl)
                .addXRequestsWithHeader();
        val jsonResponse = reqBuild.SendRequest().toJson()
        if(jsonResponse?.getString("status") == "success"){
            hasMore = !jsonResponse.getBoolean("endPage")
            response = jsonResponse.getString("content").toHtml()
        }
        else throw InvalidHtmlFormatException(InvalidHtmlFormatException.getHtmlError("No comments found"))

        return this.loadData()
    }
    private fun loadData(): ArrayList<OneComment> = response.select(".comment").run{
        val ans = ArrayList<OneComment>(this.size)
        for(el in this) ans.add(parseOneComment(el))
        ans
    }

    private fun parseOneComment(el: Element):OneComment =OneComment().apply{
        this.commentId = try{el.attr("data-id")?.toLong()?:0}catch(e: NumberFormatException){0}
        this.userCoverUrl = el.select(".comment-avatar img")?.attr("src")
        if(userCoverUrl?.startsWith("/")==true) userCoverUrl = Config.ANIMEGO_URL+userCoverUrl
        el.selectFirst(".comment-author")?.let{ authorEl->
            authorEl.selectFirst("a")?.let{
                this.user = OneAnime.Link(it)
            }
            authorEl.selectFirst("time")?.let{
                this.date = dateFormater.parse(it.attr("datetime"))
            }
        }
        el.selectFirst(".comment-text div")?.let{commentEl->
            for (image in commentEl.select("img")) {
                image.attr("src")?.let{
                    if(it.startsWith("/"))
                        image.attr("src", Config.ANIMEGO_URL+it)
                }

            }

            this.dataTextHtml = commentEl.html()
        }
        this.likeDislikes = try{
            el.selectFirst(".comment-actions .text-danger,.comment-actions .text-success")?.text()?.toInt()?:0
        }catch(e:NumberFormatException){ 0 }
        for(childComment in el.select(".children .comment")){
            val commentObj = parseOneComment(childComment)
            this.subComments.addLast(commentObj)
            //TODO: add show more comments button
        }
    }
    companion object{
        private val dateFormater by lazy {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        }
    }
}