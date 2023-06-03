package com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeVideo.controls;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeVideo.VideoPlayerControls;
import com.imcorp.animeprog.OneAnimeActivity.OneAnimeActivity;
import com.imcorp.animeprog.R;
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException;
import com.imcorp.animeprog.Requests.JsonObj.OneAnime;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo;

public class DeleteOneVideoHandler {
    private final VideoPlayerControls fragment;
    private AlertDialog dialog;
    private final OnClose onClose;
    private final OneVideo deletingVideo;
    private final OneAnimeActivity activity;
    public DeleteOneVideoHandler(VideoPlayerControls fragment,OnClose onEnc) {
        this.onClose = onEnc;
        this.fragment = fragment;
        this.deletingVideo = fragment.getEpisode();
        this.activity = ((OneAnimeActivity)fragment.getActivity());
    }
    public void showDialog() {
        final MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(fragment.getContext())
            .setTitle(R.string.delete_video_dialog_title)
            .setMessage(Config.getText(fragment.getContext(), R.string.delete_video_dialog_desc,deletingVideo.num))
            .setIcon(R.drawable.ic_delete_forever)
            .setPositiveButton(R.string.delete, (dialog, which) -> onSuccessClick())
            .setNegativeButton(R.string.cancel, (dialog, which) -> onAction(false));
        dialog = dialogBuilder.show();
    }
    private void onAction(final boolean deleting){
        dialog.cancel();
        onClose.onClose(deleting);
    }
    private void onSuccessClick(){
        onAction(true);
        new Thread(this::deleteThread).start();
    }
    private void deleteThread(){
        final OneAnime anime = activity.getOneAnime();
        try{
            activity.dataBase.downloads.deleteVideoFromDownloads(anime,deletingVideo);
        } catch (InvalidHtmlFormatException e){
            activity.showInvalidHtmlException(e);
        }
    }

    public interface OnClose{
        public void onClose(final boolean deleting);
    }
}
