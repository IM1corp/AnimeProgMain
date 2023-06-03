package com.imcorp.animeprog.MainActivity.fragments.Adapters.animePreviewItem;

import com.imcorp.animeprog.Requests.JsonObj.OneAnime;

import org.jetbrains.annotations.NotNull;

public interface Events extends onItemEdit{
    public void onItemClick(@NotNull OneAnime.OneAnimeWithId anime, int index);
    public void onErrorLoadingImage(Exception e);
    public boolean loadMore(int offset);
}