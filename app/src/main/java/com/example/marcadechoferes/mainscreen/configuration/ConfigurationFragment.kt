package com.example.marcadechoferes.mainscreen.configuration

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.marcadechoferes.R
import com.example.marcadechoferes.databinding.FragmentConfigurationBinding
import com.example.marcadechoferes.mainscreen.viewModel.MainViewModel

class ConfigurationFragment : Fragment() {
    lateinit var dailogBuilder : AlertDialog.Builder
    lateinit var alertDialog: AlertDialog
lateinit var mainViewModel: MainViewModel
lateinit var binding :FragmentConfigurationBinding
    lateinit var dismiss: ImageView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment


        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_configuration, container, false
        )

        var viewBinding = binding.root
        initViews()
        openPopupWindow()
        return viewBinding

    }
    fun initViews(){
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        mainViewModel.navigationLiveData.observe(viewLifecycleOwner, Observer {
            if (it=="1"){
                findNavController().navigate(
                    R.id.action_configurationFragment_to_homeFragment,null,null

                )

            }
            else if(it=="2"){
                findNavController().navigate(
                    R.id.action_configurationFragment_to_profileFragment,null,null

                )
            }

        }
        )


    }


    fun openPopupWindow(){
        dailogBuilder = AlertDialog.Builder(getContext())
        val contactPopupView: View = layoutInflater.inflate(R.layout.langauge_popup_window, null)
        alertDialog = dailogBuilder.create()
        binding.languageLayout.setOnClickListener {
            alertDialog.setView(contactPopupView)
            alertDialog.show()
            alertDialog.getWindow()?.setLayout(650, 600)
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dismiss = contactPopupView.findViewById(R.id.close)

        dismiss.setOnClickListener {
            alertDialog.dismiss()
        }

    }

}