package com.imcorp.animeprog.OneAnimeActivity.Fragments.Comments

import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.Default.TextViewImagesAdapter
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.comments.OneComment
import com.imcorp.animeprog.databinding.OneCommentBinding
//import kotlinx.android.synthetic.main.one_comment.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ocpsoft.prettytime.PrettyTime
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class CommentsAdapter(private val myApp: MyApp) : RecyclerView.Adapter<CommentsAdapter.OneCommentItem>() {
    public var comments:ArrayList<OneComment> = ArrayList()
    inner class OneCommentItem(private val v: OneCommentBinding):RecyclerView.ViewHolder(v.root){
        public fun bind(comment:OneComment){
            v.titleTextView.text = comment.user?.title?:""
            v.dateTextView.text = PrettyTime(Locale.getDefault()).format(comment.date)
            v.commentsTextView.text =
                    Html.fromHtml(comment.dataTextHtml,
                            TextViewImagesAdapter(v.commentsTextView,myApp ),
                            null)
            GlobalScope.launch (Dispatchers.IO){
                try {
                    val bitmap = comment.loadUserCoverImage(myApp)
                    GlobalScope.launch(Dispatchers.Main) {
                        v.avatarImageView.setImageBitmap(bitmap)
                    }
                } catch(e: IOException){
                    Log.e("ImageLoadError", e.message, e)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OneCommentItem =
            OneCommentItem(OneCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

    override fun onBindViewHolder(holder: OneCommentItem, position: Int) = holder.bind(comments[position])

    override fun getItemCount() = comments.size
}