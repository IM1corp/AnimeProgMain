package com.imcorp.animeprog.DownloadManager;

import android.content.Intent;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.Default.MyApp;
import com.imcorp.animeprog.Default.ParcelableSparseIntArray;
import com.imcorp.animeprog.DownloadManager.DownloadService.DownloadService;
import com.imcorp.animeprog.R;
import com.imcorp.animeprog.Requests.JsonObj.OneAnime;

public class DownloadManager {
    MyApp activity;
    OneAnime anime;
    private RecyclerView episodesRecycleView;
    private RecycleViewAdapter adapter;
    Button okButton;
    public DownloadManager(MyApp activity, OneAnime thing_to_download){
        this.activity = activity;
        this.anime = thing_to_download;
    }
    public void showWindow(){
        if(anime == null)return;
        this.initInterface();
    }
    private void initInterface(){
        Rect displayRectangle = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.CustomAlertDialog);
        ViewGroup viewGroup = activity.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.fragment_download, viewGroup, false);

        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        dialogView.findViewById(R.id.cancelButton).setOnClickListener(v -> alertDialog.cancel());
        dialogView.findViewById(R.id.okButton).setOnClickListener(v -> {
            alertDialog.cancel();
            new Thread(this::startDownloading).start();
        });
        okButton = dialogView.findViewById(R.id.okButton);
        alertDialog.show();


        this.episodesRecycleView = dialogView.findViewById(R.id.recyclerView);
        this.episodesRecycleView.setHasFixedSize(true);
        this.episodesRecycleView.setAdapter(adapter = new RecycleViewAdapter(this));
        this.episodesRecycleView.setItemViewCacheSize(20);
        this.episodesRecycleView.setDrawingCacheEnabled(true);
        this.episodesRecycleView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        this.episodesRecycleView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
        this.episodesRecycleView.setNestedScrollingEnabled(false);
    }
    private void startDownloading(){
        if(this.adapter == null || this.episodesRecycleView == null || adapter.map.size() == 0)return;

        Intent service = new Intent(activity, DownloadService.class);
        service.putExtra(Config.EPISODES_MAP,new ParcelableSparseIntArray<>(adapter.map,RecycleViewAdapter.EpisodeSelected.class));
        service.putExtra(Config.FRAGMENT_ANIME_OBJ,anime);
        activity.startService(service);
    }
}
