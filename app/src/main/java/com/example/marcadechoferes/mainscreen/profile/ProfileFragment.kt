package com.example.marcadechoferes.mainscreen.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.example.marcadechoferes.Extra.Language
import com.example.marcadechoferes.R
import com.example.marcadechoferes.databinding.FragmentProfileBinding
import com.example.marcadechoferes.mainscreen.MainActivity
import com.example.marcadechoferes.mainscreen.profile.viewmodel.ProfileViewModel
import com.example.marcadechoferes.mainscreen.viewModel.MainViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProfileFragment : Fragment() {
    lateinit var dailogBuilder: AlertDialog.Builder
    lateinit var alertDialog: AlertDialog
    lateinit var dismiss: ImageView
    lateinit var title:TextView
    lateinit var changedName:EditText
    lateinit var binding: FragmentProfileBinding
    lateinit var confirmbtn:Button
val profileViewModel :ProfileViewModel by viewModels()
    lateinit var mainViewModel: MainViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val language= Language()

        language.setLanguage((activity as MainActivity).baseContext)
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_profile, container, false
        )


        var viewBinding=binding.root
        initViews()
        openPopupWindow()
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


        binding.editProfile.setOnClickListener {
            com.github.dhaval2404.imagepicker.ImagePicker.with(this)
               //Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()

        }




    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            var uri : Uri = data?.data!!
            val bitmapImage = MediaStore.Images.Media.getBitmap(context?.contentResolver, uri)

            profileViewModel.bitmapToBase64(bitmapImage)

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }

    }

    fun openPopupWindow(){
        dailogBuilder = AlertDialog.Builder(getContext())
        val contactPopupView: View = layoutInflater.inflate(R.layout.name_change_popup_window, null)
        alertDialog = dailogBuilder.create()
        title=contactPopupView.findViewById(R.id.popupTitleName)
        var option=1
        var context = (activity as MainActivity).context


        binding.editName.setOnClickListener {
            alertDialog.setView(contactPopupView)
            title.text= getResources().getString(R.string.firstname)
            var name = binding.TitleName.text
            println("showable name is $name")
            changedName.setText("$name", TextView.BufferType.EDITABLE);
            alertDialog.show()
            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
            val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
            alertDialog.getWindow()?.setLayout(width, height)
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        binding.editSurname.setOnClickListener {
            option=2
            alertDialog.setView(contactPopupView)
            title.text=getResources().getString(R.string.father_name)
            var name = binding.FatherName.text
            println("showable name is $name")
            changedName.setText("$name", TextView.BufferType.EDITABLE);
            alertDialog.show()
            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
            val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
            alertDialog.getWindow()?.setLayout(width, height)
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        dismiss = contactPopupView.findViewById(R.id.close)
        confirmbtn=contactPopupView.findViewById(R.id.confirm_btn)
        changedName=contactPopupView.findViewById(R.id.popup_Name_Field)
        dismiss.setOnClickListener {
            alertDialog.dismiss()
        }

        confirmbtn.setOnClickListener {
            var nameChanges =changedName.text
            if((nameChanges.length >= 3)) {
                var fatherName= binding.FatherName.text
                var Name=binding.TitleName.text
                Log.d("input Parameters","$nameChanges  $fatherName  $Name")
                if (option == 1) {
                    profileViewModel.updateProfile(nameChanges.toString(),fatherName.toString(),alertDialog)
                } else {
                    profileViewModel.updateProfile(Name.toString(),nameChanges.toString(),alertDialog)
                }
            }
        }









    }





}