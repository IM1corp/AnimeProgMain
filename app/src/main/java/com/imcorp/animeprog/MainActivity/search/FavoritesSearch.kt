package com.imcorp.animeprog.MainActivity.search

import com.imcorp.animeprog.Config
import com.imcorp.animeprog.MainActivity.MainActivity
import com.imcorp.animeprog.Requests.JsonObj.OneAnime

class FavoritesSearch(fragment: SearchFragment) : SimpleSearchAdapter(fragment,fragment){
    override fun getLittleData(q: String): ArrayList<OneAnime.OneAnimeWithId> {
        val data = (fragment.activity as MainActivity).dataBase.favorites.search(q, Config.SEARCH_ITEMS_LOAD_COUNT);
        return data;
    }

    override fun getBigSearchData(q: String): ArrayList<OneAnime.OneAnimeWithId> {
        TODO("Not yet implemented")
    }

}