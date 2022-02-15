package com.logicasur.appchoferes.mainscreen.home.Adapter

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.logicasur.appchoferes.Extra.ResendApis
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.databinding.ItemVehiclelistBinding
import com.logicasur.appchoferes.mainscreen.home.OnclickItem
import com.logicasur.appchoferes.myApplication.MyApplication
import com.logicasur.appchoferes.network.signinResponse.Vehicle

class SearchAdapter(var vehicleSearchArrayList: ArrayList<String>,var onItemClicked:OnclickItem,var vehicleList:ArrayList<Vehicle>):
    RecyclerView.Adapter<SearchAdapter.searchViewHolder>(), Filterable {
    lateinit var tinyDB: TinyDB
    var allVehicleArrayList:ArrayList<String> = ArrayList()
    var charactersLength=0
    init {

        allVehicleArrayList.addAll(vehicleSearchArrayList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): searchViewHolder =
        searchViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
               R.layout.item_vehiclelist,
                parent,
                false
            )
        )
    override fun onBindViewHolder(holder: searchViewHolder, position: Int) {
        tinyDB= TinyDB(MyApplication.appContext)
        holder.adapterViewBindingAdapter.searchedText.text = vehicleSearchArrayList[position]
        var searchText=holder.adapterViewBindingAdapter.searchedText.text.toString()
        val spannable: Spannable = SpannableString(searchText)
        val fcsBlack = ForegroundColorSpan(Color.BLACK)
        spannable.setSpan(fcsBlack, 0, charactersLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        holder.adapterViewBindingAdapter.searchedText.text=spannable
        holder.adapterViewBindingAdapter.searchedText.setTextColor(Color.parseColor("#C6C6C6"))
        holder.adapterViewBindingAdapter.checkbg.setBackgroundColor(Color.parseColor("#979797"))
        var selected = tinyDB.getInt("vehicle")
    selected=selected.minus(1)
    if(selected==position){
        holder.adapterViewBindingAdapter.searchedText.setTextColor(Color.BLACK)
        holder.adapterViewBindingAdapter.checkbg.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))
    }
        holder.adapterViewBindingAdapter.vehicle.setOnClickListener {
            Log.d("VEHICALTESTING","${holder.adapterViewBindingAdapter.searchedText.text}")
            var vehicle = holder.adapterViewBindingAdapter.searchedText.text
            vehicle.trim()
            var selectedVehicle= vehicleList[position]
            selectedVehicle.id
            tinyDB.putInt("lastVehicleid",selectedVehicle.id)

//             var plateNumber = vehicle.split(" - ").toTypedArray()[0]
//            if(plateNumber.isEmpty()){
//                var discr= vehicle.split(" - ").toTypedArray()[0]
//            }
//            for(item in vehicleList){
//                if(item.plateNumber == plateNumber){
//                    Log.d("VEHICALTESTING","${item.id}")
//                    tinyDB.putInt("lastVehicleid",item.id.toInt())
//                }
//            }

            onItemClicked.vehicleSelected(position)
            holder.adapterViewBindingAdapter.searchedText.setTextColor(Color.BLACK)
            holder.adapterViewBindingAdapter.checkbg.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))
            println("holder position $holder")
        }

    }

    override fun getItemCount(): Int {
        return vehicleSearchArrayList.size
    }

    class searchViewHolder(val adapterViewBindingAdapter: ItemVehiclelistBinding)
        : RecyclerView.ViewHolder(adapterViewBindingAdapter.root){


    }

    override fun getFilter(): Filter {

        return myFilter
    }
    var myFilter: Filter = object : Filter() {
        //Automatic on background thread
        override fun performFiltering(charSequence: CharSequence): FilterResults {
            val filteredList: ArrayList<String> = ArrayList()
            println("CharSequence${charSequence.length}")
            charactersLength=charSequence.length
            if (charSequence == null || charSequence.length == 0) {
                filteredList.addAll(allVehicleArrayList)
            } else {
                for (searches in allVehicleArrayList) {
                    if (searches.toString().toLowerCase().contains(charSequence.toString().toLowerCase())) {

                        filteredList.add(searches)
                    }
                }
            }
            val filterResults = FilterResults()
            filterResults.values = filteredList
            return filterResults
        }

        //Automatic on UI thread
        override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
            vehicleSearchArrayList.clear()
            vehicleSearchArrayList.addAll(filterResults.values as Collection<String>)
            notifyDataSetChanged()
        }
    }




}