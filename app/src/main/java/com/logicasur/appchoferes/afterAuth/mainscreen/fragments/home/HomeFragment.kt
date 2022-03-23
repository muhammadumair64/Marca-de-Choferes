package com.logicasur.appchoferes.afterAuth.mainscreen.fragments.home

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.databinding.FragmentHomeBinding
import com.logicasur.appchoferes.afterAuth.mainscreen.MainActivity
import com.logicasur.appchoferes.afterAuth.mainscreen.fragments.home.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.logicasur.appchoferes.utils.ResendApis
import com.logicasur.appchoferes.afterAuth.mainscreen.viewModel.MainViewModel
import com.logicasur.appchoferes.afterAuth.mainscreen.fragments.home.adapters.SearchAdapter
import com.logicasur.appchoferes.afterAuth.mainscreen.fragments.home.adapters.StatusAdapter
import com.logicasur.appchoferes.Extra.Language
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.afterAuth.mainscreen.fragments.home.interfaces.OnclickItem
import com.logicasur.appchoferes.common.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.utils.myApplication.MyApplication
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
    var networkAlertDialog: AlertDialog? = null
    lateinit var networkDialogBuilder: AlertDialog.Builder
    lateinit var alertDialog: AlertDialog
    lateinit var dialog: AlertDialog
    lateinit var binding: FragmentHomeBinding
    lateinit var recyclerView: RecyclerView
    lateinit var statusRecyclerView: RecyclerView
    lateinit var searchView: SearchView
    lateinit var proceed_btn: AppCompatButton
    lateinit var cancel_btn: RelativeLayout
    val viewModel: HomeViewModel by viewModels()
    lateinit var mainViewModel: MainViewModel
    lateinit var tinyDB: TinyDB
    lateinit var fragment: HomeFragment
    var mainContext: Context? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val language = Language()
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

    private fun initViews() {
        val context = (activity as MainActivity).context
        mainContext = context
        binding.statusListBtn.visibility = View.GONE
        setColors()
        binding.apply {
            initialState.visibility = View.VISIBLE
            secondState.visibility = View.GONE
        }
        val animation = TransitionInflater.from(requireContext()).inflateTransition(
            R.transition.example_1
        )
        sharedElementEnterTransition = animation


        tinyDB = TinyDB(context)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        mainViewModel.valueReset()
        viewModel.viewsForHomeFragment(context, binding)
        viewModel.initBreakBar()
        viewModel.initWorkBar()
        val intent = (activity as MainActivity)
        intent.viewsOfFragment(binding)
        viewModel.clickListnersForActivityButtons()
        animators()

    }


    //--------------------------------------------Vehicle and status lists---------------------------------
    private fun searchVehicle() {
        val context = (activity as MainActivity).context

        searchAdapter =
            SearchAdapter(viewModel.searchedArrayList, this, viewModel.vehicleArrayListforUpload)
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

    private fun statusShow() {
        statusAdapter = StatusAdapter(viewModel.statusArrayList, this)
        statusRecyclerView.adapter = statusAdapter

    }

    override fun vehicleSelected(position: Int) {
        println("position of holder $position")
        val Position = position.plus(1)
        tinyDB.putInt("vehicle", Position)

        alertDialog.dismiss()
        viewModel.selectVehicle(position)
    }

    override fun statusSelection(position: Int) {
        var selected = tinyDB.getInt("state")
        selected = selected.minus(1)
        Log.d("StatusTesting", "---- $selected-----$position")
        if (selected != position) {
            dialog.dismiss()

            val Position = position.plus(1)
            val previous = tinyDB.getInt("state")
            if (previous != 0) {
                tinyDB.putInt("previous_state", previous)
            }

            tinyDB.putInt("state", Position)
            viewModel.selectState(position)
            viewModel.getDataForStateApi(position)
            viewModel.checkNetConnection()
            (activity as MainActivity).getLocation(requireContext())
        }
    }


//-------------------------------------------------- popups windows   ----------------------------------------------


    private fun createPopup() {
        networkDialogBuilder = AlertDialog.Builder(context)
        val PopupView: View = layoutInflater.inflate(R.layout.item_networkcheck_popup, null)

        Log.d("CallingOpenPop2", "before---")
        if (networkAlertDialog == null) {

            Log.d("CallingOpenPop2", "after---")
            networkAlertDialog = networkDialogBuilder.create()

            proceed_btn = PopupView.findViewById(R.id.proceed_btn)
            cancel_btn = PopupView.findViewById(R.id.cancel_btn)
            if (!MyApplication.checKForPopup) {
                try {
                    viewModel.openPopup(networkAlertDialog!!, PopupView, resources)
                } catch (e: Exception) {
                    Log.d("CallingOpenPop", "Exception...${e.localizedMessage}")
                    LoadingScreen.OnEndLoadingCallbacks?.endLoading("From home fragment")
                }

            } else {
                networkAlertDialog = null
                MyApplication.checKForPopup = false
            }

            (activity as MainActivity).setGrad(
                ResendApis.primaryColor,
                ResendApis.secondaryColor,
                proceed_btn
            )
            cancel_btn.setOnClickListener {
                Log.d("HomeFragment", "Inside acncel btn listner")

                val stateCheck = tinyDB.getBoolean("STATEAPI")
                if (stateCheck) {
                    val position = tinyDB.getInt("previous_state")
                    if (position != 0) {
                        tinyDB.putInt("state", position)
                        viewModel.selectState(position - 1)
                    }

                }
                networkAlertDialog!!.dismiss()
                networkAlertDialog = null

            }
            proceed_btn.setOnClickListener {

                var position = tinyDB.getInt("state")
                if (position != 0) {
                    position = position.minus(1)
                    viewModel.selectState(position)
                }
                val stateCheck = tinyDB.getBoolean("STATEAPI")
                if (stateCheck) {
                    Log.d("APIDATATESTING", "IN IF BLOCK")
                    (activity as MainActivity).updatePendingData(true)
                } else {
                    actionCallOnProceed()
                    viewModel.insertDataWhenOffline()
                    Log.d("APIDATATESTING", "IN Else")
                    (activity as MainActivity).updatePendingData(false)
                }


                networkAlertDialog!!.dismiss()
                networkAlertDialog = null
            }
        }





        if (MyApplication.checKForPopup) {
            closePopup()
            MyApplication.checKForPopup = false
        }


    }

    private fun vehiclePopupWindow() {
        dailogBuilder = AlertDialog.Builder(context)
        statusDailogBuilder = AlertDialog.Builder(context)
        val contactPopupView: View = layoutInflater.inflate(R.layout.vehicle_popup_window, null)
        val statusPopupView: View = layoutInflater.inflate(R.layout.status_popup_window, null)
        alertDialog = dailogBuilder.create()
        dialog = statusDailogBuilder.create()
        binding.vehicleListBtn.setOnClickListener {
            searchAdapter.notifyDataSetChanged()
            alertDialog.setView(contactPopupView)
            alertDialog.show()

            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
            val height = (resources.displayMetrics.heightPixels * 0.60).toInt()
            alertDialog.getWindow()?.setLayout(width, height);
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

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

    private fun closePopup() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {

                if (networkAlertDialog != null) {
                    networkAlertDialog!!.dismiss()
                    Log.d("CallingOpenPop2", "Self dismiss")
                    networkAlertDialog = null
                }

            }
        }
    }

//-----------------------------------------------------Utils---------------------------------------------------


    override fun onResume() {
        super.onResume()
        Log.d("STATE_TESTING", "In On Resume")
        var position = tinyDB.getInt("state")
        if (position != 0) {
            position = position.minus(1)
            viewModel.selectState(position)
        }
        (activity as MainActivity).binding.menu.setItemSelected(R.id.home, true)
    }

    private fun setColors() {
        (activity as MainActivity).setGrad(
            ResendApis.primaryColor,
            ResendApis.secondaryColor,
            binding.secondState
        )
        (activity as MainActivity).setGrad(
            ResendApis.primaryColor,
            ResendApis.secondaryColor,
            binding.TakeBreak
        )
        binding.date.setTextColor(Color.parseColor(ResendApis.primaryColor))
    }


    private fun animators() {
        mainViewModel.popupLiveData.observe(viewLifecycleOwner, {
            if (it != 0) {
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

    }

    fun actionCallOnProceed(){
        (MyApplication.activityContext as MainActivity).clickOnProceed()
    }
}













