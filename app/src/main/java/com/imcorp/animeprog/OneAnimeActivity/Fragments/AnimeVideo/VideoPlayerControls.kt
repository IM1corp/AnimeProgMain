package com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeVideo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.*
import android.view.View.OnTouchListener
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.Default.MyApp.OnTouch
import com.imcorp.animeprog.Default.MyPopupMenu
import com.imcorp.animeprog.Default.VideoTest
import com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeVideo.controls.*
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo
import com.imcorp.animeprog.databinding.FragmentVideoPlayerBinding
import com.imcorp.animeprog.databinding.FragmentVideoPlayerControlsBinding
import kotlin.math.max
import kotlin.math.min

class VideoPlayerControls : Fragment(R.layout.fragment_video_player_controls) {
    //region create
    //region vars
    val fragment: FragmentVideoPlayer get()=(parentFragment as FragmentVideoPlayer)
    private lateinit var events: CancelScrollEvents
    private val hideRunnable = Runnable {
        if(isAdded) {
            viewBinding.topView.visibility = View.INVISIBLE
            viewBinding.bottomView.visibility = View.INVISIBLE
            stopLoop()
            showing = false
        }
    }
    public var posSeekTo = 0
    public var subtitleIndex = -1
    private var showing = false
    public var episode: OneVideo? = null
    private var _viewBinding: FragmentVideoPlayerControlsBinding? = null
    public val viewBinding: FragmentVideoPlayerControlsBinding get() = _viewBinding!!

