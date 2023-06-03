package com.imcorp.animeprog.Default;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.imcorp.animeprog.BuildConfig;
import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.DB.DataBase;
import com.imcorp.animeprog.DB.Settings;
import com.imcorp.animeprog.MainActivity.MainActivity;
import com.imcorp.animeprog.R;
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException;
import com.imcorp.animeprog.Requests.Http.Request;
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.errors.YummyError;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

@SuppressLint("Registered")
public class MyApp extends AppCompatActivity {
    public Handler threadCallback;
    public ArrayList<OnBackPressed> backCallbacks = new ArrayList<>();
    public ArrayList<OnTouch> touchCallbacks = new ArrayList<>();
    public Notificator notificator = new Notificator(this);
    public DataBase dataBase=new DataBase(this);
    public Request request = new Request(this);
    private final LinkedList<OnPermission> permissions = new LinkedList<OnPermission>();

    @CallSuper
    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        updateTheme(false);

//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        initYandexAppVisor();
        final String error=getIntent().getStringExtra(Config.DATA_ERROR);
        if(error!=null){
            if(!BuildConfig.DEBUG && dataBase.settings.getSendBugs())
                YandexMetrica.reportError("Fatal error",error);
            new MaterialAlertDialogBuilder(this)
                    .setPositiveButton(R.string.ok, (d,_d)->d.dismiss())
                    .setNegativeButton(R.string.copy_text, (d,_d)->{
                        copyText(error);
                        d.dismiss();
                    })
                    .setTitle(R.string.undefined_error)
                    .setMessage(error).show();
        }

        Thread.currentThread().setUncaughtExceptionHandler ((_g, e) ->{
            if(Config.NEED_LOG) Log.e(Config.LOG_TAG,e.getLocalizedMessage(),e);
                startActivity(new Intent(this,MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(Config.DATA_ERROR, e.toString() + '\n' + Log.getStackTraceString(e)));
            android.os.Process.killProcess(android.os.Process.myPid());
        });

        threadCallback = new Handler();
        if((dataBase.settings.getUserProgress() & Settings.PROGRESS_VERIFY_POLICY) == 0 ) showPolicyDialog();
//        ProviderInstaller.installIfNeededAsync(this,this);
//        SSLContext sslContext;
//        try {
//            sslContext = SSLContext.getInstance("TLSv1.2");
//            sslContext.init(null, null, null);
//            sslContext.createSSLEngine();
//        }
//        catch (NoSuchAlgorithmException|KeyManagementException e) {
//            e.printStackTrace();
//        }
    }
    private void showPolicyDialog() {
        AlertDialog dialog= new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.policy_title)
                .setMessage(R.string.policy_description)
                .setIcon(R.drawable.ic_info)
                .setPositiveButton(R.string.verify_policy,(_s, b)->
                        dataBase.settings.setUserProgress(dataBase.settings.getUserProgress() | Settings.PROGRESS_VERIFY_POLICY ))

                .setOnCancelListener(onC ->{
                    if((dataBase.settings.getUserProgress() & Settings.PROGRESS_VERIFY_POLICY )==0)
                        showPolicyDialog();
                })
                .show();
        ((TextView)dialog.findViewById(android.R.id.message))
                .setMovementMethod(LinkMovementMethod.getInstance());
        dialog.show();
    }

    @Override public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();
//        if(this.dataBase.settings.isDarkTheme()){
            //theme.applyStyle(R.style.AppDarkTheme, true);
