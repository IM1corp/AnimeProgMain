package com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeVideo

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.RelativeLayout.*
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.ExoPlaybackException
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.DB.local.Progress
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.Default.NotSwipeableViewPager
import com.imcorp.animeprog.Default.OnBackPressed
import com.imcorp.animeprog.Default.VideoTest
import com.imcorp.animeprog.OneAnimeActivity.OneAnimeActivity
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException
import com.imcorp.animeprog.Requests.Http.JummyAnimeAdapter
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEpisodeQuality
import com.imcorp.animeprog.Requests.JsonObj.Video.Subtitle
import com.imcorp.animeprog.databinding.FragmentOneAnimeDataBinding
import com.imcorp.animeprog.databinding.FragmentVideoPlayerBinding
import java.util.*


class FragmentVideoPlayer : Fragment(), OnBackPressed {

    //private var progressBar: ProgressBar? = null
    lateinit var videoView: VideoTest
    private var backCallback: OnBackPressed? = null
    lateinit var controls: VideoPlayerControls
    lateinit var videoFragment: FragmentOneAnimeVideo

    var fullscreen = false
    private var firstTime = true
    var selectedQuality = 0
    private var videoQualities: ArrayList<OneVideoEpisodeQuality>? = null
    var subtitles: ArrayList<Subtitle>? = null
    lateinit var headers: Map<String, String>
    private var webViewShown: WebView? = null
    val videoNotification: VideoNotification by lazy { VideoNotification(this) }

