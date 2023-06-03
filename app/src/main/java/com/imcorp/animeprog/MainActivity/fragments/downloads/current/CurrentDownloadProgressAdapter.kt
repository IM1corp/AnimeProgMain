package com.imcorp.animeprog.MainActivity.fragments.downloads.current

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.MainActivity.MainActivity
import com.imcorp.animeprog.databinding.ItemDownloadProgressBinding
import kotlin.math.roundToInt

class CurrentDownloadProgressAdapter(private val activity: MainActivity) : RecyclerView.Adapter<CurrentDownloadProgressAdapter.ProgressItem>(){
    public val data:ArrayList<SmallProgressItem> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ProgressItem(ItemDownloadProgressBinding.inflate(LayoutInflater.from(parent.context), parent,false))
    override fun onBindViewHolder(holder: ProgressItem, position: Int) = holder.bind(position)
    override fun getItemCount()=data.size


    inner class ProgressItem(private val binding: ItemDownloadProgressBinding) : RecyclerView.ViewHolder(binding.root){
        private var positionEl:Int =-1
        private val item get()=data[positionEl]
        fun bind(position: Int) {
            positionEl=position
            update()
        }
        @SuppressLint("SetTextI18n")
        fun update(){

            binding.progressBar2.apply{
                progress= item.currentProgress.toInt()
                max =  item.maxProgress.toInt()
            }
            binding.textViewA.text = Config.getSizeFromByte(item.currentProgress.toDouble())
            binding.textViewB.text = Config.getSizeFromByte(item.maxProgress.toDouble())
            binding.textViewPorc.text =(item.currentProgress.toDouble()/item.maxProgress.coerceAtLeast(1).toDouble()*100.0).roundToInt().toString()+"%"
        }
    }
    class SmallProgressItem(var currentProgress:Long=0,
                            var maxProgress:Long=0)
}