package com.imcorp.animeprog.MainActivity.search

import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Requests.JsonObj.OneAnime

class DownloadsSearch(fragment: SearchFragment) : SimpleSearchAdapter(fragment,fragment){

    override fun getLittleData(q: String): ArrayList<OneAnime.OneAnimeWithId> {
        return fragment.activity.dataBase.downloads.search(q, Config.SEARCH_ITEMS_LOAD_COUNT)
    }
    override fun getBigSearchData(q: String): ArrayList<OneAnime.OneAnimeWithId> = getLittleData(q);
}