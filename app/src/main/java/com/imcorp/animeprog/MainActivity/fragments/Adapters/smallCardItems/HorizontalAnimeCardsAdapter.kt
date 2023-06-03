package com.imcorp.animeprog.MainActivity.fragments.Adapters.smallCardItems

import android.graphics.Bitmap
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.text.bold
import androidx.recyclerview.widget.RecyclerView
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Default.LoadImageEvents
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.MainActivity.fragments.Adapters.MenuAnimeItem
import com.imcorp.animeprog.MainActivity.fragments.Adapters.smallCardItems.HorizontalAnimeCardsAdapter.CardItemModel
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.Requests.JsonObj.OneAnime.OneAnimeWithId
import java.util.*
import kotlin.reflect.KFunction1

class HorizontalAnimeCardsAdapter(private val mContext: MyApp, private val showMenuButton: Boolean) : RecyclerView.Adapter<CardItemModel>() {
    private val items = ArrayList<OneAnime>()
    private var onClickListener: KFunction1<OneAnime, Unit>? = null
    private var hasDataText:Boolean = false;
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardItemModel = CardItemModel(
            LayoutInflater.from(parent.context)
                    .inflate(R.layout.horizontal_card_item, parent, false)
    )
    override fun onBindViewHolder(holder: CardItemModel, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size

    fun addItems(items: List<OneAnime>?): HorizontalAnimeCardsAdapter {
        if (!items.isNullOrEmpty()) {
            this.items.addAll(items)
            hasDataText=this.items.any{it.posterExists()};
            notifyDataSetChanged() //TODO:(from,this.items.size());
        }
        return this
    }
    fun clear(): HorizontalAnimeCardsAdapter {
        items.clear()
        notifyDataSetChanged()
        return this
    }
    fun setOnClickListener(onClickListener: KFunction1<OneAnime, Unit>): HorizontalAnimeCardsAdapter {
        this.onClickListener = onClickListener
        return this
    }

    inner class CardItemModel(itemView: View) : RecyclerView.ViewHolder(itemView), LoadImageEvents, View.OnClickListener {
        private val titleTextView: TextView = itemView.findViewById(R.id.cardTitleTextView)
        private val dataTextView: TextView = itemView.findViewById(R.id.dataTextView)
        private val imageView: androidx.appcompat.widget.AppCompatImageView = itemView.findViewById(R.id.imageView)
        private val menuButton: ImageButton = itemView.findViewById(R.id.menuButton)
        private lateinit var anime: OneAnime
        fun bind(anime: OneAnime) {
            if(this::anime.isInitialized)this.anime.removeLoadImageCallbacks()
            fun getDataText() : CharSequence{
                return if(anime.posterExists()){
                    SpannableStringBuilder(anime.dataPosterText?.subTitle ?: "")
                            .append('\n')
                            .bold {
                                append(anime.dataPosterText.mainTitle)
                            }
                } else ""// mContext.getString(R.string.no_description)

            }
            this.anime = anime
            imageView.setImageResource(0)//mContext.resources.getColor(R.color.noImageColor))
            if(anime.cover.isNullOrBlank()) Thread{
                try {
                    val a = mContext.request.loadAnimeFromPath(anime.path, anime.HOST.toInt())
                    if(this.anime == a) {
                        this.anime = a
                        mContext.threadCallback.post(::updateCover)
                    }
                }catch (e: Exception){
                    if (Config.NEED_LOG) Log.e(Config.LOG_TAG, e.message, e)
                }
            }.start()
            else updateCover()
            titleTextView.text = anime.title

            if(hasDataText)
                dataTextView.text = getDataText()
            else itemView.layoutParams = itemView.layoutParams.apply{height= Config.dpToPix(mContext, 250)}
            itemView.setOnClickListener(this)
            if (showMenuButton) {
                menuButton.visibility = View.VISIBLE
                menuButton.setOnClickListener(::showMenu)
                itemView.setOnLongClickListener { showMenu(null);false }
            } else menuButton.visibility = View.GONE
        }
        private fun updateCover() =anime.loadCover(mContext, mContext.request, this)
        override fun onClick(v: View) {
            onClickListener?.let {
                it(anime)
            }
        }

        override fun onSuccess(bitmap: Bitmap) = imageView.setImageBitmap(bitmap)
        override fun onFail(exception: Exception): Boolean {
            //if (events != null) events.onErrorLoadingImage(exception);
            //TODO: send better error responding
            return false
        }

        private fun showMenu(_view: View?) = MenuAnimeItem(mContext, R.style.BottomSheetMenu, OneAnimeWithId(0, anime), false)
                .setOnButtonClick(this)
                .setEvents()
                .show()

    }
}