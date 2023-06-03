package com.imcorp.animeprog.Requests.JsonObj

import android.content.Context
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException
import org.jsoup.nodes.Document

interface SimpleAnimeLoader {
    @Throws(InvalidHtmlFormatException::class)
    fun fromHtml(html: Document, path: String, context: Context): OneAnime
}