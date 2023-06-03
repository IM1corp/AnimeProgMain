package com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeVideo.controls

import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.appcompat.view.ContextThemeWrapper
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.Default.MyPopupMenu
import com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeVideo.FragmentVideoPlayer
import com.imcorp.animeprog.R
//import kotlinx.android.synthetic.main.fragment_video_player_controls.*


class OrientationHandler(private val fragment: FragmentVideoPlayer) : View.OnClickListener,View.OnLongClickListener {
    companion object{
        const val ORIENTATION_AUTO:Byte = 1
        const val ORIENTATION_LANDSCAPE:Byte = 2
        const val ORIENTATION_PORTRAIT:Byte = 3
        const val ORIENTATION_LANDSCAPE_ROTATED:Byte = 4
    }
    private var orientationState:Byte = ORIENTATION_AUTO
    private var requestedOrientation get()=fragment.requireActivity().requestedOrientation; set(value){fragment.requireActivity().requestedOrientation=value }
    private var animation: ViewPropertyAnimator? = null
    public fun changeOrientation(stateTo: Byte = ORIENTATION_AUTO){
        orientationState = stateTo
        requestedOrientation = when(stateTo) {
            ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            ORIENTATION_LANDSCAPE_ROTATED -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            else->ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        getAnimationFromDegrees(when (stateTo) {
            ORIENTATION_LANDSCAPE_ROTATED -> 90
            ORIENTATION_PORTRAIT -> 0
            ORIENTATION_LANDSCAPE -> -90
            else -> 0
        }).start()
    }
    override fun onClick(v: View) {
        if(fragment.fullscreen) when(orientationState){
            ORIENTATION_AUTO -> changeOrientation(ORIENTATION_LANDSCAPE)
            ORIENTATION_LANDSCAPE -> changeOrientation(ORIENTATION_PORTRAIT)
            ORIENTATION_PORTRAIT -> changeOrientation(ORIENTATION_LANDSCAPE_ROTATED)
            ORIENTATION_LANDSCAPE_ROTATED -> changeOrientation()
        }
        else{
            enterFullscreen(v)
            changeOrientation()
        }
//        if (!blocked && fragment.FULLSCREEN) { //landscape
//            blocked = true
//            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//            getAnimationFromDegrees(0).start()
//        } else if (!portrait && fragment.FULLSCREEN) { //portrait
//            portrait = true
//            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//            getAnimationFromDegrees(90).start()
//        } else { //sensor
//            if(v!=null&&!fragment.FULLSCREEN){
//                (fragment.requireActivity() as MyApp).notificator.showEnterFullscreen(v){
//                    fragment.fullscreenClick()
//                }
//            }
//        }
    }

    private fun getAnimationFromDegrees(degrees: Int): ViewPropertyAnimator {
        animation?.cancel()
        return fragment.controls.viewBinding.blockOrientationButton.animate()
                .rotation(degrees.toFloat())
                .setDuration(Config.VIDEO_CONTROLS_ROTATION_ANIMATION_DURATION).also { animation = it }
    }
    override fun onLongClick(v: View): Boolean {
        if(fragment.fullscreen)
            MyPopupMenu(ContextThemeWrapper(fragment.requireContext(), R.style.DarkPopupMenuStyle), v.parent as View,Gravity.TOP).apply {
            setShowIcon(true)
            menuInflater.inflate(R.menu.orientation_menu, this.menu)
            with(menu.findItem(when(orientationState){
                ORIENTATION_LANDSCAPE->R.id.menuOrientationLandscape
                ORIENTATION_LANDSCAPE_ROTATED->R.id.menuOrientationLandscapeRotated
                ORIENTATION_PORTRAIT->R.id.menuOrientationPortrait
                else->R.id.menuOrientationAuto
            })){
                title = SpannableString(title).apply { setSpan(StyleSpan(Typeface.BOLD),0,title!!.length,0) }
            }
            setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.menuOrientationLandscape -> changeOrientation(ORIENTATION_LANDSCAPE)
                    R.id.menuOrientationLandscapeRotated -> changeOrientation(ORIENTATION_LANDSCAPE_ROTATED)
                    R.id.menuOrientationPortrait -> changeOrientation(ORIENTATION_PORTRAIT)
                    R.id.menuOrientationAuto -> changeOrientation(ORIENTATION_AUTO)
                }
                true
            }
            show()
        }
        else {
            enterFullscreen(v)
            changeOrientation()
        }
        return true
    }
    private fun enterFullscreen(v:View)=(fragment.activity as MyApp).notificator.showEnterFullscreen(v){fragment.fullscreenClick()}
}