package com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeVideo.controls

import android.os.Build
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.core.text.bold
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeVideo.FragmentVideoPlayer
import com.imcorp.animeprog.R
import com.imcorp.animeprog.databinding.BottomSpeedMenuBinding
//import kotlinx.android.synthetic.main.bottom_speed_menu.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class VolumeHandler(private val fragment: FragmentVideoPlayer, val theme:Int= R.style.BottomSheetMenu) :
        BottomSheetDialog(fragment.requireContext(),theme){
    val binder = BottomSpeedMenuBinding.inflate(LayoutInflater.from(context))

    init{
//        val view = LayoutInflater.from(context).inflate(R.layout.bottom_speed_menu,speedMenu)
        setContentView(binder.root)
        this.dismissWithAnimation=true
    }

    override fun show() {
        super.show()
        BottomSheetBehavior.from(
                this.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)!!
        ).state = BottomSheetBehavior.STATE_EXPANDED
    }
    fun setEvents(): VolumeHandler = (binder).run{
        imageIcon.setImageResource(R.drawable.ic_volume)
        title.setText(R.string.volume)
        restoreSpeed.visibility=View.GONE
        val progress = fragment.videoView.currentVolume
        with(speedProgressBar){
            max = if(fragment.videoView.isMoreThan100VolumeSupported) 200 else 100
            setOnSeekBarChangeListener(onSeek)
            post{ setProgress((progress*100).toInt()) }
        }
        addMoreProgress.setOnClickListener {seekVolume(true)}
        addLessProgress.setOnClickListener {seekVolume(false)}
        //restoreSpeed.setOnClickListener { setSpeed(1f) }
        speedTextView.text = getProgressString(progress)
        return this@VolumeHandler
    }
    private fun seekVolume(forward:Boolean){
        fun formatVol(volume: Float):Float= round(volume*20) / 20f
        setVolume(formatVol(fragment.videoView.currentVolume+
                if(forward).05f else-.05f))
    }
    private fun setVolume(volume:Float)=with(binder){
        val vol:Float = if(volume>2f)2f else if(volume<0f)0f else volume
        speedProgressBar.progress = round( vol*100).toInt()
        fragment.videoView.setVolume( vol )
    }
    private fun getProgressString(progressFloat:Float) : CharSequence = SpannableStringBuilder(
        round(progressFloat*100f).toInt().toString()
    ).bold{append("%")}
    private val onSeek: SeekBar.OnSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(_s: SeekBar, progress: Int, fromUser: Boolean) = with(binder){
            speedTextView.text = getProgressString(progress/100f)
            fragment.videoView.setVolume(progress/100f)
        }
        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        @RequiresApi(api = Build.VERSION_CODES.M)
        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    }

}