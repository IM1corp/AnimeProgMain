package com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeVideo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.Default.NoScrollableNestedScrollView;
import com.imcorp.animeprog.OneAnimeActivity.Fragments.Comments.CommentsFragment;
import com.imcorp.animeprog.OneAnimeActivity.Fragments.MyFragment;
import com.imcorp.animeprog.OneAnimeActivity.OneAnimeActivity;
import com.imcorp.animeprog.R;
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException;
import com.imcorp.animeprog.Requests.JsonObj.OneAnime;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;

public class FragmentOneAnimeVideo extends Fragment implements MyFragment {
    OneAnimeActivity context;
    private ConstraintLayout videoConstraintLayout,errorConstraintLayout;
    private androidx.appcompat.widget.AppCompatImageView voiceStudioQuestionMark,videoPlayerQuestionMark;
    private TextView errorText;
    private Button actionButton;
    NoScrollableNestedScrollView scrollView;
    public FragmentVideoPlayer videoPlayer;

    private OneAnime anime;
    OneVideo videoSelected;

    private SpinnerController spinnerController;
    public CommentsFragment commentsFragment;
    //region onCreate
    public FragmentOneAnimeVideo(){
        super();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_one_anime_video,container,false);
        this.context = (OneAnimeActivity) getActivity();
        videoConstraintLayout = view.findViewById(R.id.videoConstraintLayout);
        errorConstraintLayout = view.findViewById(R.id.errorConstraintLayout);
        videoPlayerQuestionMark = videoConstraintLayout.findViewById(R.id.videoPlayerQuestionMark);
        voiceStudioQuestionMark = videoConstraintLayout.findViewById(R.id.voiceStudioQuestionMark);
        spinnerController = new SpinnerController(
                videoConstraintLayout.findViewById(R.id.episodesSpinner),
                videoConstraintLayout.findViewById(R.id.authorSpinner),
                videoConstraintLayout.findViewById(R.id.videoPlayerSpinner),
                this);
        errorText = errorConstraintLayout.findViewById(R.id.errorTextView);
        actionButton=errorConstraintLayout.findViewById(R.id.actionButton);
        scrollView = view.findViewById(R.id.scrollView);
        setHintsOnclick();

        videoConstraintLayout.setVisibility(View.INVISIBLE);
        errorConstraintLayout.setVisibility(View.GONE);
        if(savedInstanceState!=null){
            spinnerController.restoreInstanceState(savedInstanceState);
        }
        if(this.anime!=null  && this.anime.isFullyLoaded() ){
            this.setData(anime);
        }
        return view;
    }

