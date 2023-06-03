package com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeData

import android.animation.ObjectAnimator
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.*
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Default.LoadImageEvents
import com.imcorp.animeprog.OneAnimeActivity.Fragments.MyFragment
import com.imcorp.animeprog.OneAnimeActivity.OneAnimeActivity
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.databinding.FragmentOneAnimeDataBinding
import com.imcorp.animeprog.databinding.FragmentSearchBinding
import java.io.IOException
import java.util.*


class FragmentOneAnimeData : Fragment(), MyFragment {
    private val onClick: OnUrlClicked = object : OnUrlClicked {
        override fun onClick(type: UrlType, path: String?) {
            activity.onPathClicked(type, path)
        }
    }
    private var _viewBinding: FragmentOneAnimeDataBinding? = null
    public val viewBinding: FragmentOneAnimeDataBinding get() = _viewBinding!!

    private lateinit var viewingOrderAdapter: FragmentAnimeViewingOrderAdapter
    private var anime: OneAnime? = null
    private val activity: OneAnimeActivity get() = getActivity() as OneAnimeActivity
    private var expanded=false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentOneAnimeDataBinding.inflate(inflater, container, false).also{_viewBinding=it}.root

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(viewBinding.viewingOrderRecycleView) {
            adapter = FragmentAnimeViewingOrderAdapter(
                    activity,
                    ArrayList()
            ) {
                onClick.onClick(UrlType.ANIME, it.animeURI)
            }.also { viewingOrderAdapter = it }
            layoutManager = LinearLayoutManager(activity)
        }
        for(i in arrayOf(viewBinding.animeTitle)) //, animeDescription
            Config.copyOnLongPress(activity, i)
        anime?.let{ setData(it) }
        viewBinding.animeCover.setOnClickListener { showImage() }
    }
    private fun showImage() {
        class ScaleListener(private val imageView: ImageView) : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            private var mScaleFactor = 1.0f
            override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
                mScaleFactor *= scaleGestureDetector.scaleFactor
                mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f))
                imageView.scaleX = mScaleFactor
                imageView.scaleY = mScaleFactor
                return true
            }
        }
        val imageView = ImageView(activity).apply {
            val detector  = ScaleGestureDetector(context, ScaleListener(this))
            setOnTouchListener { v, event ->detector.onTouchEvent(event) }
        }
        val dialog = Dialog(activity)
        val loadingThread = Thread{
            try {
                val image = anime!!.getBigCover(activity)
                activity.threadCallback.post {
                    imageView.setImageBitmap(image)
                    imageView.scaleType = ImageView.ScaleType.FIT_XY
                }
            }catch (e: Exception){
                activity.threadCallback.post {
                    dialog.addContentView(TextView(activity).apply { text = getString(R.string.no_internet) },//TODO: set good error
                            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                }
            }
        }
        loadingThread.start()
        with(dialog){
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setOnDismissListener {
                loadingThread.interrupt()
            }
            addContentView(imageView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            show()
        }
    }
    override fun setData(anime: OneAnime) {
        this.anime = anime.apply {
            fun setTitleDescriptionLayout() {
                viewBinding.animeTitle.text = title
                viewBinding.animeDescription.text = description
                loadCover(activity, activity.request, object : LoadImageEvents {
                    override fun onSuccess(bitmap: Bitmap) = viewBinding.animeCover.setImageBitmap(bitmap)
                    override fun onFail(exception: Exception): Boolean {
                        if (exception is IOException) {
                            viewBinding.animeCover.setImageResource(R.drawable.ic_no_internet)
                            activity.showNoInternetException()
                        }
                        return false;
                    }
                })
            }
            fun setViewingOrder() {
                if (viewingOrder == null) {
                    viewBinding.viewingOrderLayout.visibility = View.GONE
                } else {
                    viewBinding.viewingOrderLayout.visibility = View.VISIBLE
                    viewingOrderAdapter.animes.addAll(viewingOrder)
                    viewingOrderAdapter.notifyDataSetChanged()
                }
            }
            fun setShowMoreDesc() =  with(viewBinding.showMoreDescription) {
                val maxLines = context.resources.getInteger(R.integer.maxLines);
                post{
                    visibility = if(viewBinding.animeDescription.lineCount>context.resources.getInteger(R.integer.maxLines))
                        View.VISIBLE else View.GONE
                }
                setOnClickListener {
                    ObjectAnimator.ofInt(
                        viewBinding.animeDescription,
                            "maxLines",
                            if (expanded) context.resources.getInteger(R.integer.maxLines) else viewBinding.animeDescription.lineCount)
                            .apply { duration=((viewBinding.animeDescription.lineCount-maxLines)*context.resources.getInteger(R.integer.maxLinesAnimation)/6).toLong(); }
                            .start()
                    expanded = !expanded
                    setText(if (expanded) R.string.show_less else R.string.show_more)
                }
            }
            view?.run {
                visibility = View.VISIBLE
                setTitleDescriptionLayout()
                setViewingOrder()
                setShowMoreDesc()
                setDataItems(this@apply)
            }
        }

    }
    private fun setDataItems(animeM: OneAnime) {
        val anime = animeM.attrs
        val offsetPaddingL = Config.dpToPix(context, 15)
        val offsetPaddingR = Config.dpToPix(context, 5)
        val minTitleWidth = Config.dpToPix(context, 115)
        with(viewBinding.dataLayout) {
            fun addItem(@StringRes stringId: Int, container: TextView? = null, text: CharSequence? = null,
                        bold: Boolean = false, showScrollLine: Boolean = false, allowCopy: Boolean = true,
                        then: ((container: LinearLayout) -> Unit)? = null) = addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                addView(TextView(context).apply {
                    setText(stringId);
                    minWidth = minTitleWidth;
                })
                val scrollView = HorizontalScrollView(context).apply {
                    isHorizontalScrollBarEnabled = showScrollLine
                    setPadding(offsetPaddingL, 0, offsetPaddingR, 0)
                }
                scrollView.addView(if (then != null) LinearLayout(context).also(then) else container
                        ?: TextView(context).apply {
                            this.text = text
                            if (bold) setTypeface(typeface, Typeface.BOLD)
                            gravity = Gravity.START or Gravity.CENTER_VERTICAL
                            if (allowCopy) Config.copyOnLongPress(activity, this)
                        })
                addView(scrollView)
            })
            anime.synonyms?.let { list ->
                val text = SpannableString(list.joinToString() + "\n");
                var offset = 0;
                for (item in list) {
                    offset += item.length + 2;
                    text.setSpan(StyleSpan(Typeface.BOLD), offset - 2, offset - 1, 0);
                }
                addItem(R.string.synonyms, text = text, showScrollLine = true)
            }
            animeM.status?.let {
                addItem(R.string.status, text = it, bold = true)
            }
            animeM.year.run {
                addItem(R.string.year_1, text = this.toString(), bold = true, allowCopy = false)
            }
            animeM.animeType?.let{ type->
                addItem(R.string.anime_type, text = type, allowCopy = false)
            }
            anime.genres?.let { list ->
                addItem(R.string.genres) { container ->
                    for ((i, item) in list.withIndex()) {
                        container.addView(Config.getLinkTextView(context, item.title) {
                            onClick.onClick(UrlType.GENRE, item.link)
                        })
                        if (i != list.size - 1)
                            container.addView(TextView(context).apply { text = ", " })
                    }
                }
            }
            anime.rating?.let {
                addItem(R.string.rating, text = it, bold = true, allowCopy = false)
            }
            if (anime.epCount != 0) addItem(R.string.episodes_count, text = anime.epCount.toString(), bold = true, allowCopy = false)
            anime.producer?.let { producer ->
                addItem(R.string.producer, Config.getLinkTextView(context, producer.title) {
                    onClick.onClick(UrlType.PRODUCER, producer.link)
                })
            }
            anime.studio?.let { studio ->
                addItem(R.string.studio, Config.getLinkTextView(context, studio.title) {
                    onClick.onClick(UrlType.STUDIO, studio.link)
                })
            }
            anime.issueDate?.let{
                addItem(R.string.issueDate, text = anime.issueDateFormatted)
            }
            anime.originalSource?.let {
                addItem(R.string.originalSource, text = it)
            }
        }
    }
    interface OnUrlClicked {
        fun onClick(type: UrlType, path: String?)
    }
    enum class UrlType {
        ANIME, AUTHOR, STUDIO, GENRE, PRODUCER
    }
}