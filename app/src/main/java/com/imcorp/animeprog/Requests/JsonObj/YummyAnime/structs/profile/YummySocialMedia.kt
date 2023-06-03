package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.profile

import android.os.Parcel
import android.os.Parcelable
import com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField

class YummySocialMedia(
        @SerializableField("vk") public var vkId: Int? = null,
        @SerializableField("tg") public var tgId: Int? = null,
        @SerializableField("tg_nickname") public var tgNickname: String? = null
): Parcelable{
        constructor(parcel: Parcel) : this(
                parcel.readValue(Int::class.java.classLoader) as? Int,
                parcel.readValue(Int::class.java.classLoader) as? Int,
                parcel.readString()
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeValue(vkId)
                parcel.writeValue(tgId)
                parcel.writeString(tgNickname)
        }

        override fun describeContents(): Int {
                return 0
        }

        companion object CREATOR : Parcelable.Creator<YummySocialMedia> {
                override fun createFromParcel(parcel: Parcel): YummySocialMedia {
                        return YummySocialMedia(parcel)
                }

                override fun newArray(size: Int): Array<YummySocialMedia?> {
                        return arrayOfNulls(size)
                }
        }

}