//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        FragmentContainerView g =  view.findViewById(R.id.commentsFragment);
//
//    }

    public void onAttachFragment(@NonNull Fragment fragment) {
        if (fragment instanceof FragmentVideoPlayer) {
            videoPlayer = (FragmentVideoPlayer) fragment;
        }
        else if(fragment instanceof CommentsFragment){
            commentsFragment = (CommentsFragment) fragment;
        }
    }

    @Override public void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        spinnerController.saveInstanceState(bundle);
    }

    private void setHintsOnclick(){
        videoPlayerQuestionMark.setOnClickListener(view -> new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.video_player)
                    .setMessage(R.string.video_player_hint)
                    .setIcon(R.drawable.ic_info)
                    .setPositiveButton(R.string.ok,null)
                    .create()
                    .show());
        voiceStudioQuestionMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                StringBuilder hint = new StringBuilder();
                OneVideo video = anime.videos.videos.get(authorSpinner.getSelectedItemPosition()).value.get(0);
                if (video == null) {
                    hint.delete(0, hint.length());
                    hint.append(getString(R.string.undefined_error));
                    return;
                } else if (video.voiceAuthors==null||video.voiceAuthors.length == 0) {
                    hint.delete(0, hint.length());
                    hint.append(getString(R.string.no_voice_authors));
                } else {
                    for (int i = 0; i < video.voiceAuthors.length; i++) {
                        hint.append(video.voiceAuthors[i]);
                        if (i != video.voiceAuthors.length - 1) hint.append(",\n");
                    }
                }
                new AlertDialog.Builder(context)
                        .setTitle(getResources().getString(R.string.voice_authors))
                        .setMessage(hint.toString())
                        .setIcon(R.drawable.ic_info)
                        .setPositiveButton("OK",null)
                        .create()
                        .show();
                 */
            }
        });

    }
    //endregion
    public void setCommentsData(){
        if (anime.comments!=null && anime.comments.getCommentsList().size() != 0) {
            commentsFragment.updateComments(true);
        }
    }
    @Override public void setData(final OneAnime anime){
        this.anime=anime;
        this.setCommentsData();
        if(videoConstraintLayout!=null){
            if(anime.responseType == OneAnime.VideoResponseType.SUCCESS && anime.videos.size()!=0){
                videoConstraintLayout.setVisibility(View.VISIBLE);
                errorConstraintLayout.setVisibility(View.GONE);
                spinnerController.setSpinners(anime);
            } else{
                errorConstraintLayout.setVisibility(View.VISIBLE);
                videoConstraintLayout.setVisibility(View.GONE);

                errorText.setText(anime.getErrorFromResponseType());
                final int error = anime.getActionButtonFromResponseType();
                    actionButton.setText(error);
                    actionButton.setOnClickListener(view -> {
                        switch (anime.responseType){
                            case VIDEO_NOT_ABLE_IN_YOUR_COUNTRY:
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Config.vpn_string)));
                                }catch (android.content.ActivityNotFoundException e){
                                    context.notificator.showPlayMarketNotInstalled();
                                }
                                break;
                            case VIDEO_DELETED:
                                OneAnimeActivity activity = ((OneAnimeActivity)getActivity());
                                activity.loadAnimeFromPath(null);
                                break;
                        }
                    });

            }
        }
    }
    private void onSuccessLoadedEpisode(){
        this.videoPlayer.setLoading(false);
        if(videoSelected.videoQualities==null){
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Плеер в разработке")
                    .setMessage("К сожалению, пока данный плеер не работает.")
                    .setIcon(R.drawable.ic_info)
                    .setPositiveButton(R.string.ok,null)
                    .create()
                    .show();
            return;
        }
        try {
            this.videoPlayer.loadVideo(anime, videoSelected);
        }
        catch (InvalidHtmlFormatException e) {
            context.showInvalidHtmlException(e);
        } catch(Throwable e) {
            context.showUndefinedError("Video parser error",e);
        }
    }
    void loadOneVideo(){
        if(videoSelected==null)return;
        this.videoPlayer.setLoading(true);
        new Thread(() -> {
            try {
                videoSelected.getVideoQualities(anime,context,anime.getHostString(),videoSelected,true);
                context.threadCallback.post(this::onSuccessLoadedEpisode);
                return;
            }
            catch (final InvalidHtmlFormatException e){
                context.threadCallback.post(() -> new MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.video_player_not_enabled)
                        .setMessage(getString(R.string.invalid_video_html, e.getMessage()))
                        .setIcon(R.drawable.ic_error)
                        .setPositiveButton(R.string.ok,null)
                        .setNeutralButton(R.string.show_video_video_view,(eb,q1)->{
                            eb.dismiss();
                            videoPlayer.showVideoView(videoSelected.urlFrame, new HashMap<String,String>(1){{
                                put("Referer", anime.getAnimeURI());
                            }});
                        })
                        .create()
                        .show());
            }
            catch (IOException e) {
                Log.e(Config.VIDEO_PLAYER_LOG,e.getMessage(),e);
                context.showNoInternetException();
            }
            catch (JSONException e) {
                context.showInvalidJsonError();
            }
            catch (Throwable e){
                context.showUndefinedError("Video parser error", e);
                Log.e(Config.LOG_TAG,e.getMessage(),e);
            }
            context.threadCallback.post(() -> videoPlayer.show(false));
        }).start();
    }

    public void nextVideoButtonClick(){
        final String voiceStudio = videoSelected.voiceStudio;
        this.videoPlayer.setLoading(true);
        final int nextEp = this.spinnerController.getSelectedEpisodeSpinnerIndex()+1;
        if (this.anime.videos.size() == nextEp) {
            this.videoPlayer.show(false);
        }
        else {
            this.spinnerController.loadEp(nextEp,voiceStudio);
        }
    }
    public void backVideoButtonClick(){
        final String voiceStudio = videoSelected.voiceStudio;
        this.videoPlayer.setLoading(true);
        final int backEp = this.spinnerController.getSelectedEpisodeSpinnerIndex()-1;
        if (backEp==-1) {
            this.videoPlayer.show(false);
        }
        else {
            this.spinnerController.loadEp(backEp,voiceStudio);
        }
    }
}
