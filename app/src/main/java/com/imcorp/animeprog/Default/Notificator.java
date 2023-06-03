package com.imcorp.animeprog.Default;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.imcorp.animeprog.R;
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.errors.YummyError;

import org.jetbrains.annotations.NotNull;

public class Notificator {
    public static final byte STATE_ADDED=1;
    public static final byte STATE_DELETED=0;
    public static final byte STATE_ERROR=-1;

    private final MyApp mContext;
    private Snackbar snackbar;
    Toast showingNow;
    public Notificator(MyApp context){
        this.mContext = context;
    }
    public void showAddedAnimeToFavorites(final byte state, final View.OnClickListener onActionClick,
                                          @Nullable final Runnable onDismiss) {
        if (snackbar != null && snackbar.isShown()) snackbar.dismiss();
        final int text, actionButton;
        switch (state) {
            case STATE_ADDED:
                text = R.string.favorites_added;
                actionButton = R.string.cancel;
                break;
            case STATE_DELETED:
                text = R.string.favorites_deleted;
                actionButton = R.string.cancel;
                break;
            default:
                text = R.string.favorites_error_added;
                actionButton = R.string.retry;
        }
        snackbar = Snackbar.make(mContext.findViewById(R.id.parent), text, BaseTransientBottomBar.LENGTH_LONG)
                .setAction(actionButton, onActionClick);
        if (onDismiss != null)
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                if(event != Snackbar.Callback.DISMISS_EVENT_ACTION)
                    new Thread(onDismiss).start();
                }
            });
        snackbar.show();
    }
    public void showAddedAnimeToFavorites(final byte state, final View.OnClickListener onActionClick){
        this.showAddedAnimeToFavorites(state,onActionClick,null);
    }
    public void showDubbingsChanged(final View.OnClickListener onActionClick) {
        if (snackbar != null && snackbar.isShown()) snackbar.dismiss();
        snackbar = Snackbar.make(mContext.findViewById(R.id.parent), mContext.getString(R.string.dubbing_has_changed), BaseTransientBottomBar.LENGTH_SHORT)
                .setAction(R.string.change_dubbing,onActionClick);
        snackbar.show();
    }
    public void showPlayMarketNotInstalled() {
        if (snackbar != null && snackbar.isShown()) snackbar.dismiss();
        snackbar = Snackbar.make(mContext.findViewById(R.id.parent),R.string.play_market_not_installed,BaseTransientBottomBar.LENGTH_SHORT);
        snackbar.show();
    }

    public void showMediaPlayerError(View view, @StringRes int messageId,final View.OnClickListener retry,final Runnable onCancel) {
        if(snackbar != null && snackbar.isShown())
            snackbar.dismiss();
        snackbar = Snackbar.make(view,messageId,BaseTransientBottomBar.LENGTH_SHORT)
                .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    public void onDismissed(Snackbar transientBottomBar, int event) { onCancel.run(); }
                })
                .setAction(R.string.retry,retry);
        snackbar.show();
    }
    public void showNeedPermission(View view, View.OnClickListener onActionClick) {
        if(snackbar != null && snackbar.isShown())
            snackbar.dismiss();
        snackbar = Snackbar.make(view,R.string.need_permission,BaseTransientBottomBar.LENGTH_LONG)
                .setAction(R.string.ok,onActionClick);
        snackbar.show();
    }

    public void showEnterFullscreen(View view,@Nullable View.OnClickListener onActionClick) {
        if(snackbar != null && snackbar.isShown())
            snackbar.dismiss();
        snackbar = Snackbar.make(view,R.string.enter_fullscreen,BaseTransientBottomBar.LENGTH_SHORT)
                .setAction(R.string.enter,onActionClick);
        snackbar.show();
    }
    public void showAuthError(View view, @NotNull YummyError error, Runnable retry) {
        if(snackbar != null && snackbar.isShown()) snackbar.dismiss();
        SpannableString errorSpan = new SpannableString(error.getErrorTitle()+": "+error.getError());
        errorSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, error.getErrorTitle().length(), 0);
        snackbar = Snackbar.make(view, errorSpan,BaseTransientBottomBar.LENGTH_SHORT)
                .setAction(R.string.retry,g-> retry.run());
        snackbar.show();


    }
    public void showHistoryDeleted(final View view,Runnable onDismiss,Runnable onCancel) {
        if(snackbar != null && snackbar.isShown()) snackbar.dismiss();
        snackbar = Snackbar.make(view,R.string.history_deleted,BaseTransientBottomBar.LENGTH_SHORT)
                .setAction(R.string.cancel,g-> onCancel.run())
                .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        if(event!= BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_ACTION){
                            onDismiss.run();
                        }
                    }
                });
        snackbar.show();

    }

    public void showCaptchaNeeded() {
        Toast.makeText(this.mContext, R.string.captcha_needed, Toast.LENGTH_SHORT)
                .show();
    }


}
