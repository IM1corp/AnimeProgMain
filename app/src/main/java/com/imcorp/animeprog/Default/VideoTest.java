package com.imcorp.animeprog.Default;

import static com.imcorp.animeprog.Config.LOG_TAG;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.LoudnessEnhancer;
import android.net.Uri;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.CueGroup;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.video.VideoSize;
import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.Requests.Http.JummyAnimeAdapter;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo;
import com.imcorp.animeprog.Requests.JsonObj.Video.Subtitle;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
public class VideoTest extends VideoView {
    private MediaPlayer mp;
    public VideoTest(Context context) {
        super(context);
        init();
    }
    public VideoTest(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public VideoTest(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mps) {
                mp = mps;
            }
        });
        this.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }
            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                pause();
            }
        });
    }
    @Override
    public void pause(){
        if(this.mp!=null)
            this.mp.pause();
        super.pause();
    }
    @Override
    public void start(){
        if(this.mp!=null)
            this.mp.start();
        else super.start();
    }
    @Override
    public void stopPlayback(){
        super.pause();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);//View.VISIBLE);
    }

}*/

/*public class VideoTest extends SurfaceView implements MediaPlayerControl {
    private final String TAG = "VideoView";
    // settable by the client
    private Uri         mUri;
    private Map<String, String> mHeaders;
    private int         mDuration;

    // all possible internal states
    private static final int STATE_ERROR              = -1;
    private static final int STATE_IDLE               = 0;
    private static final int STATE_PREPARING          = 1;
    private static final int STATE_PREPARED           = 2;
    private static final int STATE_PLAYING            = 3;
    private static final int STATE_PAUSED             = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int mCurrentState = STATE_IDLE;
    private int mTargetState  = STATE_IDLE;

    // All the stuff we need for playing and showing a video
    private SurfaceHolder mSurfaceHolder = null;
    private MediaPlayer mMediaPlayer = null;
    private int         mVideoWidth;
    private int         mVideoHeight;
    private int         mSurfaceWidth;
    private int         mCurrentLastPosition=0;
    private int         mSurfaceHeight;
    private MediaController mMediaController;
    private OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private int         mCurrentBufferPercentage;
    private OnErrorListener mOnErrorListener;
    private int         mSeekWhenPrepared;  // recording the seek position while preparing
    private boolean     mCanPause;
    private boolean     mCanSeekBack;
    private boolean     mCanSeekForward;
    private boolean     startOnPrepare=false;
    private Context mContext;

    private float mSpeedOnPrepared = -1f;
    private AudioManager audioManager;
    private LoudnessEnhancer volumeGain;

    public VideoTest(Context context) {
        super(context);
        this.mContext = context;
        initVideoView();
    }

    public VideoTest(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.mContext = context;
        initVideoView();
    }

    public VideoTest(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        initVideoView();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Log.i("@@@@", "onMeasure");
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            if ( mVideoWidth * height  > width * mVideoHeight ) {
                //Log.i("@@@", "image too tall, correcting");
                height = width * mVideoHeight / mVideoWidth;
            } else if ( mVideoWidth * height  < width * mVideoHeight ) {
                //Log.i("@@@", "image too wide, correcting");
                width = height * mVideoWidth / mVideoHeight;
            }
        }
        setMeasuredDimension(width, height);
    }
    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize =  MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:

                result = desiredSize;
                break;
            case MeasureSpec.AT_MOST:

                result = Math.min(desiredSize, specSize);
                break;
            case MeasureSpec.EXACTLY:
                // No choice. Do what we are told.
                result = specSize;
                break;
        }
        return result;
    }

    private void initVideoView() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        getHolder().addCallback(mSHCallback);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = STATE_IDLE;
        mTargetState  = STATE_IDLE;
        this.audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
    }

    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }


    public void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared =mCurrentLastPosition= 0;
        openVideo();
        requestLayout();
        invalidate();
    }
    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState  = STATE_IDLE;
        }
    }
    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) return;
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);

        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        //release(false);
        if(mMediaPlayer!=null&&isInPlaybackState())this.mMediaPlayer.stop();
        release(true);
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mDuration = -1;
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mCurrentBufferPercentage = 0;
            mMediaPlayer.setDataSource(mContext, mUri, mHeaders);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                this.volumeGain = new LoudnessEnhancer(mMediaPlayer.getAudioSessionId());
            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = STATE_PREPARING;
            attachMediaController();
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            if(mTargetState!=STATE_ERROR) {
                mTargetState = STATE_ERROR;
                mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            }
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }
    }

    public void setMediaController(MediaController controller) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController();
    }

    private void attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);

            View anchorView = this.getParent() instanceof View ? (View)this.getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(isInPlaybackState());
        }
    }

    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new MediaPlayer.OnVideoSizeChangedListener() {
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    mVideoWidth = mp.getVideoWidth();
                    mVideoHeight = mp.getVideoHeight();
                    if (mVideoWidth != 0 && mVideoHeight != 0) {
                        getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                    }
                }
            };

    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mCurrentState = STATE_PREPARED;

            mCanPause = mCanSeekBack = mCanSeekForward = true;
            if(startOnPrepare){
                startOnPrepare=false;
                start();
            }
            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }
            if (mMediaController != null) {
                mMediaController.setEnabled(true);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                setSpeed(mSpeedOnPrepared!=-1f?mSpeedOnPrepared:((MyApp)mContext).dataBase.settings.getSpeed());

            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            int seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }

            if (mVideoWidth != 0 && mVideoHeight != 0) {
                //Log.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                    // We didn't actually change the size (it was already at the size
                    // we need), so we won't get a "surface changed" callback, so
                    // start the video here instead of in the callback.
                    if (mTargetState == STATE_PLAYING) {
                        start();
                        if (mMediaController != null) {
                            mMediaController.show();
                        }
                    } else if (!isPlaying() &&
                            (seekToPosition != 0 || getCurrentPosition() > 0)) {
                        if (mMediaController != null) {
                            // Show the media controls when we're paused into a video and make 'em stick.
                            mMediaController.show(0);
                        }
                    }
                }
            } else {
                // We don't know the video size yet, but should start anyway.
                // The video size might be reported to us later.
                if (mTargetState == STATE_PLAYING) {
                    start();
                }
            }
        }
    };

    private MediaPlayer.OnCompletionListener mCompletionListener =
            new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mCurrentState = mTargetState = STATE_PLAYBACK_COMPLETED;
                    if (mMediaController != null) {
                        mMediaController.hide();
                    }
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mMediaPlayer);
                    }
                    //mMediaPlayer.reset();
                }
            };

    private final MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
                public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
                    Log.d(TAG, "Error: " + framework_err + "," + impl_err);
                    mCurrentState = STATE_ERROR;
                    mTargetState = STATE_ERROR;
                    if (mMediaController != null) mMediaController.hide();
                    else if (mOnErrorListener != null) {
                        if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                            return true;
                        }
                    }
                    if (getWindowToken() != null ) {
                        final int messageId= (framework_err ==
                                        MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) ?
                                        android.R.string.VideoView_error_text_invalid_progressive_playback:
                                android.R.string.VideoView_error_text_unknown;

                        ((MyApp)mContext).notificator.showMediaPlayerError(VideoTest.this,messageId,
                                _view->{
                                    if (mOnCompletionListener != null)
                                        mOnCompletionListener.onCompletion(mMediaPlayer);
                                    start();
                                },
                                ()->{}
                        );
                    }
                    return true;
                }
            };
    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new MediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    mCurrentBufferPercentage = percent;
                }
            };
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l)
    {
        mOnPreparedListener = l;
    }
    public void setOnCompletionListener(OnCompletionListener l)
    {
        mOnCompletionListener = l;
    }
    public void setOnErrorListener(OnErrorListener l)
    {
        mOnErrorListener = l;
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        private boolean videoOpened=false;

        public void surfaceChanged(SurfaceHolder holder, int format,
                                   int w, int h)
        {
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState =  (mTargetState == STATE_PLAYING);
            boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
        }

        public void surfaceCreated(SurfaceHolder holder)
        {
            final SurfaceHolder def_holder = mSurfaceHolder;
            mSurfaceHolder = holder;
            if(!videoOpened){
                openVideo();
                videoOpened=true;
            }else{
                if(mMediaPlayer!=null&&def_holder!=holder){

                    try {
                        mMediaPlayer.setSurface(holder.getSurface());
                    }catch (java.lang.IllegalStateException ignored) {
                        resume();
                    }
                }
            }

        }

        public void surfaceDestroyed(SurfaceHolder holder)
        {
            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
            if (mMediaController != null) mMediaController.hide();
            //release(true);
            pause();
            mSeekWhenPrepared =getCurrentPosition();
            if(mMediaPlayer!=null)mMediaPlayer.setDisplay(null);
        }
    };


    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState  = STATE_IDLE;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                }
                return true;
            } else {
                toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisiblity() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    public void start() {
        if(mCurrentState!=STATE_ERROR){
            if (isInPlaybackState()) {
                mMediaPlayer.start();
                mCurrentState = STATE_PLAYING;
            }
            mTargetState = STATE_PLAYING;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                setSpeed(mSpeedOnPrepared);
        } else {
            startOnPrepare=true;
            openVideo();
        }
    }

    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void suspend() {
        release(false);
    }

    public void resume() {
        openVideo();
    }

    // cache duration as mDuration for faster access
    public int getDuration() {
        if (isInPlaybackState()) {
            if (mDuration > 0) {
                return mDuration;
            }
            mDuration = mMediaPlayer.getDuration();
            return mDuration;
        }
        mDuration = -1;
        return mDuration;
    }

    public int getCurrentPosition() {
        try {
            if (isInPlaybackState()) {
                return mCurrentLastPosition = mMediaPlayer.getCurrentPosition();
            }
        }catch (java.lang.IllegalStateException ignored){
            return mCurrentLastPosition;
        }
        return mCurrentLastPosition;
    }

    public void seekTo(int msec) {
        if (isInPlaybackState()){
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared=msec;
        }
    }

    public boolean isPlaying() {
        try {
            return isInPlaybackState() && mMediaPlayer.isPlaying();
        }catch (java.lang.IllegalStateException ignored){
            return false;
        }
    }

    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }

    public boolean canPause() {
        return mCanPause;
    }

    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override public int getAudioSessionId() {
        return 0;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setSpeed(float speed){
        try {
            if (isInPlaybackState() && isPlaying())
                this.mMediaPlayer.setPlaybackParams(this.mMediaPlayer.getPlaybackParams().setSpeed(speed));
            mSpeedOnPrepared = speed;
            ((MyApp) mContext).dataBase.settings.setSpeed(speed);
        }catch (IllegalArgumentException e){
            Log.e(Config.LOG_TAG,e.toString());
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public float getSpeed(){
        try {
            if (isInPlaybackState()) {
                final float speed = this.mMediaPlayer.getPlaybackParams().getSpeed();
                return speed != 0 ? speed : ((MyApp) mContext).dataBase.settings.getSpeed();
            }
            return ((MyApp) mContext).dataBase.settings.getSpeed();
        }catch (IllegalArgumentException e){
            Log.e(Config.LOG_TAG,e.toString());
        }
        return 1f;
    }
    public boolean enableChangeVolume(){
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || !audioManager.isVolumeFixed();
    }
    public final boolean isMoreThan100VolumeSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    public void setVolume(float val){
        final float min= Build.VERSION.SDK_INT >= Build.VERSION_CODES.P?
                audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC):0,
                max=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        final int valueToSet = (int) (min+(max-min)*val);
        if(valueToSet > max){
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) max,0);
            if (volumeGain!=null&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                volumeGain.setTargetGain((int)((val - 1)*10000f));
                volumeGain.setEnabled(true);
            }
        }
        else{
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,valueToSet,0);
        }
        ((MyApp) mContext).dataBase.settings.setVolume(val);
    }
    public float getCurrentVolume(){
        return  ((MyApp) mContext).dataBase.settings.getVolume(this);
    }
    public float getSystemVolume(){
        final float min= Build.VERSION.SDK_INT >= Build.VERSION_CODES.P?
                audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC):0,
                max=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                now = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return (now - min)/max;
    }

}

 */
