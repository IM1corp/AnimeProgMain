package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.profile

import android.os.Parcel
import android.os.Parcelable
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField

class YummyUser(
    @SerializableField("id") public var id: Int = 0,
    @SerializableField("ids") public var socialMedia: YummySocialMedia = YummySocialMedia(),
    @SerializableField("banned") public var banned: Boolean = false,
    @SerializableField("register_date") public var registerDate: Long = 0,
    @SerializableField("roles") public var roles: Array<String> = arrayOf(),
    @SerializableField("bdate") public var bdate: String = "",
    @SerializableField("sex") public var sex: Byte = 0,
    @SerializableField("last_online") public var lastOnline: Long = 0,
    @SerializableField("about") public var about: String = "",
    @SerializableField("texts") public var Texts: YummyProfileTexts? = null,
    @SerializableField("avatar") public var avatar: String = "",
    @SerializableField("nickname") public var nickname: String = ""

) : Parcelable {

    open val avatarSrc: String get() = if(avatar.startsWith("/")) Config.YUMMY_ANIME_URL+avatar else avatar

    companion object CREATOR : Parcelable.Creator<YummyUser> {
        override fun createFromParcel(parcel: Parcel): YummyUser =YummyUser(
            parcel.readInt(),
            parcel.readParcelable(YummySocialMedia::class.java.classLoader)!!,
            parcel.readByte() != 0.toByte(),
            parcel.readLong(),
            parcel.createStringArray()!!,
            parcel.readString()!!,
            parcel.readByte(),
            parcel.readLong(),
            parcel.readString()!!,
            parcel.readParcelable(YummyProfileTexts::class.java.classLoader),
            parcel.readString()!!
        )

        override fun newArray(size: Int): Array<YummyUser?> = arrayOfNulls(size)
    }
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeParcelable(socialMedia, flags)
        parcel.writeByte(if (banned) 1 else 0)
        parcel.writeLong(registerDate)
        parcel.writeStringArray(roles)
        parcel.writeString(bdate)
        parcel.writeByte(sex)
        parcel.writeLong(lastOnline)
        parcel.writeString(about)
        parcel.writeParcelable(Texts, flags)
        parcel.writeString(avatar)
    }
    override fun describeContents(): Int {
        return 0
    }



}