//        }
        // you could also use a switch if you have many themes that could apply
        return theme;
    }
    public boolean isThemeDark(){
        final int theme = dataBase.settings.getTheme();
        if(theme == 2) {
            final int themeMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
            return themeMode == Configuration.UI_MODE_NIGHT_YES;
        }
        return theme == 1;
    }
    public void updateTheme(boolean recreate){
        final int theme = dataBase.settings.getTheme();
        if(theme == 2) {
            final int themeMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
            setTheme(Settings.THEMES_STYLE[ themeMode == Configuration.UI_MODE_NIGHT_YES ? 1 : 0]);
        }
        else setTheme(Settings.THEMES_STYLE[theme]);
        if(recreate){
            PackageManager packageManager = getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
            ComponentName componentName = intent.getComponent();
            intent = Intent.makeRestartActivityTask(componentName);
            startActivity(this instanceof MainActivity?intent.putExtra(Config.GO_TO_FRAGMENT_ID,((MainActivity)this).getCurrentMenuItemId()):intent);
            finish();



//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//
//                final ImageView imageView = findViewById(R.id.imageViewChangeTheme);
//                ConstraintLayout container = (ConstraintLayout)findViewById(R.id.parent);
//                final int w = container.getMeasuredWidth(),
//                        h = container.getMeasuredHeight();
//                final Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//                final Canvas canvas = new Canvas(bitmap);
//
//                container.draw(canvas);
//                recreate();
//
//                imageView.setImageBitmap(bitmap);
//                imageView.setColorFilter(Color.parseColor("#12312367"));
//
//                final double finalRadius = hypot((float)w, (float)h);
//
//
//                Animator anim = ViewAnimationUtils.createCircularReveal(imageView, w / 2, h / 2, 0f, (float)finalRadius);
//                anim.setDuration(4000L);
//                anim.addListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        super.onAnimationEnd(animation);
//                        imageView.setImageDrawable(null);
//                        imageView.setVisibility(View.GONE);
//                    }
//                });
//                anim.setStartDelay(1000);
//                anim.start();
//            } else recreate();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            TypedValue t= new TypedValue();
            getTheme().resolveAttribute(R.attr.navigationColor,t,true);
            getWindow().setNavigationBarColor(getResources().getColor(t.resourceId));
        }

    }
    public void showInvalidJsonError(){
        runRunnableInUI(() -> {
            if(notificator.showingNow!=null)notificator.showingNow.cancel();
            notificator.showingNow = Toast.makeText(getApplicationContext(),"Invalid json format.",Toast.LENGTH_SHORT);
            notificator.showingNow.show();
        });
    }
    public void showNoInternetException(){ showNoInternetException(null); }
    public void showNoInternetException(IOException ex){
        runRunnableInUI(() -> {
            if(notificator.showingNow!=null)notificator.showingNow.cancel();
            SpannableStringBuilder b = new SpannableStringBuilder(getString(R.string.no_internet));
            b.setSpan(Typeface.BOLD,0,b.length(),0);
            notificator.showingNow=Toast.makeText(getApplicationContext(),
                    b.append('\n').append(ex!=null?ex.getMessage():""), Toast.LENGTH_SHORT);
            notificator.showingNow.show();
        });
    }
    public void showInvalidStatusException(){
        runRunnableInUI(() -> {
            if(notificator.showingNow!=null)notificator.showingNow.cancel();
            notificator.showingNow=Toast.makeText(getApplicationContext(),"Invalid status.",Toast.LENGTH_SHORT);
            notificator.showingNow.show();
        });
    }
    @Override public void onBackPressed(){
        for(int i=backCallbacks.size();--i>=0;){
            if(backCallbacks.get(i).onBackPressed()){
                return;
            }
        }
        super.onBackPressed();
    }

    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        for(OnTouch r:touchCallbacks){
            r.onTouch(event);
        }
        return super.dispatchTouchEvent(event);
    }

    public void showInvalidHtmlException(final InvalidHtmlFormatException ex) {
        final CharSequence text;
        if(ex instanceof InvalidHtmlFormatException.NoVideosFoundException){
            text = ex.getMessage();
        }else{
            text = getString(R.string.invalid_video_html,ex.getMessage());
        }
        runRunnableInUI(() -> new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.undefined_error)
                .setMessage(text)
                .setNegativeButton(R.string.copy_error_code, (dialog, which) -> copyText(ex.getMessage()))
                .setPositiveButton(R.string.ok,null)
                .show());
    }
    public void showYummyError(YummyError error){
        runRunnableInUI(() -> {
            if(notificator.showingNow!=null)notificator.showingNow.cancel();
            notificator.showingNow = Toast.makeText(getApplicationContext(),error.getErrorTitle()+": "+error.getMessage(),Toast.LENGTH_SHORT);
            notificator.showingNow.show();
        });
    }
    public void showUndefinedError(String error,Throwable th) {
        runRunnableInUI(() -> {
            if(notificator.showingNow!=null)notificator.showingNow.cancel();
            notificator.showingNow = Toast.makeText(getApplicationContext(),"Undefined error...",Toast.LENGTH_SHORT);
            notificator.showingNow.show();
        });
        if(error!=null&&th!=null&&
                !BuildConfig.DEBUG && dataBase.settings.getSendBugs())
                YandexMetrica.reportError(error, th);

    }
    public void showUndefinedError(){
        showUndefinedError(null,null);
    }

    public void addOnRequestPermissionListener(@NotNull OnPermission permission) {
        this.permissions.add(permission);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions,@NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        final boolean success = grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        for (Iterator<OnPermission> p = this.permissions.iterator(); p.hasNext(); ) {
            OnPermission per = p.next();
            if (per.onRequest(success, requestCode, permissions))
                p.remove();
        }
    }


    public interface OnTouch {
        void onTouch(MotionEvent event);
    }
    public void copyText(String data){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if(clipboard==null)return;
        ClipData clip = ClipData.newPlainText("", data);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this,R.string.text_has_been_copied,Toast.LENGTH_SHORT).show();
    }
    @CallSuper
    @Override public void onDestroy() {
        if(this instanceof MainActivity){
            this.dataBase.cache.deleteCache();
        }
        this.dataBase.close();
        super.onDestroy();
    }
    public void runRunnableInUI(final Runnable runnable){
        if(inMainThread())runnable.run();
        else threadCallback.post(runnable);
    }

    public static boolean inMainThread(){return Looper.myLooper() == Looper.getMainLooper();}
    private void initYandexAppVisor(){
        YandexMetricaConfig config = YandexMetricaConfig.newConfigBuilder(Config.API_key).build();
        // Initializing the AppMetrica SDK.
        YandexMetrica.activate(getApplicationContext(), config);
        // Automatic tracking of user activity.
        if(dataBase.settings.getSendBugs() && !BuildConfig.DEBUG) YandexMetrica.enableActivityAutoTracking(this.getApplication());
    }
    interface OnPermission{
        boolean onRequest(boolean success, int requestCode, String[] permissions);
    }
}
