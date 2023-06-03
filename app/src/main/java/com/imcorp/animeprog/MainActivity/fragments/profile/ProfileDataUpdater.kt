package com.imcorp.animeprog.MainActivity.fragments.profile

import android.util.Log
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.profile.YummyUser

class ProfileDataUpdater(private val fragment: ProfileFragment) {
    fun update() {
        if(fragment.profile != null)
            this.updateData(fragment.profile!!)
        else this.clearData()
    }

    private fun clearData() = with(fragment.viewBinding) {
        nickName.text = ""
        siteDescription.text = ""
        avatar.setImageResource(R.drawable.ic_profile)
    }

    private fun updateData(profile: YummyUser)=with(fragment.viewBinding) {
        //TODO: add to cache
        Thread{
            try {
                val image = fragment.activity.request.loadImageFromUrl(profile.avatarSrc)
                (fragment?.getActivity() as? MyApp)?.threadCallback?.post {
                    avatar.setImageBitmap(image)
                }
            }catch (e: Throwable){
                Log.e("DownloadImageError", e.localizedMessage, e)
            }
        }.also{it.start()}
        nickName.text = profile.nickname
        siteDescription.text = profile.about

    }

}