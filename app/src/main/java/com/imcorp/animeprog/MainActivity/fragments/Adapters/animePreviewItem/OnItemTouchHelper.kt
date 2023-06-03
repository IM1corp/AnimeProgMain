package com.imcorp.animeprog.MainActivity.fragments.Adapters.animePreviewItem

import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.imcorp.animeprog.Default.getColorAttr
import com.imcorp.animeprog.R

class OnItemTouchHelper(private val mAdapter: onItemEdit, private val mEnableHorizontal: Boolean,
                        private val mEnableVertical: Boolean) : ItemTouchHelper.Callback() {
    private var backgroundColor: Drawable? = null
    override fun isLongPressDragEnabled() = true
    override fun isItemViewSwipeEnabled() = true

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        //On start moving
        backgroundColor?.let{
            viewHolder.itemView.background = it
        }
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            //onEndMoving
            backgroundColor = viewHolder!!.itemView.background
            viewHolder.itemView.setBackgroundColor(viewHolder.itemView.context.getColorAttr(R.attr.dragElementColor))
        }
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = if (mEnableVertical) ItemTouchHelper.UP or ItemTouchHelper.DOWN else 0
        val swipeFlags = if (mEnableHorizontal) ItemTouchHelper.START or ItemTouchHelper.END else 0
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = mAdapter.onItemDelete(viewHolder.adapterPosition)
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        mAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

}