package com.imcorp.animeprog.Default

import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html.ImageGetter
import android.util.Log
import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


public class TextViewImagesAdapter(t: View, val c: MyApp) : ImageGetter {
    var container: View = t
    override fun getDrawable(source: String): Drawable {
        val urlDrawable = URLDrawable()
        
        GlobalScope.launch(Dispatchers.IO) {
            try {
                loadImageFromUrl(source, urlDrawable)
            }catch(e: Exception) {
                Log.e("LoadImageCommentEx", e.message, e)
            }
        }
        return urlDrawable
    }

    /***
     * Construct the URLImageParser which will execute AsyncTask and refresh the container
     * @param t
     * @param c
     */

    class URLDrawable : BitmapDrawable() {
        // the drawable that you need to set, you could set the initial drawing
        // with the loading image if you need to
        var drawable: Drawable? = null
        override fun draw(canvas: Canvas) {
            // override the draw to facilitate refresh function later
            drawable?.draw(canvas)
        }
    }
    private fun loadImageFromUrl(url: String, urlDrawable: URLDrawable){
        val bitmap = c.request.loadImageFromUrl(url)
        bitmap?.let {
            val bitmapDr = BitmapDrawable(bitmap)

            GlobalScope.launch(Dispatchers.Default) {
                urlDrawable.drawable = bitmapDr
                urlDrawable.setBounds(0, 0, bitmap.width, bitmap.height)
                this@TextViewImagesAdapter.container.invalidate()
            }
        }
    }
}
