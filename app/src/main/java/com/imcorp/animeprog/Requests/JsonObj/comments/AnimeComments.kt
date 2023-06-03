package com.imcorp.animeprog.Requests.JsonObj.comments

import android.os.Parcel
import android.os.Parcelable
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.Requests.JsonObj.AnimeGo.AGCommentsLoader
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.JACommentsLoader
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.YummyRestApi
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.routes.YummyComments

class AnimeComments() : Parcelable {
    public var defaultUrl:String?=null
    public var commentsList: ArrayList<OneComment> = ArrayList()
    public var fullyLoaded = false
    private var count:Int = -1

    public fun loadComments(anime:OneAnime, activity: MyApp, offset:Int=0){
        when (anime.HOST) {
            Config.HOST_ANIMEGO_ORG -> {
                val loader = AGCommentsLoader(activity, defaultUrl!!, anime.animeURI)
                commentsList.addAll(loader.loadComments(offset, 20))
                fullyLoaded = !loader.hasMore
            }
            Config.HOST_GOGO_ANIME ->{
                fullyLoaded = true // TODO: add comments
            }
            Config.HOST_ANIME_JOY ->{
                fullyLoaded = true // TODO: add comments
            }
            Config.HOST_YUMMY_ANIME ->{
                val comments = YummyRestApi(activity.request, activity.dataBase).comments.getComments(
                        anime.animeId,
                        skip=this.count,
                        parentId=0,
                        sort=YummyComments.CommentsSort.Relevant
                )

//                val loader = JACommentsLoader(activity, defaultUrl!!, anime.animeURI)
                commentsList.addAll(comments.comments.map{it.toOneComment()})
                fullyLoaded = true
            }

            else -> TODO("NOT YET IMPLEMENTED")
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(defaultUrl)
        parcel.writeByte(if (fullyLoaded) 1 else 0)
        parcel.writeInt(count)
    }
    override fun describeContents()=0
    private constructor(parcel: Parcel):this() {
        defaultUrl = parcel.readString()
        fullyLoaded = parcel.readByte() != 0.toByte()
        count = parcel.readInt()
    }

    companion object{
        @JvmField
        val CREATOR = object: Parcelable.Creator<AnimeComments> {
            override fun createFromParcel(parcel: Parcel): AnimeComments {
                return AnimeComments(parcel)
            }

            override fun newArray(size: Int): Array<AnimeComments?> {
                return arrayOfNulls(size)
            }
        }
    }
}