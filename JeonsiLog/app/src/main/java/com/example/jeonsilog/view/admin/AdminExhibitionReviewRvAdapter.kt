package com.example.jeonsilog.view.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.jeonsilog.databinding.ItemExhibitionReviewBinding

class AdminExhibitionReviewRvAdapter(private val reviewList:List<ReviewModel>):
    RecyclerView.Adapter<AdminExhibitionReviewRvAdapter.RecycleViewHolder>() {
    private var listener: OnItemClickListener? = null

    inner class RecycleViewHolder(private val binding: ItemExhibitionReviewBinding):
        RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int){
            binding.tvUserName.text = reviewList[position].userId.toString()
            binding.ibMenu.setOnClickListener{
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecycleViewHolder {
        val binding = ItemExhibitionReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecycleViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: AdminExhibitionReviewRvAdapter.RecycleViewHolder, position: Int) {
        holder.bind(position)

        if(position != RecyclerView.NO_POSITION){
            holder.itemView.setOnClickListener {
                listener?.onItemClick(holder.itemView, reviewList[position], position)
            }
        }
    }

    override fun getItemCount(): Int = reviewList.size

    interface OnItemClickListener {
        fun onItemClick(v: View, data: ReviewModel, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }
}