    private var _viewBinding: FragmentVideoPlayerBinding? = null
    public val viewBinding: FragmentVideoPlayerBinding get() = _viewBinding!!
    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        controls = VideoPlayerControls()
        _viewBinding = FragmentVideoPlayerBinding.inflate(inflater, container, false)
        //progressBar = view.findViewById(R.id.progressBar)
        videoView = viewBinding.videoView
        videoFragment = parentFragment as FragmentOneAnimeVideo
        viewBinding.root.isSoundEffectsEnabled = false
        return viewBinding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        videoView.setOnErrorListener { _, error ->
            val error_msg: Int = when (error.errorCode) {

                ExoPlaybackException.ERROR_CODE_REMOTE_ERROR, ExoPlaybackException.ERROR_CODE_UNSPECIFIED, ExoPlaybackException.ERROR_CODE_TIMEOUT,
                ExoPlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT, ExoPlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED-> {
                    controls.posSeekTo = videoView.currentPosition
                    R.string.no_internet
                }
                ExoPlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS, ExoPlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ->{
                    R.string.not_found
                }
                ExoPlaybackException.ERROR_CODE_DECODING_FAILED-> {
                    R.string.undefined_error
                }
                ExoPlaybackException.TYPE_REMOTE, ExoPlaybackException.TYPE_RENDERER -> {
                    R.string.undefined_error
                }
                else -> R.string.undefined_error
            }
            Toast.makeText(context, error_msg, Toast.LENGTH_SHORT).show()
        }
                .setLoadingListener { isLoading: Boolean -> }
                .setOnVideoSizeChanged { _, _-> screenOrientationChanged(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) }
                .setOnPreparedListener {
                    setLoading(false)
                    videoView.requestFocus()
                    controls.onVideoReady(videoView)
                }
        show(false)
        setFragmentBackOnclick()
        screenOrientationChanged(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)

        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment, controls).commit()
        videoView.setOnIsPlayingListener { isPlaying: Boolean -> videoNotification.update(isPlaying = isPlaying) }
        if (!this.requireView().hasOnClickListeners()) requireView().setOnTouchListener(controls.onVideoViewClick)
    }
    //region public methods
    fun show(show: Boolean) {
        requireView().visibility = if (show) View.VISIBLE else View.GONE
    }

    fun setLoading(loading: Boolean) {
        if (requireView().visibility != View.VISIBLE) {
            show(true)
        }
        if (loading) with(videoView){
            stopPlayback()
            if (isPlaying) pause()
        }
        viewBinding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        //this.videoView.setVisibility(loading?View.GONE:View.VISIBLE);
    }

    @Throws(InvalidHtmlFormatException::class)
    fun loadVideo(anime: OneAnime, episode: OneVideo) {
        videoNotification.update(anime = anime, episode = episode)
        updateProgress(anime, episode)
        videoQualities = episode.videoQualities
        subtitles = episode.videoSubtitles
        setVideoUrl(0) //TODO: set null
        if (!firstTime) controls.posSeekTo = 0
        firstTime = false
        controls.setData(anime, episode)
    }

    private fun updateProgress(anime: OneAnime, episode: OneVideo) {
        val activity = activity as MyApp? ?: return
        val progress = anime.getProgress(activity.dataBase).addNum(episode.num)
        val videosSize = anime.videos.size
        progress.progress = if (videosSize != 0) progress.numSize() / videosSize.toFloat() else 0f
        progress.watchState = if (episode.num == anime.videos[videosSize - 1].num) Progress.WatchState.ENDED else Progress.WatchState.WATCHING
        progress.update(activity.dataBase)
    }

    @Throws(InvalidHtmlFormatException::class)
    fun setVideoUrl(index: Int) {
        if (videoQualities == null || videoQualities!!.size == 0) return  //TODO: show error
        selectedQuality = index
        if (controls.posSeekTo == 0) controls.posSeekTo = videoView.currentPosition
        val ep = videoQualities!![index]
        this.headers = ep.ref?.let{hashMapOf(Pair("Referer", it))}?: hashMapOf()
        val url: String = when {
            ep.m3u8Url != null -> ep.m3u8Url
            ep.downloadUrl != null -> ep.downloadUrl
            ep.mp4Url != null -> ep.mp4Url
            else -> throw InvalidHtmlFormatException(InvalidHtmlFormatException.getHtmlError("Url not found!"))
        }
        videoView.setVideoURI(Uri.parse(url), headers, subtitles)
    }

    //endregion
    private fun setFragmentBackOnclick() {
        (requireActivity() as MyApp).backCallbacks.add(OnBackPressed {
            if (fullscreen) {
                fullscreenClick()
                true
            } else false
        }.also { backCallback = it })
    }
    fun fullscreenClick() {
        val activity = requireActivity() as OneAnimeActivity
        fun hideSystemUI() {
            val g = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) View.SYSTEM_UI_FLAG_IMMERSIVE else 0
            activity.window.decorView.systemUiVisibility = (g
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    // Hide the nav bar and status bar
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        }
        fun showSystemUI() {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_VISIBLE)
        }

        if (!fullscreen) {
            hideSystemUI()
            activity.binding.appBarLayout.setExpanded(true, false)
            activity.binding.tabLayout.visibility = View.GONE
            activity.supportActionBar?.hide()
            activity.binding.viewPager.setSwipeable(false)
            videoFragment.scrollView.setScrollable(false)
        }
        else {
            showSystemUI()
            activity.supportActionBar?.show()
            (activity.findViewById<View>(R.id.viewPager) as NotSwipeableViewPager).setSwipeable(true)
            activity.findViewById<View>(R.id.tabLayout).visibility = View.VISIBLE
            videoFragment.scrollView.setScrollable(true)
        }
        fullscreen = !fullscreen
        screenOrientationChanged(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
        controls.onFullScreenChanged(fullscreen)
    }
    override fun onBackPressed(): Boolean {
        if (fullscreen) {
            fullscreenClick()
            return true
        }
        return false
    }
    override fun onDestroy() {
        val act = activity as MyApp?
        if (backCallback != null && act != null) act.backCallbacks.remove(backCallback)
        videoView.stopPlayback()
        videoView.release()
        videoNotification.hideNotification()
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        screenOrientationChanged(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
        videoFragment.scrollView.scrollToTop()
    }

    private fun screenOrientationChanged(portrait: Boolean) {
        val metrics = windowSize
        val videoParams = videoView.layoutParams
        val parentParams = if (requireView().layoutParams != null) requireView().layoutParams else ConstraintLayout.LayoutParams(ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        val videoWidth = videoView.videoWidth.toFloat()
        val videoHeight = videoView.videoHeight.toFloat()
        if (portrait && fullscreen) {
            if (videoWidth > videoHeight) {
                videoParams.width = metrics.widthPixels
                videoParams.height = (metrics.widthPixels * videoHeight / videoWidth).toInt()
            }
            parentParams.height = metrics.heightPixels
            videoFragment.scrollView.scrollToTop()
        }
        else if (portrait && !fullscreen) {
            if (videoWidth > videoHeight) {
                videoParams.width = metrics.widthPixels
                videoParams.height = (metrics.widthPixels * videoHeight / videoWidth).toInt()
            }
            parentParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        else if (!portrait && fullscreen) {
            val dimScreen = metrics.heightPixels.toFloat() / metrics.widthPixels
            // width * dimScreen = height
            val dimVideo = videoHeight / videoWidth
            if (dimVideo < 1f) {
                if (dimScreen <= dimVideo) {
                    videoParams.height = metrics.heightPixels
                    videoParams.width = (metrics.heightPixels / dimVideo).toInt()
                } else {
                    videoParams.width = metrics.widthPixels
                    videoParams.height = (metrics.widthPixels * dimVideo).toInt()
                }
            }
            else {
                videoParams.height = metrics.heightPixels
                videoParams.width = (metrics.heightPixels / dimVideo).toInt()
            }
            parentParams.height = metrics.heightPixels
            videoFragment.scrollView.scrollToTop()
        }
        else if (!portrait && !fullscreen) {
            val dimScreen = metrics.heightPixels.toFloat() / metrics.widthPixels
            // width * dimScreen = height
            val dimVideo = videoHeight / videoWidth
            var height = 0f
            var width = 0f
            if (dimVideo < 1f) {
                if (dimScreen <= dimVideo) {
                    height = metrics.heightPixels.toFloat()
                    width = (metrics.heightPixels / dimScreen)
                } else {
                    width = metrics.widthPixels.toFloat()
                    height = (metrics.widthPixels * dimScreen)
                }
            }
            val videoScreenHeight = Config.dpToPix(requireContext(), 150)
            width *= videoScreenHeight / height
            height = videoScreenHeight.toFloat()
            videoParams.width = width.toInt()
            videoParams.height = height.toInt()
            parentParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        videoView.layoutParams = videoParams
    }

    fun showVideoView(url: String? = null, headers: Map<String, String>? = null) {
        url?.run{
            if(webViewShown==null){
                @SuppressLint("SetJavaScriptEnabled")
                webViewShown = WebView(requireContext()).apply {
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = false
                    settings.setSupportZoom(false)
                    settings.javaScriptEnabled = true
                    settings.userAgentString = JummyAnimeAdapter.getRandomUserAgent().name
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                            // Here put your code
                            Log.d("My Webview", url)
                            // return true; //Indicates WebView to NOT load the url;
                            return false //Allow WebView to load url
                        }
                    }
                    setBackgroundColor(Color.TRANSPARENT)
                    loadUrl(url, headers!!)
                }
                val dialog = Dialog(requireActivity(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                dialog.setOnDismissListener {
                    webViewShown?.destroy()
                    webViewShown = null
                }
                dialog.addContentView(webViewShown!!, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
                dialog.show()
            }
        }?:{
            (webViewShown?.parent as Dialog).dismiss()
            //webViewShown?.destroy()
            //webViewShown = null
        }()
    }

    val windowSize: DisplayMetrics get() {
            fun getNavigationBarHeight(): Int {
                val display = activity?.windowManager?.defaultDisplay
                return if (display == null) {
                    0
                } else {
                    val realMetrics = DisplayMetrics()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        display.getRealMetrics(realMetrics)
                    }
                    val metrics = DisplayMetrics()
                    display.getMetrics(metrics)
                    realMetrics.heightPixels - metrics.heightPixels
                }
            }

        val metrics = DisplayMetrics()
        if (activity == null) return metrics
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            requireActivity().windowManager.defaultDisplay.getRealMetrics(metrics)
        }

        //metrics.heightPixels+=getNavigationBarHeight()
        return metrics
    }
}