package com.logicasur.appchoferes.afterAuth.mainscreen.fragments.profile

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
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
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.logicasur.appchoferes.Extra.Language
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.databinding.FragmentProfileBinding
import com.logicasur.appchoferes.afterAuth.mainscreen.MainActivity
import com.logicasur.appchoferes.afterAuth.mainscreen.fragments.profile.viewmodel.ProfileViewModel
import com.logicasur.appchoferes.afterAuth.mainscreen.viewModel.MainViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.logicasur.appchoferes.Extra.CheckConnection
import com.logicasur.appchoferes.utils.ResendApis
import com.logicasur.appchoferes.common.loadingScreen.LoadingScreen
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class ProfileFragment : Fragment() {
    lateinit var dailogBuilder: AlertDialog.Builder
    lateinit var alertDialog: AlertDialog
    lateinit var dismiss: ImageView
    lateinit var title: TextView
    lateinit var changedName: EditText
    lateinit var binding: FragmentProfileBinding
    lateinit var confirmbtn: AppCompatButton
    private val profileViewModel: ProfileViewModel by viewModels()
    lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var resendApis: ResendApis

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val language = Language()

        language.setLanguage((activity as MainActivity).baseContext)
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_profile, container, false
        )

        initViews()
        openPopupWindow()
        setButtonColor()
        return binding.root
    }

    private fun initViews() {
        val context = (activity as MainActivity).context


        profileViewModel.viewsForFragment(context, binding)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        val animation = TransitionInflater.from(requireContext()).inflateTransition(
            R.transition.example_1
        )
        sharedElementEnterTransition = animation

        mainViewModel.navigationLiveData.observe(viewLifecycleOwner, Observer {

            if (it == "1") {
                val extras = FragmentNavigatorExtras(
                    binding.profileImage to "image_small",
                    binding.namelayout to "small_name_layout",
                    binding.dateLayout to "small_date"
                )
                findNavController().navigate(
                    R.id.action_profileFragment_to_homeFragment, null, null, extras
                )

            } else if (it == "3") {
                findNavController().navigate(
                    R.id.action_profileFragment_to_configurationFragment,
                    null,
                    null, null
                )

            }
        })


        binding.editProfile.setOnClickListener {
            lifecycleScope.launch {

                            initPermission()
//                        } else {
//                            Toast.makeText(context, profileViewModel.TAG2, Toast.LENGTH_SHORT)
//                                .show()
//                        }

            }

        }


    }


    fun startImagePicker() {
        com.github.dhaval2404.imagepicker.ImagePicker.with(this)
            //Crop image(Optional), Check Customization for more option
            .compress(40) //Final image size will be less than 1 MB(Optional)
            .maxResultSize(
                360,
                360
            )
            //Final image resolution will be less than 1080 x 1080(Optional)
            .start()
    }

    private fun initPermission() {

        val permissions =
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        Permissions.check(
            (activity as MainActivity).context/*context*/,
            permissions,
            null /*rationale*/,
            null /*options*/,
            object : PermissionHandler() {
                override fun onGranted() {

                    startImagePicker()


                }

                override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
                    super.onDenied(context, deniedPermissions)
                }
            })


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {
            Activity.RESULT_OK -> {
                //Image Uri will not be null for RESULT_OK
                val uri: Uri = data?.data!!
                val bitmapImage = MediaStore.Images.Media.getBitmap(context?.contentResolver, uri)
                Log.d("ImageUploadAvatar", "Before Calling bitmapToBase64 function.")
                profileViewModel.bitmapToBase64(bitmapImage)

            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun openPopupWindow() {
        dailogBuilder = AlertDialog.Builder(getContext())
        val contactPopupView: View = layoutInflater.inflate(R.layout.name_change_popup_window, null)
        alertDialog = dailogBuilder.create()
        title = contactPopupView.findViewById(R.id.popupTitleName)
        var option = 1
        val context = (activity as MainActivity).context


        binding.editName.setOnClickListener {

            lifecycleScope.launch {
//                        if (CheckConnection.netCheck(context)) {

                            option = 1
                            alertDialog.setView(contactPopupView)
                            title.text = getResources().getString(R.string.firstname)
                            val name = binding.TitleName.text
                            println("showable name is $name")
                            changedName.setText("$name", TextView.BufferType.EDITABLE);
                            alertDialog.show()
                            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
                            val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
                            alertDialog.getWindow()?.setLayout(width, height)
                            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


//                        } else {
//                            Toast.makeText(context, profileViewModel.TAG2, Toast.LENGTH_SHORT)
//                                .show()
//                        }



            }

        }
        binding.editSurname.setOnClickListener {


            lifecycleScope.launch {


                            option = 2
                            alertDialog.setView(contactPopupView)
                            title.text = getResources().getString(R.string.father_name)
                            val name = binding.FatherName.text
                            println("showable name is $name")
                            changedName.setText("$name", TextView.BufferType.EDITABLE);
                            alertDialog.show()
                            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
                            val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
                            alertDialog.window?.setLayout(width, height)
                            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


//                        } else {
//                            Toast.makeText(context, profileViewModel.TAG2, Toast.LENGTH_SHORT)
//                                .show()
//                        }




            }


        }

        dismiss = contactPopupView.findViewById(R.id.close)
        confirmbtn = contactPopupView.findViewById(R.id.confirm_btn)
        (activity as MainActivity).setGrad(
            ResendApis.primaryColor,
            ResendApis.secondaryColor,
            confirmbtn
        )
        changedName = contactPopupView.findViewById(R.id.popup_Name_Field)
        dismiss.setOnClickListener {
            alertDialog.dismiss()
        }

        confirmbtn.setOnClickListener {
            val nameChanges = changedName.text
            if ((nameChanges.length >= 3)) {
                val fatherName = binding.FatherName.text
                val name = binding.TitleName.text
                Log.d("input Parameters", "$nameChanges  $fatherName  $name")
                if (option == 1) {

                    profileViewModel.moveToLoadingScreen()
                    lifecycleScope.launch(Dispatchers.IO) {
                        resendApis.serverCheck.serverCheckMainActivityApi { serverAction ->
                            profileViewModel.updateProfile(
                                nameChanges.toString(),
                                fatherName.toString(),
                                alertDialog
                            ) { serverAction() }
                        }

                    }

                } else {
                    profileViewModel.moveToLoadingScreen()
                    lifecycleScope.launch(Dispatchers.IO) {
                        resendApis.serverCheck.serverCheckMainActivityApi { serverAction ->
                            profileViewModel.updateProfile(
                                name.toString(),
                                nameChanges.toString(),
                                alertDialog
                            ) { serverAction() }
                        }


                    }

                }
            }
        }


    }


    private fun setButtonColor() {

        binding.editSurname.setCardBackgroundColor(Color.parseColor(ResendApis.primaryColor))
        binding.editName.setCardBackgroundColor(Color.parseColor(ResendApis.primaryColor))
        binding.edit.setCardBackgroundColor(Color.parseColor(ResendApis.primaryColor))
        binding.editProfile.setCardBackgroundColor(Color.parseColor(ResendApis.primaryColor))
        binding.upperLayoutFrount.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))
        binding.upperLayoutFrount.alpha = 0.73F
    }

    override fun onResume() {
        super.onResume()

        (activity as MainActivity).binding.menu.setItemSelected(R.id.User, true)

    }

}