public class VideoTest extends TextureView implements MediaPlayerControl{
    private static final int MAX_BITRATE = 2893867 * 4;
    public final boolean isMoreThan100VolumeSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    private static final int MIN_SEEK_TIME = 10000;
    private SurfaceHolder mSurfaceHolder = null;
    private int mSurfaceWidth,mSurfaceHeight,mVideoWidth=1600,mVideoHeight=900, mSeekWhenPrepared;
    private float mSpeedOnPrepared;
    DefaultTrackSelector trackSelector;

    SimpleExoPlayer player;
    private AudioManager audioManager;
    private LoudnessEnhancer volumeGain;
    private boolean needSetQuality = true;
    //    private final SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
//        private boolean videoOpened=false;
//        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
//            mSurfaceWidth = w;
//            mSurfaceHeight = h;
//            boolean isValidState =  (player.getPlaybackState() == Player.STATE_READY);
//            boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
//            if (player != null && isValidState/*&& hasValidSize*/) {
//                if (mSeekWhenPrepared != 0) seekTo(mSeekWhenPrepared);
//                start();
//            }
//        }
//        public void surfaceCreated(SurfaceHolder holder) {
//            final SurfaceHolder def_holder = mSurfaceHolder;
//            mSurfaceHolder = holder;
//            holder.setKeepScreenOn(true);
//            /*if(!videoOpened && player!=null){
//                player.prepare();
//                videoOpened=true;
//            }*/if(false);
//            else{
//                if(player!=null&&def_holder!=holder){
//                    try {
//                        player.setVideoSurfaceHolder(holder);
//                    } catch (java.lang.IllegalStateException ignored) {
//                        player.pause();
//                    }
//                }
//            }
//
//        }
//        public void surfaceDestroyed(SurfaceHolder holder) {
////            Surface def_holder = null;
////            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
////                def_holder = DummySurface.newInstanceV17(getContext(),false);
////            }
//
//            // after we return from this we can't use the surface any more
//            if (player != null) {
//                pause();
//                // player.setVideoSurfaceHolder(null);
//            }
//            mSurfaceHolder = null;
//            mSeekWhenPrepared = getCurrentPosition();
//        }
//    };
    private final Player.Listener events = new Player.Listener() {
        @Override public void onIsLoadingChanged(boolean isLoading) {
            if(loadingListener!=null) loadingListener.onLoadingChange(isLoading);
        }
        @Override public void onPlaybackStateChanged(int state) {
            if(state == Player.STATE_READY && mPrepListener!=null && getWindowToken()!=null){
                if(needSetQuality){
                    final ArrayList<OneVideo.VideoType> list = getAvailableVideoQualities();
                    setVideoQuality(list.size() > 0 ? list.get(0): OneVideo.VideoType.V240);
                    needSetQuality=false;
                }
                mPrepListener.onPrepared(null);
            }
        }
        @Override public void onIsPlayingChanged(boolean isPlaying) {
            mIsPlayingListener.onIsPlayingChanged(isPlaying);
        }

        @Override
        public void onPlayerError(PlaybackException error) {
            Player.Listener.super.onPlayerError(error);
            if(Config.NEED_LOG) Log.e("VideoPlayerError",error.getMessage(),error);
            if(mErrorListener!=null)mErrorListener.onError(VideoTest.this, error);
        }

//        @Override
//        public void onPlayerError(ExoPlaybackException error) {
//        }
    };
    private OnErrorListener mErrorListener;
    private OnIsPlayingChanged mIsPlayingListener;
    private OnVideoSizeChanged mOnVideoSizeChanged;
    private MediaPlayer.OnPreparedListener mPrepListener;
    private OnBufferingUpdateListener mBufferingUpdateListener;
    private OnLoadingListener loadingListener;
    private final Runnable checkIfBufferedUpdated = new Runnable(){
        private int lastBuffered = 0;
        public void run(){
            if(getWindowToken()!=null) {
                int lastBufferedP = player.getBufferedPercentage();
                if (lastBuffered != lastBufferedP) {
                    lastBuffered = lastBufferedP;
                    if (mBufferingUpdateListener != null)
                        mBufferingUpdateListener.onUpdate(lastBufferedP, (int) player.getBufferedPosition());
                }
            }

            postDelayed(this,1000);
        }
    };
    private Map<String, String> headers=new HashMap<>();
    private MediaSessionCompat mediaSession;
    private DefaultDataSourceFactory dataSourceFactory;
    public TextView subtitle;
    TrackSelectionParameters.Builder trackParameters;
    public VideoTest(Context context) {
        super(context);
        initVideoView();
    }
    public VideoTest(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initVideoView();
    }
    public VideoTest(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initVideoView();
    }
    private void initVideoView(){
        //getHolder().addCallback(mSHCallback);
        //getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        HttpDataSource.Factory httpDataSourceFactory = new HttpDataSource.Factory() {
            private HttpDataSource.RequestProperties getHeaders(){
                final HttpDataSource.RequestProperties d = new HttpDataSource.RequestProperties();
                d.set(headers);
                return d;
            }
            @Override
            public HttpDataSource createDataSource() {
                return new DefaultHttpDataSource(
                        JummyAnimeAdapter.INSTANCE.getRandomUserAgent().getName(),
                        100000,
                        100000,
                        true,getHeaders()
                );
            }

            @Override
            public HttpDataSource.Factory setDefaultRequestProperties(Map<String, String> defaultRequestProperties) {
                if(defaultRequestProperties instanceof HashMap){
                    for(Map.Entry<String, String> j: headers.entrySet())
                        defaultRequestProperties.put(j.getKey(), j.getValue());
                }
                return null;
            }
//
//            @Override
//            public HttpDataSource.RequestProperties getDefaultRequestProperties() {
//                return getHeaders();
//            }
//
//            @Override
//            public void setDefaultRequestProperty(String name, String value) {
//                headers.put(name,value);
//            }
//
//            @Override
//            public void clearDefaultRequestProperty(String name) {
//                headers.remove(name);
//            }
//
//            @Override
//            public void clearAllDefaultRequestProperties() {
//                headers.clear();
//            }
        };
        this.dataSourceFactory = new DefaultDataSourceFactory(getContext(), httpDataSourceFactory);
        trackSelector = new DefaultTrackSelector();
        this.trackParameters = trackSelector.buildUponParameters();
        //trackSelector.setParameters(trackParameters);

