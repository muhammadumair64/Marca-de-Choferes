package com.example.marcadechoferes.mainscreen.home

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.example.marcadechoferes.R
import com.example.marcadechoferes.databinding.FragmentHomeBinding
import com.example.marcadechoferes.mainscreen.MainActivity
import com.example.marcadechoferes.mainscreen.home.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.marcadechoferes.mainscreen.viewModel.MainViewModel
import com.example.marcadechoferes.mainscreen.home.Adapter.SearchAdapter
import com.example.marcadechoferes.mainscreen.home.Adapter.StatusAdapter


@AndroidEntryPoint
class HomeFragment : Fragment(),OnclickItem {
    lateinit var dismiss: ImageView
    lateinit var searchAdapter: SearchAdapter
    lateinit var statusAdapter: StatusAdapter
    lateinit var dailogBuilder: AlertDialog.Builder
    lateinit var statusDailogBuilder: AlertDialog.Builder
    lateinit var alertDialog: AlertDialog
    lateinit var dialog: AlertDialog
    lateinit var binding: FragmentHomeBinding
    lateinit var recyclerView: RecyclerView
    lateinit var statusRecyclerView: RecyclerView
    lateinit var searchView: SearchView
    val viewModel: HomeViewModel by viewModels()
    lateinit var mainViewModel: MainViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_home, container, false
        )

        val viewBinding = binding.root
        initViews()
        viewModel.dammyData()
        vehiclePopupWindow()
        searchVehicle()
        statusShow()


        return viewBinding

    }

    fun initViews() {
        binding.statusListBtn.visibility=View.GONE
        binding.apply {
            initialState?.setVisibility(View.VISIBLE)
            secondState?.setVisibility(View.GONE)
        }
        val animation = TransitionInflater.from(requireContext()).inflateTransition(
            R.transition.example_1
        )
        sharedElementEnterTransition = animation

        var context = (activity as MainActivity).context
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        viewModel.viewsForHomeFragment(context, binding)
        viewModel.Breakbar()
        viewModel.Workbar()
        var intent = (activity as MainActivity)
        intent.viewsOfFragment(binding)
        viewModel.timers()

        mainViewModel.navigationLiveData.observe(
            viewLifecycleOwner, androidx.lifecycle.Observer {
                println("Live Data $it")
                if (it == "2") {
                    val extras = FragmentNavigatorExtras(
                        binding.profileImage to "image_big",
                        binding.namelayout to "large_name_layout",
                        binding.dateLayout to "large_date"
                    )
                    findNavController().navigate(
                        R.id.action_homeFragment_to_profileFragment,
                        null,
                        null,
                        extras
                    )

                } else if (it == "3") {
                    findNavController().navigate(
                        R.id.action_homeFragment_to_configurationFragment, null, null

                    )
                }

            }
        )


    }

    fun vehiclePopupWindow() {
        dailogBuilder = AlertDialog.Builder(getContext())
        statusDailogBuilder = AlertDialog.Builder(getContext())
        val contactPopupView: View = layoutInflater.inflate(R.layout.vehicle_popup_window, null)
        val statusPopupView: View = layoutInflater.inflate(R.layout.status_popup_window, null)
        alertDialog = dailogBuilder.create()
        dialog = statusDailogBuilder.create()
        binding.vehicleListBtn.setOnClickListener {
            searchAdapter.notifyDataSetChanged()
            alertDialog.setView(contactPopupView)
            alertDialog.show()
            alertDialog.getWindow()?.setLayout(650, 800);
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        }

        binding.statusListBtn.setOnClickListener {
            statusAdapter.notifyDataSetChanged()
            dialog.setView(statusPopupView)
            dialog.show()
            dialog.getWindow()?.setLayout(650, 750);
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        statusRecyclerView = statusPopupView.findViewById(R.id.status_RV)
        searchView = contactPopupView.findViewById<SearchView>(R.id.searchText)
        recyclerView = contactPopupView.findViewById(R.id.vehiclesRecyclerView)
        dismiss = statusPopupView.findViewById(R.id.close)


        dismiss.setOnClickListener {
            dialog.dismiss()
        }


    }

    fun searchVehicle() {
        var context = (activity as MainActivity).context

        searchAdapter = SearchAdapter(viewModel.searchedArrayList,this)
        recyclerView.adapter = searchAdapter

        val typeface = ResourcesCompat.getFont(context, R.font.open_sans_regular)
        val id: Int = searchView.getContext().getResources()
            .getIdentifier("android:id/search_src_text", null, null)
        val textView = searchView.findViewById(id) as TextView
        textView.hint = "Buscar vehículo aquí ..."
        textView.typeface = typeface

        searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchAdapter.getFilter().filter(newText)
                println("New text${newText.length}")
                return false
            }
        })


    }

    fun statusShow() {

        viewModel.statusRV()
        statusAdapter = StatusAdapter(viewModel.statusArrayList,this)
        statusRecyclerView.adapter = statusAdapter

    }




    override fun vehicleSelected(position: Int) {
        alertDialog.dismiss()
       var text= viewModel.searchedArrayList[position]
        println("selected text $text")
        binding.apply {
            vehicleListBtn.setBackgroundResource(R.drawable.bg_selectedvehicleback)
            iconCar.setImageResource(R.drawable.ic_white_car)
            vehicleNameSelected.setTextColor(Color.WHITE)
            vehicleNameSelected.text = text
            Arrow.visibility = View.GONE
            dots.visibility=View.VISIBLE
           statusListBtn.visibility=View.VISIBLE

           initialState?.setVisibility(View.GONE)
           secondState?.setVisibility(View.VISIBLE)
        }
    }

    override fun statusSelection(position: Int) {
       dialog.dismiss()
        var text= viewModel.statusArrayList[position]
        binding.statusSelected.text = text
    }
}











