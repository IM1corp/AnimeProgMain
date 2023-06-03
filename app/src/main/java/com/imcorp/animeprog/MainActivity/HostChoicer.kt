package com.imcorp.animeprog.MainActivity

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.RadioGroup
import androidx.annotation.StyleRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.forEach
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.R
import com.imcorp.animeprog.databinding.BottomChoiceHostMenuBinding
import com.imcorp.animeprog.databinding.OneHostItemBinding

public class HostChoicer(private val context: MyApp,private val isMainPage:Boolean, @StyleRes res:Int=R.style.BottomSheetMenu) :
        BottomSheetDialog(context,res) {
    private var binding: BottomChoiceHostMenuBinding = BottomChoiceHostMenuBinding.inflate(LayoutInflater.from(context))
    private val view:View = binding.root //,R.layout.bottom_choice_host_menu, binding.hostMenu)
    init {
        setContentView(view)
        this.dismissWithAnimation = true
    }
    fun setEvents(onDismiss: () -> Unit): HostChoicer{
        this.setOnDismissListener { onDismiss() }
        with(binding.choiceHostRecycleView){
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
            adapter = HostsAdapter()
        }
        if(!isMainPage) binding.titleTextView.setText(R.string.choice_host_search)

        return this
    }
    override fun show() {
        super.show()
        BottomSheetBehavior.from(
                this.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)!!
        ).state = BottomSheetBehavior.STATE_EXPANDED
    }
    public inner class HostsAdapter : RecyclerView.Adapter<HostsAdapter.Item>() {
        private val items:List<ItemStruct> = Config.HOSTS.map{
            ItemStruct(it.value,Config.getIconByHost(it.key),it.key)
        }

        public inner class Item(private val binding: OneHostItemBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
            private var itemPosition:Int=0
            private val item get() = items[itemPosition]
            fun bind(position: Int){
                itemPosition = position
                binding.radioButton.isChecked = item.isSelected

                binding.hostTitleTextView.text = item.title
                binding.imageView.setImageResource(item.icon)
                itemView.setOnClickListener (this)
                if(!item.isSelected) binding.radioButton.setOnCheckedChangeListener { buttonView, _ -> this.onClick(buttonView) }
            }
            override fun onClick(v: View) {
                (this@HostChoicer.binding.choiceHostRecycleView.findViewHolderForAdapterPosition(items.indexOfFirst{ it.isSelected }) as Item)
                    .binding.radioButton.isChecked = false

//                this@HostChoicer.binding.choiceHostRecycleView.layoutManager?.findViewByPosition()
//                    ?.radioButton?.isChecked = false
                binding.radioButton.isChecked=true
                this.item.selectIT()
                v.postDelayed(this@HostChoicer::dismiss, context.resources.getInteger(R.integer.hostChoiceWaitTime).toLong())
            }
        }
        public inner class ItemStruct(val title:CharSequence,val icon:Int,private val id:Byte ) {
            val isSelected: Boolean get() = (if(isMainPage)context.dataBase.settings.mainHost else context.dataBase.settings.searchHost) == id
            fun selectIT() {
                if (!isMainPage) context.dataBase.settings.searchHost = id
                else context.dataBase.settings.mainHost = id
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Item =
            Item(OneHostItemBinding.inflate(layoutInflater, parent,false))
        override fun onBindViewHolder(holder: Item, position: Int) = holder.bind(position)
        override fun getItemCount(): Int = items.size
    }
}
