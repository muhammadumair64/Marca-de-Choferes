package com.logicasur.appchoferes.mainscreen.home

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.databinding.FragmentHomeBinding
import com.logicasur.appchoferes.mainscreen.MainActivity
import com.logicasur.appchoferes.mainscreen.home.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.logicasur.appchoferes.Extra.ResendApis
import com.logicasur.appchoferes.mainscreen.viewModel.MainViewModel
import com.logicasur.appchoferes.mainscreen.home.Adapter.SearchAdapter
import com.logicasur.appchoferes.mainscreen.home.Adapter.StatusAdapter
import com.logicasur.appchoferes.Extra.Language
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.myApplication.MyApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.concurrent.schedule


@AndroidEntryPoint
class HomeFragment : Fragment(), OnclickItem {
    lateinit var dismiss: ImageView
    lateinit var searchAdapter: SearchAdapter
    lateinit var statusAdapter: StatusAdapter
    lateinit var dailogBuilder: AlertDialog.Builder
    lateinit var statusDailogBuilder: AlertDialog.Builder
    lateinit var networkAlertDialog: AlertDialog
    lateinit var networkDialogBuilder:AlertDialog.Builder
    lateinit var alertDialog: AlertDialog
    lateinit var dialog: AlertDialog
    lateinit var binding: FragmentHomeBinding
    lateinit var recyclerView: RecyclerView
    lateinit var statusRecyclerView: RecyclerView
    lateinit var searchView: SearchView
    lateinit var proceed_btn  :AppCompatButton
    lateinit var cancel_btn: RelativeLayout
    val viewModel: HomeViewModel by viewModels()
    lateinit var mainViewModel: MainViewModel
    lateinit var tinyDB: TinyDB
    lateinit var fragment : HomeFragment
    var mainContext: Context? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val language = Language()
//        checkLanguage()
        language.setLanguage((activity as MainActivity).baseContext)
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_home, container, false
        )

        val viewBinding = binding.root
        initViews()
        vehiclePopupWindow()
        searchVehicle()
        statusShow()



        return viewBinding

    }

    fun initViews() {
        var context = (activity as MainActivity).context
        mainContext = context
        binding.statusListBtn.visibility = View.GONE
        (activity as MainActivity).setGrad(ResendApis.primaryColor, ResendApis.secondrayColor, binding.secondState)
        (activity as MainActivity).setGrad(ResendApis.primaryColor, ResendApis.secondrayColor, binding.TakeBreak)
        binding.date.setTextColor(Color.parseColor(ResendApis.primaryColor))
        binding.apply {
            initialState?.setVisibility(View.VISIBLE)
            secondState?.setVisibility(View.GONE)
        }
        val animation = TransitionInflater.from(requireContext()).inflateTransition(
            R.transition.example_1
        )
        sharedElementEnterTransition = animation


        tinyDB = TinyDB(context)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        mainViewModel.valueReset()
        viewModel.viewsForHomeFragment(context, binding)
        viewModel.Breakbar()
        viewModel.Workbar()
        var intent = (activity as MainActivity)
        intent.viewsOfFragment(binding)
        viewModel.timers()

        mainViewModel.popupLiveData.observe(viewLifecycleOwner, Observer {
            if(it != 0){
                createPopup()
            }

        })


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

        binding.profileImage.setOnClickListener {
            mainViewModel.navigationLiveData.postValue("2")
        }
        Timer().schedule(1000) {
//            closePopup()
        }

    }

    fun vehiclePopupWindow() {
        dailogBuilder = AlertDialog.Builder(getContext())
        statusDailogBuilder = AlertDialog.Builder(getContext())
        val contactPopupView: View = layoutInflater.inflate(R.layout.vehicle_popup_window, null)
        val statusPopupView: View = layoutInflater.inflate(R.layout.status_popup_window, null)
        alertDialog = dailogBuilder.create()
        dialog = statusDailogBuilder.create()
        binding.vehicleListBtn.setOnClickListener {
            if (binding.StateActive.isVisible) {

            } else if (binding.secondState.text == "End Break" || binding.secondState.text == "Fin del descanso" || binding.secondState.text == "Fim do intervalo") {

            } else {
                searchAdapter.notifyDataSetChanged()
                alertDialog.setView(contactPopupView)
                alertDialog.show()

                val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
                val height = (resources.displayMetrics.heightPixels * 0.60).toInt()
                alertDialog.getWindow()?.setLayout(width, height);
                alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }

        binding.statusListBtn.setOnClickListener {
            if (binding.secondState.text == "End Break" || binding.secondState.text == "Fin del descanso" || binding.secondState.text == "Fim do intervalo") {

                if (binding.secondState.visibility == View.GONE) {
                    statusAdapter.notifyDataSetChanged()
                    dialog.setView(statusPopupView)
                    dialog.show()
                    val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
                    val height = (resources.displayMetrics.heightPixels * 0.45).toInt()
                    dialog.getWindow()?.setLayout(width, height);
                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }

            } else {
                statusAdapter.notifyDataSetChanged()
                dialog.setView(statusPopupView)
                dialog.show()
                val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
                val height = (resources.displayMetrics.heightPixels * 0.45).toInt()
                dialog.getWindow()?.setLayout(width, height);
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
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

        searchAdapter = SearchAdapter(viewModel.searchedArrayList, this,viewModel.vehicleArrayListforUpload)
        recyclerView.adapter = searchAdapter

        val typeface = ResourcesCompat.getFont(context, R.font.open_sans_regular)
        val id: Int = searchView.getContext().getResources()
            .getIdentifier("android:id/search_src_text", null, null)
        val textView = searchView.findViewById(id) as TextView
        textView.hint = getString(R.string.search_here)
        textView.textSize = 20f
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
        statusAdapter = StatusAdapter(viewModel.statusArrayList, this)
        statusRecyclerView.adapter = statusAdapter

    }

    override fun vehicleSelected(position: Int) {
        println("position of holder $position")
        var Position = position.plus(1)
        tinyDB.putInt("vehicle", Position)

        alertDialog.dismiss()
        viewModel.selectVehicle(position)
    }


    override fun statusSelection(position: Int) {
        dialog.dismiss()
        var Position = position.plus(1)
        var previous=tinyDB.getInt("state")
        if(previous!=0){
            tinyDB.putInt("previous_state",previous)
        }


        tinyDB.putInt("state", Position)
//        viewModel.selectState(position)
        viewModel.hitStateAPI(position)
        (activity as MainActivity).getLocation(requireContext())

    }


    fun checkLanguage() {
        if (MyApplication.checkForLanguageChange == 200) {
            MyApplication.checkForLanguageChange = 0
            mainViewModel.navigationLiveData.postValue("3")
        }


    }

    override fun onResume() {
        super.onResume()
        var position= tinyDB.getInt("state")
        if(position!=0){
            position = position.minus(1)
            viewModel.selectState(position)
        }
        (activity as MainActivity).binding.menu.setItemSelected(R.id.home, true)
    }

    fun createPopup(){
        networkDialogBuilder = AlertDialog.Builder(getContext())
        val PopupView: View = layoutInflater.inflate(R.layout.item_networkcheck_popup, null)
        networkAlertDialog= networkDialogBuilder.create()
        proceed_btn=PopupView.findViewById(R.id.proceed_btn)
        cancel_btn=PopupView.findViewById(R.id.cancel_btn)
        if(!MyApplication.checKForPopup){
            viewModel.openPopup(networkAlertDialog,PopupView,resources)

        }else{
            MyApplication.checKForPopup = false
        }

        (activity as MainActivity).setGrad(ResendApis.primaryColor, ResendApis.secondrayColor, proceed_btn)
        cancel_btn.setOnClickListener {

            networkAlertDialog.dismiss()
            var stateCheck=  tinyDB.getBoolean("STATEAPI")
            if(stateCheck){
                var position=tinyDB.getInt("previous_state")
                if(position!=0){
                    tinyDB.putInt("state",position)
                }

            }

        }
        proceed_btn.setOnClickListener {

            var position= tinyDB.getInt("state")
            if(position!=0){
                position = position.minus(1)
                viewModel.selectState(position)
            }
          var stateCheck=  tinyDB.getBoolean("STATEAPI")
            if(stateCheck){
                (activity as MainActivity).updatePendingData(true)
            }else{
                (activity as MainActivity).updatePendingData(false)
            }


            networkAlertDialog.dismiss()
        }

        if(MyApplication.checKForPopup == true){
            closePopup()
            MyApplication.checKForPopup = false
        }

    }



fun closePopup(){
            lifecycleScope.launch {
            withContext(Dispatchers.IO){

if(networkAlertDialog != null){
    networkAlertDialog.dismiss()
}

                }
            }
        }



}














