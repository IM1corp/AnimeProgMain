package com.imcorp.animeprog.Requests.JsonObj.comments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcel
import android.os.Parcelable
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import java.util.*

class OneComment(var user:OneAnime.Link?=null,
                 var date: Date? = null,
                 var likeDislikes:Int = 0,
                 var dislikes:Int = 0,
                 var likes:Int = 0,
                 var userCoverUrl: String? = null,
                 var commentId:Long=0,
                 var subComments: LinkedList<OneComment> = LinkedList(),
                 var dataTextHtml: String="") : Parcelable {

    public fun loadUserCoverImage(activity: MyApp):Bitmap?{
        return userCoverUrl?.run {
            val cacheFile = activity.dataBase.cache.tryGetImgFromCache(userCoverUrl);
            if(cacheFile != null) return@run BitmapFactory.decodeFile(cacheFile);

            activity.request.loadImageFromUrl(this)?.also{
                activity.dataBase.cache.saveImgToCache(userCoverUrl, it)
            }
//            ansBitmap
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(user, flags)
        parcel.writeLong(date?.time?:0)
        parcel.writeInt(likeDislikes)
        parcel.writeInt(likes)
        parcel.writeInt(dislikes)
        parcel.writeString(userCoverUrl)
        parcel.writeLong(commentId)
        parcel.writeTypedList(subComments)
        parcel.writeString(dataTextHtml)
    }
    private constructor(parcel: Parcel) : this(
            parcel.readParcelable(OneAnime.Link::class.java.classLoader),
            Date(parcel.readLong()),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readLong(),
            LinkedList(parcel.createTypedArrayList(CREATOR)),
            parcel.readString()!!)

    override fun describeContents(): Int {
        return 0
    }
    companion object {
        @JvmField val CREATOR = object:Parcelable.Creator<OneComment> {
        override fun createFromParcel(parcel: Parcel): OneComment {
            return OneComment(parcel)
        }

        override fun newArray(size: Int): Array<OneComment?> {
            return arrayOfNulls(size)
        }
    }}
}