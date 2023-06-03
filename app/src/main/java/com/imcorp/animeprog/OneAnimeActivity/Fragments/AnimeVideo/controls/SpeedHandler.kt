package com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeVideo.controls

import android.os.Build
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.RequiresApi
import androidx.core.text.bold
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeVideo.FragmentVideoPlayer
import com.imcorp.animeprog.R
import com.imcorp.animeprog.databinding.BottomSpeedMenuBinding
//import kotlinx.android.synthetic.main.bottom_speed_menu.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

@RequiresApi(Build.VERSION_CODES.M)
class SpeedHandler(private val fragment: FragmentVideoPlayer,val theme:Int=R.style.BottomSheetMenu) :
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
    fun setEvents(): SpeedHandler =binder.run{
        val progress = fragment.videoView.speed
        imageIcon.setImageResource(R.drawable.ic_speed)
        with(speedProgressBar){
            max = Config.MAX_SPEED_PROGRESS_BAR_VALUE
            secondaryProgress = Config.DEFAULT_SPEED_PROGRESS_BAR_VALUE
            post { this.progress = getProgressFromFloat(progress) }
            //indeterminateDrawable.setColorFilter(fragment.resources.getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY)
            setOnSeekBarChangeListener(onSeek)
        }
        addMoreProgress.setOnClickListener {seekSpeed(true)}
        addLessProgress.setOnClickListener {seekSpeed(false)}
        restoreSpeed.setOnClickListener { setSpeed(1f) }
        speedTextView.text = getProgressString(progress)
        return this@SpeedHandler
    }
    private fun seekSpeed(forward:Boolean){
        fun roundN(num:Float):Float{
            var intNum:Int = (num * 100).roundToInt()
            val round = intNum%5
            if(round<3) intNum -= round
            else intNum += (5-round)
            return intNum/100f
        }
        val speed = roundN(fragment.videoView.speed) + if(forward) .05f else -.05f
        setSpeed(min(max(.1f,speed),4f))
    }
    private fun setSpeed(speed:Float) =with(binder){
        speedProgressBar.progress = getProgressFromFloat(speed)
        fragment.videoView.speed = speed
    }
    private fun getProgressFromFloat(y: Float): Int {
        return if (y < 1) {
            (y * Config.DEFAULT_SPEED_PROGRESS_BAR_VALUE).roundToInt()
        } else {
            (sqrt(y) * Config.DEFAULT_SPEED_PROGRESS_BAR_VALUE).roundToInt()
        }
    }
    private fun getFloatFromProgress(x: Int): Float {
        val proc = x.toFloat() / Config.DEFAULT_SPEED_PROGRESS_BAR_VALUE
        return if (proc < 1) {
            proc
        } else {
            proc * proc
        }
    }
    private fun getProgressString(progressFloat:Float) : CharSequence = SpannableStringBuilder(
            String.format("%.2f", progressFloat).replace('.',',')
    ).bold{append("x")}
    private val onSeek: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(_s: SeekBar, progress: Int, fromUser: Boolean) = with(binder){
            speedTextView.text = getProgressString(getFloatFromProgress(progress))
        }
        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        @RequiresApi(api = Build.VERSION_CODES.M)
        override fun onStopTrackingTouch(seekBar: SeekBar) {
            fragment.videoView.speed = getFloatFromProgress(seekBar.progress)
        }
    }

}