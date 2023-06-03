package com.imcorp.animeprog.OneAnimeActivity.Fragments.Comments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.OneAnimeActivity.OneAnimeActivity
import com.imcorp.animeprog.R
import com.imcorp.animeprog.databinding.FragmentAnimeCommentsBinding

//import kotlinx.android.synthetic.main.fragment_anime_comments.*
//import kotlinx.android.synthetic.main.fragment_anime_comments.view.*

class CommentsFragment : Fragment(R.layout.fragment_anime_comments) {
    private val comments get() = (requireActivity() as OneAnimeActivity).oneAnime!!.comments
    private val adapter: CommentsAdapter get() = (binding.commentsRecyclerView.adapter as CommentsAdapter)!!
    private var _binding: FragmentAnimeCommentsBinding? = null
    private val binding: FragmentAnimeCommentsBinding get() = _binding!!
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentAnimeCommentsBinding.bind(view)
        binding.commentsRecyclerView.apply{
            adapter = CommentsAdapter(requireActivity() as MyApp)
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    @SuppressLint("NotifyDataSetChanged")
    public fun updateComments(loaded: Boolean = true){
            if (loaded) {
                binding.loadingProgressBar.visibility = View.GONE
                adapter.comments = ArrayList(comments.commentsList)
                adapter.notifyDataSetChanged()
            } else {
                binding.loadingProgressBar.visibility = View.VISIBLE
                adapter.comments = ArrayList()
                adapter.notifyDataSetChanged()
            }
    }

}