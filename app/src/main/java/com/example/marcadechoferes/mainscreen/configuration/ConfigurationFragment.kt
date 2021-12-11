package com.example.marcadechoferes.mainscreen.configuration

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.marcadechoferes.Extra.Language
import com.example.marcadechoferes.Extra.TinyDB
import com.example.marcadechoferes.R
import com.example.marcadechoferes.databinding.FragmentConfigurationBinding
import com.example.marcadechoferes.mainscreen.MainActivity
import com.example.marcadechoferes.mainscreen.configuration.viewModel.ConfigurationViewModel
import com.example.marcadechoferes.mainscreen.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint




@AndroidEntryPoint
class ConfigurationFragment : Fragment() {
    val viewModel: ConfigurationViewModel by viewModels()
    lateinit var dailogBuilder: AlertDialog.Builder
    lateinit var alertDialog: AlertDialog
    lateinit var mainViewModel: MainViewModel
    lateinit var binding: FragmentConfigurationBinding
    lateinit var dismiss: ImageView
    lateinit var tinyDB: TinyDB
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
        tinyDB.putBoolean("reload",false)
        var viewBinding = binding.root
        initViews()
        openPopupWindow()
        return viewBinding

    }

    fun initViews() {

        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

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

            alertDialog.setView(contactPopupView)
            viewModel.selectedLanguage()
            alertDialog.show()
            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
            val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
            alertDialog.getWindow()?.setLayout(width, height)
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dismiss = contactPopupView.findViewById(R.id.close)
        dismiss.setOnClickListener {
            alertDialog.dismiss()
        }

    }

    override fun onResume() {
        super.onResume()
       var reload= tinyDB.getBoolean("reload")
        if(reload) {
            val id = findNavController().currentDestination?.id
            findNavController().popBackStack(id!!, true)
            findNavController().navigate(id)
        }
    }

}