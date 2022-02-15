package com.logicasur.appchoferes.mainscreen.configuration

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.logicasur.appchoferes.Extra.CheckConnection
import com.logicasur.appchoferes.Extra.ResendApis
import com.logicasur.appchoferes.Extra.Language
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.databinding.FragmentConfigurationBinding
import com.logicasur.appchoferes.mainscreen.MainActivity
import com.logicasur.appchoferes.mainscreen.configuration.viewModel.ConfigurationViewModel
import com.logicasur.appchoferes.mainscreen.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class ConfigurationFragment : Fragment() {
    val viewModel: ConfigurationViewModel by viewModels()
    lateinit var dailogBuilder: AlertDialog.Builder
    lateinit var alertDialog: AlertDialog
    lateinit var mainViewModel: MainViewModel
    lateinit var binding: FragmentConfigurationBinding
    lateinit var dismiss: ImageView
    lateinit var tinyDB: TinyDB
    @Inject
    lateinit var resendApis: ResendApis

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val language= Language()
        language.setLanguage((activity as MainActivity).baseContext)
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_configuration, container, false
        )
        tinyDB = TinyDB(context)
        setLangText()
        tinyDB.putBoolean("reload",false)
        var viewBinding = binding.root
        initViews()
        openPopupWindow()
        customeColor()
        return viewBinding

    }

    fun initViews() {
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        (activity as MainActivity).binding.menu.setItemSelected(R.id.Settings,true)
        mainViewModel.navigationLiveData.observe(viewLifecycleOwner, Observer {
            if (it == "1") {
                findNavController().navigate(
                    R.id.action_configurationFragment_to_homeFragment, null, null

                )

            } else if (it == "2") {
                findNavController().navigate(
                    R.id.action_configurationFragment_to_profileFragment, null, null

                )
            }


        }
        )
    }


    fun openPopupWindow() {
        dailogBuilder = AlertDialog.Builder(getContext())
        val contactPopupView: View = layoutInflater.inflate(R.layout.langauge_popup_window, null)
        alertDialog = dailogBuilder.create()

        var context = (activity as MainActivity).context
        viewModel.viewsForConfigurationFragment(context, binding,alertDialog,contactPopupView)

        binding.languageLayout.setOnClickListener {


            lifecycleScope.launch {
                withContext(Dispatchers.IO){
                    val check = CheckConnection.netCheck(context)
                    withContext(Dispatchers.Main){
                        if(check){
                            alertDialog.setView(contactPopupView)
                            viewModel.selectedLanguage()
                            alertDialog.show()
                            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
                            val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
                            alertDialog.getWindow()?.setLayout(width, height)
                            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        }else{
                            Toast.makeText(context, viewModel.TAG2, Toast.LENGTH_SHORT).show()
                        }
                    }

                }

            }



        }
        dismiss = contactPopupView.findViewById(R.id.close)
        dismiss.setOnClickListener {
            alertDialog.dismiss()
        }

    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).binding.menu.setItemSelected(R.id.Settings, true)
       var reload= tinyDB.getBoolean("reload")
        if(reload) {
            val id = findNavController().currentDestination?.id
            findNavController().popBackStack(id!!, true)
            findNavController().navigate(id)
        }
    }


    fun setLangText(){
        var language = tinyDB.getString("language")
        if (language == "0") {

            binding.languageNameInitails.text = " (ESP)"

        } else if (language == "1") {
            var eng =" (ENG)"
            binding.languageNameInitails.text = "$eng"


        } else {
           binding.languageNameInitails.text = " (POR)"

        }


    }


    fun customeColor(){
        binding.upperLayoutBack!!.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))
        binding.arrowBack1!!.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))
        binding.arrowBack2!!.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))
        binding.togglebg!!.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))
    }





}