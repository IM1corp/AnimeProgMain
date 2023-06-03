package com.imcorp.animeprog.MainActivity.fragments.Adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.annotation.StyleRes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.imcorp.animeprog.Default.LoadImageEvents
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.databinding.BottomSheetMenuBinding
import java.lang.Exception
import kotlin.math.roundToInt

class MenuAnimeItem(private val context: MyApp,
                    @StyleRes res: Int,
                    private val anime:OneAnime.OneAnimeWithId,
                    private val enableDeleting:Boolean=true) :
        BottomSheetDialog(context, res) {
    private var onBtnClick: View.OnClickListener? = null
    private var onDelete: View.OnClickListener? = null
    private val binding: BottomSheetMenuBinding = BottomSheetMenuBinding.inflate(LayoutInflater.from(context))

    init {
//        val view: View = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_menu, bottom_menu)
        setContentView(binding.root)
        this.dismissWithAnimation = true
        addOnProgressImageAnimation()
    }
    private fun addOnProgressImageAnimation(){
        val imageViewToAnimate: ImageView = binding.progressImageView
        val defaultWidth = imageViewToAnimate.layoutParams.width
        val minWidth = (defaultWidth * 2f/3f).roundToInt()
        Int.MAX_VALUE
        this.behavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                imageViewToAnimate.layoutParams.width =
                        (defaultWidth.toFloat()*(1f+slideOffset)).toInt().coerceAtLeast(minWidth)
                imageViewToAnimate.requestLayout()
            }

        })

    }
    fun setEvents(): MenuAnimeItem {
        anime.anime.loadCover(context, context.request, object : LoadImageEvents {
            override fun onSuccess(bitmap: Bitmap?) = binding.imageView.setImageBitmap(bitmap)
            override fun onFail(exception: Exception?): Boolean {
                context.showNoInternetException()
                return false
            }
        })
        binding.titleTextView.text = anime.anime.title
        binding.descriptionTextView.text = anime.anime.description
        if (enableDeleting)
            binding.deleteAction.setOnClickListener { this.dismiss();onDelete?.onClick(null) };
        else binding.deleteAction.visibility = View.GONE
        this.onBtnClick?.let { binding.button.setOnClickListener(it) }
        return this
    }
    fun setOnDelete(onDelete: View.OnClickListener?): MenuAnimeItem {
        this.onDelete = onDelete
        return this
    }
    fun setOnButtonClick(onClick: View.OnClickListener?): MenuAnimeItem {
        this.onBtnClick = onClick
        return this
    }

}