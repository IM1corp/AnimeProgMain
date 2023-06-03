package com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeVideo.controls

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeVideo.FragmentVideoPlayer
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEpisodeQuality
import java.util.ArrayList

class VideoQualitiesMenu(v:View,private val videoQualities: ArrayList<OneVideoEpisodeQuality>,private val fragment:FragmentVideoPlayer) : PopupMenu(ContextThemeWrapper(v.context, R.style.DarkPopupMenuStyle), v, Gravity.TOP) {
    private val selectedStyle = StyleSpan(Typeface.BOLD_ITALIC)
    private val items: List<MenuItem?>
    private val isList=videoQualities.size>1
    init{
        items = if(isList) getDefaultListItems() else getVideoViewListItems()
        fragment.requireActivity().menuInflater.inflate(R.menu.default_menu, menu)
        if(isList)defaultList()
        else videoViewList()

    }
    private fun videoViewList() = setOnMenuItemClickListener { menuItem->
        for (i in items.indices) {
            if (i != fragment.selectedQuality && items[i] == menuItem) { //items[i].getTitle().equals(menuItem.getTitle())){
                try {
                    fragment.selectedQuality = i
                    fragment.videoView.setVideoQuality(fragment.videoView.availableVideoQualities[i])
                } catch (e: InvalidHtmlFormatException) {
                    (fragment.activity as MyApp).showInvalidHtmlException(e)
                }
                break
            }
        }
        true
    }
    private fun defaultList() = setOnMenuItemClickListener { menuItem ->
        for (i in items.indices) {
            if (i != fragment.selectedQuality && items[i] == menuItem) { //items[i].getTitle().equals(menuItem.getTitle())){
                try {
                    fragment.setVideoUrl(i)
                } catch (e: InvalidHtmlFormatException) {
                    (fragment.activity as MyApp).showInvalidHtmlException(e)
                }
                break
            }
        }
        true
    }
    private fun getDefaultListItems () = videoQualities.mapIndexed { index, i ->
        val text = SpannableString(OneVideoEpisodeQuality.getQualityString(i.quality, fragment.activity))
        if (index == fragment.selectedQuality)
            text.setSpan(selectedStyle, 0, text.length, 0)
        menu.add(text)
    }
    private fun getVideoViewListItems() = fragment.videoView.availableVideoQualities.mapIndexed { index, i ->
        val text = SpannableString(OneVideoEpisodeQuality.getQualityString(i, fragment.activity))
        if (index == fragment.selectedQuality)
            text.setSpan(selectedStyle, 0, text.length, 0)
        menu.add(text)
    }
}