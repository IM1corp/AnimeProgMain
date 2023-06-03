package com.imcorp.animeprog.MainActivity.fragments.history

import android.view.*
import android.view.View.OnTouchListener
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.imcorp.animeprog.MainActivity.fragments.Adapters.animePreviewItem.OnItemTouchHelper
import com.imcorp.animeprog.MainActivity.fragments.Adapters.animePreviewItem.onItemEdit
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.databinding.TwoLineListItemBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

internal typealias OnHistoryItemAction = (index: Int, item: MutableMap.MutableEntry<Date, OneAnime>) -> Unit
internal typealias OneRecyclerViewItem = MutableMap.MutableEntry<Date, OneAnime>

//class OneRecyclerViewItem(val item: MutableMap.MutableEntry<Date, OneAnime>)
class HistoryRecyclerAdapter(
        private val recyclerview: RecyclerView,
        var onItemClick: OnHistoryItemAction? = null,
): RecyclerView.Adapter<HistoryRecyclerAdapter.OneItem>() {
    public var countOfItems: Int = 0
    private var dataItems: ArrayList<OneRecyclerViewItem> = ArrayList()
    private var onItemDelete:OnHistoryItemAction? = null
    private var onLoadMoreItems: ((Int) -> Unit)?=null
    private val itemTouchHelper: ItemTouchHelper = ItemTouchHelper(OnItemTouchHelper(object : onItemEdit {
        override fun onItemMove(fromPosition: Int, toPosition: Int) {}
        override fun onItemDelete(position: Int) {
            this@HistoryRecyclerAdapter.onItemDelete?.invoke(position, dataItems[position])
            --countOfItems
            dataItems.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount)
            //notifyItemRangeChanged(position,)
        }
    },
            mEnableHorizontal = true, mEnableVertical = false)).apply {
           attachToRecyclerView(recyclerview)
        }
    val items get() = dataItems

    fun setOnItemDeleteListener(function: OnHistoryItemAction) {onItemDelete = function}
    fun setOnLoadMoreItems(f: ((Int)->Unit)?) {onLoadMoreItems=f}
    fun addItems(items: ArrayList<MutableMap.MutableEntry<Date, OneAnime>>) {
        dataItems.addAll(items)
    }
    fun clear() {
        dataItems.clear()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OneItem =
            OneItem(TwoLineListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun getItemCount(): Int = countOfItems
    override fun onBindViewHolder(holder: OneItem, position: Int) {
        if(position >= this.dataItems.size)
            onLoadMoreItems?.invoke(position)?:throw KotlinNullPointerException("onLoadMoreItems is not initilized")
        holder.bind(position)
    }
    inner class OneItem(binding: TwoLineListItemBinding) : RecyclerView.ViewHolder(binding.root), GestureDetector.OnGestureListener, OnTouchListener {
        private var textView1: TextView = binding.text1
        private var textView2: TextView = binding.text2
        private val mGestureDetector: GestureDetector = GestureDetector(recyclerview.context, this)

        private var index:Int = 0
        init {
            binding.root.setOnClickListener { onItemClick?.invoke(index, dataItems[index]) }
        }
        fun bind(position: Int) {
            this.index = position
            with(dataItems[position]){
                textView1.text = value.title
                textView2.text = dateFormat.format(key)
            }
        }

        override fun onDown(e: MotionEvent)=true
        override fun onShowPress(e: MotionEvent) {}
        override fun onSingleTapUp(e: MotionEvent)=true
        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float)=true
        override fun onLongPress(e: MotionEvent)=  itemTouchHelper.startDrag(this)
        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float)=false
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            mGestureDetector.onTouchEvent(event)
            return true
        }

    }
    companion object val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
}