        player = new SimpleExoPlayer.Builder(getContext())
                .setTrackSelector(trackSelector)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(dataSourceFactory))
                .build();
//        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setVideoTextureView(this);
        player.addListener(events);

//        player.addTextOutput(cues -> {
//        });

        player.addListener(new Player.Listener() {
            @Override
            public void onVideoSizeChanged(VideoSize videoSize) {
                Player.Listener.super.onVideoSizeChanged(videoSize);
                mVideoWidth = videoSize.width;
                mVideoHeight = videoSize.height;
                if(mOnVideoSizeChanged!=null)mOnVideoSizeChanged.onSizeChange(videoSize.width,videoSize.height);
            }

            @Override
            public void onCues(CueGroup cueGroup) {
                if(subtitle!=null) {
                    subtitle.setText("");
                    StringBuilder s = new StringBuilder();
                    for (Cue cue : cueGroup.cues)
                        s.append(cue.text).append('\n');
                    //TODO: add few textviews
                    subtitle.setText(s.substring(0, s.length()>0?s.length()-1:s.length()));
                }
                Player.Listener.super.onCues(cueGroup);
            }

            //            @Override
//            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
//                //((ConstraintLayout.LayoutParams)VideoTest.this.getLayoutParams()).dimensionRatio =asFraction(width,height);
//            }
            @Override
            public void onSurfaceSizeChanged(int width, int height) {
                int g = 12;
                //VideoTest.this.getLayoutParams().height = height;
            }
        });
        this.audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            this.volumeGain = new LoudnessEnhancer(getAudioSessionId());
        this.initMediaSession();
        disableSubtitles();
    }
    private void initMediaSession() {
        mediaSession = new MediaSessionCompat(getContext(), Config.MEDIA_SESSION_TAG);
//        final MediaMetadata.Builder mediaMetadata;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//            mediaMetadata = new MediaMetadata.Builder();
//            mediaMetadata.putLong(MediaMetadata.METADATA_KEY_DURATION, -1L);
//            //mediaSession.setMetadata(MediaMetadataCompat.fromMediaMetadata(mediaMetadata.build()));
//        }
        MediaSessionConnector mediaSessionConnector = new MediaSessionConnector(mediaSession);
        mediaSessionConnector.setPlayer(player);
    }
    @Override public void start() {
        player.play();
    }
    @Override public void pause() {
        player.pause();
    }
    @Override public int getDuration() {
        return (int) player.getDuration();
    }
    @Override public int getCurrentPosition() {
        return (int)player.getCurrentPosition();
    }
    @Override public void seekTo(int pos) {
        player.seekTo(pos);
    }
    @Override public boolean isPlaying() {
        return player.isPlaying();
    }
    @Override public int getBufferPercentage() {
        return player.getBufferedPercentage();
    }
    @Override public boolean canPause() {
        return player.getPlaybackState() == Player.STATE_READY;
    }
    @Override public boolean canSeekBackward() {
        return player.getCurrentPosition() > MIN_SEEK_TIME;
    }
    @Override public boolean canSeekForward() {
        return player.getCurrentPosition() <= player.getDuration() - MIN_SEEK_TIME;
    }
    @Override public int getAudioSessionId() {
        return player.getAudioSessionId();
    }
    @RequiresApi(api = Build.VERSION_CODES.M) public void setSpeed(float speed){
        try {
            if (player.getPlaybackState() == Player.STATE_READY )
                this.player.setPlaybackParameters(new PlaybackParameters(speed,this.player.getPlaybackParameters().pitch));

            mSpeedOnPrepared = speed;
            ((MyApp) getContext()).dataBase.settings.setSpeed(speed);
        }catch (IllegalArgumentException e){
            if(Config.NEED_LOG) Log.e(LOG_TAG,e.toString());
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M) public float getSpeed(){
        try {
            if (player.getPlaybackState() == Player.STATE_READY) {
                final float speed = this.player.getPlaybackParameters().speed;
                return speed != 0 ? speed : ((MyApp) getContext()).dataBase.settings.getSpeed();
            }
            return ((MyApp) getContext()).dataBase.settings.getSpeed();
        }catch (IllegalArgumentException e){
            if(Config.NEED_LOG) Log.e(LOG_TAG,e.toString());
        }
        return 1f;
    }
    public boolean enableChangeVolume(){
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || !audioManager.isVolumeFixed();
    }
    public void setVolume(float val){
        final float min= Build.VERSION.SDK_INT >= Build.VERSION_CODES.P?
                audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC):0,
                max=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        final int valueToSet = (int) (min+(max-min)*val);
        if(valueToSet > max){
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) max,0);
            if (volumeGain!=null&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                volumeGain.setTargetGain((int)((val - 1)*10000f));
                volumeGain.setEnabled(true);
            }
        }
        else{
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,valueToSet,0);
        }
        ((MyApp) getContext()).dataBase.settings.setVolume(val);
    }
    public float getCurrentVolume(){
        return  ((MyApp) getContext()).dataBase.settings.getVolume(this);
    }
    public float getSystemVolume(){
        final float min= Build.VERSION.SDK_INT >= Build.VERSION_CODES.P?
                audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC):0,
                max=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                now = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return (now - min)/max;
    }
    public void stopPlayback(){
        this.player.stop();
    }
    public void release(){
        this.player.release();
        this.mediaSession.release();
    }
    private MediaSource buildMediaSource(Uri uri, String mime, String language) {

//        final Format textFormat = new Format.Builder()
//                .setSampleMimeType(mime)
//                .setLanguage(language)
//                .build();
//        Format textFormat = Format.createTextSampleFormat(null, mime, Format.NO_VALUE, language);
        //Log.e("srt link is",srt_link);
        MediaSource subtitleSource = new SingleSampleMediaSource.Factory(dataSourceFactory)//(new DefaultHttpDataSource.Factory())
                .createMediaSource(
                        new MediaItem.SubtitleConfiguration.Builder(uri)
                            .setMimeType(mime)
                                .setLanguage(language)
                                .build()
                        , C.TIME_UNSET);
//        MediaSource subtitleSource = new SingleSampleMediaSource(uri, dataSourceFactory, textFormat, C.TIME_UNSET);
        return subtitleSource;
    }

    public void setVideoURI(Uri uri, Map<String, String> headers, List<Subtitle> subtitles) {
        needSetQuality=true;
        mSeekWhenPrepared = 0;
        this.headers = headers;
        MediaItem.Builder mediaItem = new MediaItem.Builder()
                .setUri(uri)
                .setDrmMultiSession(true);
        if (subtitles!=null && subtitles.size()>0) {
//            mediaItem.setSubtitles(ArrayUtilsKt.selectByFunc(subtitles,
//                    subtitle -> new MediaItem.Subtitle(Uri.parse(subtitle.url), subtitle.getMime(), subtitle.getLanguage())
//            ));
//            player.setMediaItem(mediaItem.build());
            ArrayList<MediaSource> sources = ArrayUtilsKt.selectByFunc(subtitles,
                    subtitle -> buildMediaSource(Uri.parse(subtitle.url), subtitle.getMime(), subtitle.getLanguage())
//                            new SingleSampleMediaSource.Factory(dataSourceFactory).createMediaSource(
//                                    new MediaItem.Subtitle(Uri.parse(subtitle.url), subtitle.getMime(), subtitle.getLanguage(),
//                                            C.SELECTION_FLAG_DEFAULT), C.TIME_UNSET)
            );
            MediaSource[] source = new MediaSource[sources.size()+1];
            for(int i=0;i<sources.size();i++) source[i+1] = sources.get(i);
            source[0] = new DefaultMediaSourceFactory(dataSourceFactory).createMediaSource(mediaItem.build());

            MediaSource ans = new MergingMediaSource(source);
            player.setMediaSource(ans);
        }
        else player.setMediaItem(mediaItem.build());

        player.prepare();
        requestLayout();

    }

    public VideoTest setOnErrorListener(OnErrorListener mErrorListener){
        this.mErrorListener = mErrorListener;
        return this;
    }
    public VideoTest setOnPreparedListener(MediaPlayer.OnPreparedListener mPrepListener){
        this.mPrepListener = mPrepListener;
        return this;
    }
    public VideoTest setOnIsPlayingListener(OnIsPlayingChanged listener){
        this.mIsPlayingListener = listener;
        return this;
    }
    public VideoTest setLoadingListener(OnLoadingListener loadingListener) {
        this.loadingListener = loadingListener;
        return this;
    }
    public VideoTest setOnBufferingUpdateListener(OnBufferingUpdateListener onBufferingUpdateListener){
        this.mBufferingUpdateListener = onBufferingUpdateListener;
        removeCallbacks(checkIfBufferedUpdated);
        if(onBufferingUpdateListener!=null) checkIfBufferedUpdated.run();
        return this;
    }
    public int getVideoWidth(){
        return mVideoWidth;
    }
    public int getVideoHeight(){
        return mVideoHeight;
    }
    public VideoTest setOnVideoSizeChanged(OnVideoSizeChanged onVideoSizeChanged) {
        this.mOnVideoSizeChanged=onVideoSizeChanged;
        return this;
    }

    public MediaSessionCompat getMediaSession() {
        return mediaSession;
    }

    public static interface OnErrorListener{
        public void onError(VideoTest test, PlaybackException exception);
    }
    public static interface OnLoadingListener{
        public void onLoadingChange(boolean isLoading);
    }
    public static interface OnVideoSizeChanged{
        public void onSizeChange(int videoWidth,int videoHeight);
    }
    public static interface OnIsPlayingChanged{
        public void onIsPlayingChanged(boolean isPlaying);
    }
    public strictfp interface OnBufferingUpdateListener{
        public void onUpdate(int percent,int position);
    }

    public void setVideoQuality(OneVideo.VideoType quality){
        int bitrate = getQualityBitrate(quality);
        trackParameters.setMaxVideoBitrate(bitrate)
                //.setMaxVideoSize((int)(qualityInt * ((float)mVideoWidth / mVideoHeight)),qualityInt)
                .setForceHighestSupportedBitrate(true);
        trackSelector.setParameters(trackParameters.build());
    }
    private int getQualityBitrate(OneVideo.VideoType quality){
        ArrayList<Format> arrayList = getAvailableVideoQualitiesF(format -> format.sampleMimeType != null && format.sampleMimeType.contains("video"));
        OneVideo.VideoType type;
        for(Format dataFormat : arrayList) {
            if(dataFormat.height <= 0)       type = OneVideo.VideoType.UNDEFINED;
            else if(dataFormat.height < 300) type = OneVideo.VideoType.V240;
            else if(dataFormat.height < 420) type = OneVideo.VideoType.V360;
            else if(dataFormat.height < 660) type = OneVideo.VideoType.V480;
            else if(dataFormat.height < 900) type = OneVideo.VideoType.V720;
            else                             type = OneVideo.VideoType.V1080;
            if(quality == type){
                if(dataFormat.bitrate > 0) return dataFormat.bitrate;
                else break;
            }
        }
        return MAX_BITRATE;
    }
    private ArrayList<Format> getAvailableVideoQualitiesF(IsValid isValid){
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        ArrayList<Format> ans = new ArrayList<>();
        if (mappedTrackInfo!=null)
            for (int rendererIndex = 0; rendererIndex < mappedTrackInfo.getRendererCount(); rendererIndex++) {
                TrackGroupArray trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex);

                for (int groupIndex = 0; groupIndex < trackGroupArray.length; groupIndex++) {
                    for (int trackIndex = 0; trackIndex <  trackGroupArray.get(groupIndex).length; trackIndex++) {
                        final Format format = trackGroupArray.get(groupIndex).getFormat(trackIndex);

                        if (format.sampleMimeType!=null && isValid.isValid(format))
                            ans.add(format);
                    }
                }
            }
        return ans;
    }
    public ArrayList<OneVideo.VideoType> getAvailableVideoQualities(){
        try {
            ArrayList<Format> arrayList = getAvailableVideoQualitiesF(format->format.sampleMimeType != null && format.sampleMimeType.contains("video"));
            ArrayList<OneVideo.VideoType> ans = new ArrayList<>(arrayList.size());
            OneVideo.VideoType type;
            for (Format f : arrayList) {
                final int height = f.height;
                if (height <= 0) type = OneVideo.VideoType.UNDEFINED;
                else if (height < 300) type = OneVideo.VideoType.V240;
                else if (height < 420) type = OneVideo.VideoType.V360;
                else if (height < 660) type = OneVideo.VideoType.V480;
                else if (height < 900) type = OneVideo.VideoType.V720;
                else type = OneVideo.VideoType.V1080;
                if (!ans.contains(type)) ans.add(type);
            }
            ArrayUtilsKt.sortBy(ans, (el, el1) -> el.ordinal() - el1.ordinal());

            return ans;
        }catch (Exception e){
            Log.e(LOG_TAG, e.getMessage(), e);
            return new ArrayList<>(0);
        }
    }
    public void disableSubtitles(){
        trackSelector.setParameters(new DefaultTrackSelector.ParametersBuilder()
                .setRendererDisabled(C.TRACK_TYPE_VIDEO, true)
                .setRendererDisabled(C.TRACK_TYPE_TEXT, true));
    }
    public void setSubtitle(@NotNull Subtitle subtitle) {

        trackSelector.setParameters(trackParameters
                .setPreferredTextLanguage(subtitle.getLanguage())
//                        .setTrackTypeDisabled()
                .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, false)
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                        .build()
        );

    }
    interface IsValid{
        boolean isValid(Format format);
    }
    class Item{
        int index;
        Format format;
        TrackGroupArray trackGroups;
    }
}