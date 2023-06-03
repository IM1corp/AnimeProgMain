package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.profile

import android.os.Parcel
import android.os.Parcelable
import com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField

class YummyProfileTexts(
        @SerializableField("left") public var left: String = "",
        @SerializableField("right") public var right: String = "",
        @SerializableField("color") public var color: Int = 0
): Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readInt()
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(left)
                parcel.writeString(right)
                parcel.writeInt(color)
        }

        override fun describeContents(): Int {
                return 0
        }

        companion object CREATOR : Parcelable.Creator<YummyProfileTexts> {
                override fun createFromParcel(parcel: Parcel): YummyProfileTexts {
                        return YummyProfileTexts(parcel)
                }

                override fun newArray(size: Int): Array<YummyProfileTexts?> {
                        return arrayOfNulls(size)
                }
        }
}