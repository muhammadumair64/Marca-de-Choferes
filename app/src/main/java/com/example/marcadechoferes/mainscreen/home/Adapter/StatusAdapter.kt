package com.example.marcadechoferes.mainscreen.home.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.marcadechoferes.R
import com.example.marcadechoferes.databinding.ItemStatusListBinding
import com.example.marcadechoferes.mainscreen.home.OnclickItem

class StatusAdapter (var statusArrayList: ArrayList<String>, val onclickItem: OnclickItem):
    RecyclerView.Adapter<StatusAdapter.StatusViewHolder>(){



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StatusAdapter.StatusViewHolder = StatusViewHolder(
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_status_list,
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: StatusAdapter.StatusViewHolder, position: Int) {
        holder.statusBinding.searchedText.text = statusArrayList[position]
        holder.statusBinding.searchedText.setTextColor(Color.parseColor("#C6C6C6"))
        holder.statusBinding.close.setBackgroundResource(R.drawable.ic_check_circle)
        holder.statusBinding.searchedText.setTextColor(Color.parseColor("#C6C6C6"))
        holder.statusBinding.close.setBackgroundResource(R.drawable.ic_check_circle)

        holder.statusBinding.status.setOnClickListener {

            holder.statusBinding.searchedText.setTextColor(Color.BLACK)
            holder.statusBinding.close.setBackgroundResource(R.drawable.ic_blue_check)
            onclickItem.statusSelection(position)
        }

    }

    override fun getItemCount(): Int {
        return statusArrayList.size
    }

    class StatusViewHolder(val statusBinding: ItemStatusListBinding):
        RecyclerView.ViewHolder(statusBinding.root){

    }
}