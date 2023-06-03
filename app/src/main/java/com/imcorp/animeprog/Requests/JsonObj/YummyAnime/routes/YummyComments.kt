package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.routes

import com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.YummyRestApi
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.anime.YummyComment

class YummyComments(restApi: YummyRestApi) : SimpleYummyApi(restApi) {
    public fun getComments(
            animeId: Int, type: CommentType=CommentType.Anime,
            skip: Int=0, parentId: Int=0, sort:CommentsSort=CommentsSort.New
    ): YummyCommentsAns = restApi.method<YummyCommentsAns>(
            "/comments/$type/$animeId",
            data=HashMap<String, Any>().apply{
                this["sort"] = sort.toString()
                this["skip"] = skip
                this["parent_id"] = parentId
            }
    )

    class YummyCommentsAns(
            @SerializableField("comments") public var comments: List<YummyComment> = listOf(),
            ) {
        public val hasMore get() = comments.size >= 20
    }
    enum class CommentsSort {
        New,
        Old,
        Relevant;

        override fun toString(): String {
            return when (this) {
                Old -> "old"
                Relevant -> "nice"
                else -> "new"
            }
        }
    }
    enum class CommentType{
        Anime,
        Post,
        Review;
        override fun toString(): String {
            return when(this){
                Review->"review"
                Post->"post"
                else->"anime"
            }
        }
    }
}