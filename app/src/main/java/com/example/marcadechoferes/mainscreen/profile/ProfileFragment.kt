package com.example.marcadechoferes.mainscreen.profile

import android.os.Bundle
import android.transition.Fade
import android.transition.TransitionInflater
import android.transition.TransitionSet
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.example.marcadechoferes.R
import com.example.marcadechoferes.databinding.FragmentProfileBinding
import com.example.marcadechoferes.mainscreen.MainActivity
import com.example.marcadechoferes.mainscreen.profile.viewmodel.ProfileViewModel
import com.example.marcadechoferes.mainscreen.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

lateinit var binding: FragmentProfileBinding
val profileViewModel :ProfileViewModel by viewModels()
    lateinit var mainViewModel: MainViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_profile, container, false
        )


        var viewBinding=binding.root
        initViews()
        return  viewBinding
    }

    fun initViews(){
        var context = (activity as MainActivity).context

        profileViewModel.viewsForFragment(context,binding)

        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        val animation = TransitionInflater.from(requireContext()).inflateTransition(
            R.transition.example_1
        )
        sharedElementEnterTransition = animation

        mainViewModel.navigationLiveData.observe(viewLifecycleOwner, Observer {

            if(it=="1"){
                val extras = FragmentNavigatorExtras(
                    binding.profileImage to "image_small",
                    binding.namelayout to "small_name_layout",
                    binding.dateLayout to "small_date"
                )
                findNavController().navigate(
                    R.id.action_profileFragment_to_homeFragment,null,null,extras

                )

            }else if(it=="3"){
                findNavController().navigate(
                    R.id.action_profileFragment_to_configurationFragment,
                    null,
                    null,null
                )

            }
        })



    }



}