    //endregion
    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentVideoPlayerControlsBinding.inflate(inflater, container, false).also{_viewBinding=it}.root
    override fun onSaveInstanceState(bundle: Bundle) {
        val pos = fragment.videoView.currentPosition
        bundle.putInt(Config.FRAGMENT_CONTROLS_POS, if (pos != -1) pos else 0)
        super.onSaveInstanceState(bundle)
    }
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        if (bundle != null) {
            posSeekTo = bundle.getInt(Config.FRAGMENT_CONTROLS_POS)
        }
    }
    //override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_video_player_controls, container, false)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setDataUI()
        setCancelScrollEvents()
        viewBinding.previosEpisodeButton.visibility = View.VISIBLE
        viewBinding.nextEpisodeButton.visibility = View.VISIBLE
        viewBinding.closeButton.visibility = View.INVISIBLE
        fragment.videoView.subtitle = viewBinding.subtitlesView

    }
    private fun setDataUI() {
        fragment.apply {
            this@VideoPlayerControls.viewBinding.fullScreenButton.setOnClickListener {
                fullscreenClick()
                updateHiddenTimeout()
            }
            this@VideoPlayerControls.viewBinding.closeButton.setOnClickListener {
                if (fullscreen) this@VideoPlayerControls.viewBinding.fullScreenButton.callOnClick()
                videoView.pause()
                show(false)
                fragment.videoNotification.hideNotification()
            }
            this@VideoPlayerControls.viewBinding.playPauseButton.setOnClickListener {
                stopLoop()
                startLoop(true)
                if (!videoView.isPlaying) videoView.start()
                else if (videoView.canPause()) videoView.pause()
                updateHiddenTimeout()
            }
        }
        with(viewBinding.blockOrientationButton) {
            setOnClickListener(orientationButtonEventsHandler)
            setOnLongClickListener(orientationButtonEventsHandler)
        }
        viewBinding.actionButton.setOnClickListener{view->
            episode?.videoQualities?.let {
                VideoQualitiesMenu(view, videoQualities = it, fragment = fragment).apply {
                    setOnDismissListener { updateHiddenTimeout() }
                }.show()
            }
        }
        viewBinding.menuButton.setOnClickListener(onOtherActionsClick)
        viewBinding.nextEpisodeButton.setOnClickListener { fragment.videoFragment.nextVideoButtonClick() }
        viewBinding.previosEpisodeButton.setOnClickListener { fragment.videoFragment.backVideoButtonClick() }
    }
    //endregion
    //region startLoop
    fun updateUI(position: Int) =with(viewBinding){
        var pos = position
        if (pos == -1) pos = fragment.videoView.currentPosition
        if(progressBar.max < pos) progressBar.max = fragment.videoView.duration
        progressBar.progress = pos
        currentPos.text = Config.getPosString(pos)
        playPauseButton.setImageResource(if (fragment.videoView.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
    }

    private val updateUILoopRunnable = Runnable{
        if(this.isAdded) {
            updateUI(-1)
            startLoop(false)
        }
    }
    private var loopStarted = false
    private fun startLoop(start: Boolean) {
        if (!loopStarted || !start) {
            loopStarted = true
            fragment.videoView?.run {
                val dealay = 1000 - fragment.videoView.currentPosition % 1000
                (activity as MyApp).threadCallback.postDelayed(updateUILoopRunnable, if (start) 0 else dealay.toLong())
            }
        } else updateUILoopRunnable.run()
    }
    private fun stopLoop() {
        loopStarted = false
        (activity as MyApp).threadCallback.removeCallbacks(updateUILoopRunnable)
    }
    //endregion
    //region menu
    private fun deleteHideTimeout(runRunnable: Boolean) {
        (activity as MyApp).threadCallback.removeCallbacks(hideRunnable)
        if (runRunnable) hideRunnable.run()
    }
    private fun updateHiddenTimeout() = with(viewBinding){
        //TODO: Убрать логи
        deleteHideTimeout(false)
        showing = true
        startLoop(true)
        topView.visibility = View.VISIBLE
        bottomView.visibility = View.VISIBLE
        //this.view.setVisibility(View.VISIBLE);
        (activity as MyApp).threadCallback.postDelayed(hideRunnable, Config.VIDEO_CONTROLS_WAIT_TIME.toLong())
    }
    private fun hide() = deleteHideTimeout(true)
    //endregion
    //region cancelScroll
    private fun setCancelScrollEvents() =with(viewBinding){
        events = CancelScrollEvents()
        progressBar!!.setOnSeekBarChangeListener(events)
        (activity as MyApp).touchCallbacks.add(events)
    }
    private inner class CancelScrollEvents : OnSeekBarChangeListener, OnTouch {
        /* Add top TextView on hover event -> disables seek */
        private var cancel = false
        private var scrolling = false
        private var def_color = 0
        override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean)=with(viewBinding) {
            if (b) {
                progressBar!!.progress = i
                currentPos!!.text = Config.getPosString(i)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar)=with(viewBinding) {
            deleteHideTimeout(false)
            def_color = cancelSeekTextView!!.textColors.defaultColor
            scrolling = true
            stopLoop()
            topView!!.visibility = View.GONE
            cancelSeek!!.visibility = View.VISIBLE
        }

        override fun onStopTrackingTouch(seekBar: SeekBar): Unit =with(viewBinding) {
            if (!cancel) {
                fragment.videoView.seekTo(seekBar.progress)
            }
            cancelSeek.visibility = View.INVISIBLE
            topView.visibility = View.VISIBLE
            cancelSeekTextView.setTextColor(def_color)
            scrolling = false
            cancel = scrolling
            updateHiddenTimeout()
        }

        private val out = IntArray(2)
        override fun onTouch(event: MotionEvent)=with(viewBinding) {
            if (scrolling) {
                if (event.action == MotionEvent.ACTION_MOVE) {
                    cancelSeekTextView!!.getLocationOnScreen(out)
                    val y = event.y
                    val height = cancelSeekTextView!!.height.toFloat()
                    if (cancel != (y > out[1] && y < out[1] + height).also { cancel = it }) {
                        cancelSeekTextView!!.setTextColor(if (cancel) Color.WHITE else def_color)
                        //TODO: Если произошла смена over/mouseout убрать подсведку и т.п.
                    }
                }
            }
        }
    }
    override fun onDestroy() {
        (activity as MyApp).touchCallbacks.remove(events)
        fragment.videoView.setOnBufferingUpdateListener(null)
        super.onDestroy()
    }

    //endregion
    val onVideoViewClick: OnTouchListener = object : OnTouchListener {
        private val showNextImageForward = Runnable { viewBinding.forwardSeekImageView!!.setImageResource(R.drawable.ic_forward_control_2) }
        private val showNextImageBackwards = Runnable { viewBinding.backwardsSeekImageView!!.setImageResource(R.drawable.ic_backwards_control_2) }
        private var needToStartOnEnd = false
        private var lastTimeClicked: Long = -1
        private var forward_click_count: Short = 0
        private var back_click_count: Short = 0

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            if (event.action != MotionEvent.ACTION_DOWN) return false
            if (-(lastTimeClicked - System.currentTimeMillis().also { lastTimeClicked = it }) < Config.VIDEO_CONTROLS_DOUBLE_CLICK_MAX_TIME) {
                (activity as MyApp).threadCallback.removeCallbacks(::callbackOnClick)
                onDoubleClick(event)
            } else {
                (activity as MyApp).threadCallback.postDelayed(::callbackOnClick, Config.VIDEO_CONTROLS_DOUBLE_CLICK_MAX_TIME.toLong())
            }
            return false
        }

        private fun onBackClick(seekTime: Int, animate: Boolean) {
            if (fragment.videoView.canSeekBackward()) {
                val startPos = max(fragment.videoView.currentPosition - seekTime, 0)
                if (animate) updateUI(startPos)
                else fragment.videoView.seekTo(startPos)
            }
        }
        private fun onForwardClick(seekTime: Int, animate: Boolean) {
            if (fragment.videoView.canSeekForward()) {
                var startPos = max(fragment.videoView.currentPosition, 0) + seekTime
                startPos = min(fragment.videoView.duration - 1, startPos)
                if (animate) updateUI(startPos)
                else fragment.videoView.seekTo(startPos)
            }
        }
        private fun onDoubleClick(event: MotionEvent) {
            val metrics = fragment.windowSize
            val x = event.x.toDouble()
            val width_success = metrics.widthPixels * Config.VIDEO_CONTROLS_DOUBLE_CLICK_NEXT_EP_AREA
            if (x < width_success) {
                back_click_count++
                viewBinding.backwardsSeekTextView!!.text = (back_click_count * Config.VIDEO_CONTROLS_SEEK_TIME / 1000).toString()
                updateTimer(false)
            } else if (x > metrics.widthPixels - width_success) {
                forward_click_count++
                viewBinding.forwardSeekTextView!!.text = (forward_click_count * Config.VIDEO_CONTROLS_SEEK_TIME / 1000).toString()
                updateTimer(true)
            } else viewBinding.playPauseButton!!.callOnClick()
        }
        private fun updateTimer(forward: Boolean) =with((activity as MyApp).threadCallback){
            if (showing) {
                deleteHideTimeout(false)
                stopLoop()
            }
            if (forward) {
                if (back_click_count.toInt() != 0) {
                    removeCallbacks(callbackSeekBackward)
                    callbackSeekBackward.run()
                    animate(forward = false)
                }
                removeCallbacks(callbackSeekForward)
                postDelayed(callbackSeekForward, Config.VIDEO_CONTROLS_DOUBLE_CLICK_WAIT_TIME.toLong())
                onForwardClick(Config.VIDEO_CONTROLS_SEEK_TIME * forward_click_count, true)
            }
            else {
                if (forward_click_count.toInt() != 0) {
                    removeCallbacks(callbackSeekForward)
                    callbackSeekForward.run()
                    animate()
                }
                removeCallbacks(callbackSeekBackward)
                postDelayed(callbackSeekBackward, Config.VIDEO_CONTROLS_DOUBLE_CLICK_WAIT_TIME.toLong())
                onBackClick(Config.VIDEO_CONTROLS_SEEK_TIME * back_click_count, true)
            }
            animate(true, forward)
            if (fragment.videoView.isPlaying) fragment.videoView.pause()
        }
        private fun animate(start: Boolean = false, forward: Boolean = true) = with((activity as MyApp).threadCallback){
            with(viewBinding) {
                removeCallbacks(showNextImageBackwards)
                removeCallbacks(showNextImageForward)
                if (start) {
                    if (forward) {
                        if (forward_click_count.toInt() == 1) needToStartOnEnd =
                            fragment.videoView.isPlaying
                        forwardSeek!!.visibility = View.VISIBLE
                        forwardSeekImageView!!.setImageResource(R.drawable.ic_forward_control)
                        postDelayed(
                            showNextImageForward,
                            Config.VIDEO_CONTROLS_DOUBLE_CLICK_WAIT_TIME / 2.toLong()
                        )
                    } else {
                        if (back_click_count.toInt() == 1) needToStartOnEnd =
                            fragment.videoView.isPlaying
                        backwardsSeek!!.visibility = View.VISIBLE
                        backwardsSeekImageView!!.setImageResource(R.drawable.ic_backwards_control)
                        postDelayed(
                            showNextImageBackwards,
                            Config.VIDEO_CONTROLS_DOUBLE_CLICK_WAIT_TIME / 2.toLong()
                        )
                    }
                } else {
                    if (forward) forwardSeek.visibility = View.INVISIBLE
                    else backwardsSeek.visibility = View.INVISIBLE
                }
            }
        }

        private fun callbackOnClick() = if (!showing) updateHiddenTimeout() else hide()
        private val callbackSeekForward= Runnable {
            onForwardClick(Config.VIDEO_CONTROLS_SEEK_TIME * forward_click_count, false)
            forward_click_count = 0
            animate()
            if (needToStartOnEnd && !false.also { needToStartOnEnd = it }) fragment.videoView.start()
            if (showing) updateHiddenTimeout()
        }
        private val callbackSeekBackward = Runnable {
            onBackClick(Config.VIDEO_CONTROLS_SEEK_TIME * back_click_count, false)
            back_click_count = 0
            animate(forward = false)
            if (needToStartOnEnd && !false.also { needToStartOnEnd = it }) fragment.videoView.start()
            if (showing) updateHiddenTimeout()
        }

    }
    fun onVideoReady(mediaPlayer: VideoTest)=with(viewBinding) {
        updateUI(-1)
        val duration = fragment.videoView.duration
        progressBar.max = duration
        maxPos.text = Config.getPosString(duration)
        mediaPlayer.setOnBufferingUpdateListener { _, position -> progressBar.secondaryProgress = position }
    }
    fun onFullScreenChanged(isFullscreen: Boolean)=with(viewBinding) {
        fullScreenButton.setImageResource(if (isFullscreen) R.drawable.ic_fullscreen_exit else R.drawable.ic_fullscreen)

        if (isFullscreen) {
            previosEpisodeButton.visibility = View.VISIBLE
            nextEpisodeButton.visibility = View.VISIBLE
            closeButton.visibility = View.INVISIBLE
        }
        else {
            previosEpisodeButton.visibility = View.INVISIBLE
            nextEpisodeButton.visibility = View.INVISIBLE
            closeButton.visibility = View.VISIBLE
            orientationButtonEventsHandler.changeOrientation()
        }
    }
    fun setData(anime: OneAnime, episode: OneVideo) {
        this.episode = episode
        val builder = SpannableStringBuilder()
        builder.append(episode.num)
                .append(' ')
                .append(getString(R.string.episodes))
        builder.setSpan(StyleSpan(Typeface.BOLD), 0, builder.length - 1, 0)
        builder.append(" - ")
                .append(anime.title)
        viewBinding.titleTextView!!.text = builder.toString()
        subtitleIndex=-1
    }

    //region buttonOnClicks
    private val orientationButtonEventsHandler by lazy { OrientationHandler(this.fragment) }
    private val onOtherActionsClick: View.OnClickListener = object : View.OnClickListener {
        private val onClose = MyPopupMenu.OnDismissListener { updateHiddenTimeout() }
        override fun onClick(v: View) {
            deleteHideTimeout(false)
            val wrapper: Context = ContextThemeWrapper(v.context, R.style.DarkPopupMenuStyle)
            val mypopupmenu = MyPopupMenu(wrapper, v, Gravity.TOP).apply {
                setOnDismissListener(onClose)
                setShowIcon(true)
                menuInflater.inflate(R.menu.controls_popup_menu, menu)
                show()
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) menu.removeItem(R.id.menuSpeed)
                if (episode?.downloaded!=true) menu.removeItem(R.id.menuDelete)
                else menu.removeItem(R.id.menuShowWebView)
                if(fragment.subtitles?.isEmpty()!=false) menu.removeItem(R.id.menuSubtitle)
            }
            //          mypopupmenu.getMenu().getItem(0).setIcon(getResources().getDrawable(R.mipmap.ic_launcher));
            mypopupmenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menuSpeed -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) SpeedHandler(fragment).setEvents().show()
                    R.id.menuDelete -> DeleteOneVideoHandler(this@VideoPlayerControls){
                        fragment.videoView.stopPlayback()
                        fragment.show(false)
                    }.showDialog()
                    R.id.menuVolume -> VolumeHandler(fragment).setEvents().show()
                    R.id.menuShowWebView -> episode?.run{
                        fragment.showVideoView(urlFrame, fragment.headers)
                    }
                    R.id.menuSubtitle -> {
                        SelectSubtitleBottomMenu(this@VideoPlayerControls){pos->
                            if(pos == -1) fragment.videoView.disableSubtitles()
                            else fragment.videoView.setSubtitle(fragment.subtitles!![pos])
                        }.setEvents().show()
                    }
                }
                false
            }
        }
    }
    //endregion
}