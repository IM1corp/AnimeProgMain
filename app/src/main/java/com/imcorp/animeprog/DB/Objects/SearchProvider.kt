package com.imcorp.animeprog.DB.Objects

import android.content.Context
import android.content.SearchRecentSuggestionsProvider
import android.provider.SearchRecentSuggestions

class SearchProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }
    companion object {
        fun saveQuery(context: Context, q: String) = SearchRecentSuggestions(context, AUTHORITY, MODE)
                .saveRecentQuery(q, null)
        fun clear(context: Context) = SearchRecentSuggestions(context, AUTHORITY, MODE)
                .clearHistory()
        const val AUTHORITY = "com.imcorp.SearchPro"
        const val MODE: Int = DATABASE_MODE_QUERIES
    }
}
