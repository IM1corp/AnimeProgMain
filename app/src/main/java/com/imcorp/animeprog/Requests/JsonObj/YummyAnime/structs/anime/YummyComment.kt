package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.anime

import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField
import com.imcorp.animeprog.Requests.JsonObj.comments.OneComment
import java.util.*

class YummyComment() {
    @SerializableField("user_id") public var userId: Long = 0
    @SerializableField("user_group") public var userGroup: Byte = 0 // 1|0
    @SerializableField("deleted_at") public var deletedAt: Long = 0
    @SerializableField("name") public var nickName: String = ""
    @SerializableField("id") public var commentId: Long = 0
    @SerializableField("time") public var publishDate: Long = 0
    @SerializableField("likes") public var likesCount: Int = 0
    @SerializableField("dislikes") public var dislikesCount: Int = 0
    @SerializableField("text") public var textHtml: String = ""
    @SerializableField("avatar") public var avatarSrc: String = ""
    @SerializableField("vote") private var likeAction: Int? = null
    @SerializableField("children_count") private var childrenCount: Int = 0
    @SerializableField("parent_id") private var parentId: Int = 0

    public val likedByMe get() = likeAction == 1
    public val isDikedByMe get() = likeAction == -1
    public val deleted get() = deletedAt > 0
    public fun toOneComment(): OneComment = OneComment(
            user=OneAnime.Link(nickName, "/users/id$userId"),
            date= Date(publishDate),
            likeDislikes=likesCount-dislikesCount,
            dislikes=dislikesCount,
            likes=likesCount,
            userCoverUrl=if(avatarSrc.startsWith("/")) Config.YUMMY_ANIME_URL+avatarSrc else avatarSrc,
            commentId=commentId,
            subComments= LinkedList(),
            dataTextHtml=this.textHtml
    )
}