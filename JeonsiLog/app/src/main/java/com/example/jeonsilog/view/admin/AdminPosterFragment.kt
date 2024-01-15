package com.example.jeonsilog.view.admin

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.example.jeonsilog.R
import com.example.jeonsilog.base.BaseFragment
import com.example.jeonsilog.databinding.FragmentAdminPosterBinding
import com.example.jeonsilog.view.exhibition.AdminPosterVpAdapter
import com.example.jeonsilog.viewmodel.AdminExhibitionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.IOException

class AdminPosterFragment : BaseFragment<FragmentAdminPosterBinding>(R.layout.fragment_admin_poster) {
    private val adminExhibitionViewModel:AdminExhibitionViewModel by activityViewModels()
    private val MY_PERMISSIONS_REQUEST_READ_MEDIA_IMAGES = 1
    private var imageUri: Uri? = null
    override fun init() {
        if(adminExhibitionViewModel.exhibitionPosterImg!=null){
            setPosterView(true)
            Glide.with(requireContext())
                .load(adminExhibitionViewModel.exhibitionPosterImg.value)
                .into(binding.ivPoster)
        }else{
            setPosterView(false)
        }

        binding.ibPosterDelete.setOnClickListener{
            setPosterView(false)
        }
        binding.ibGetPosterFromGallery.setOnClickListener{
            checkPermissionAndOpenGallery()
        }
    }
    private fun setPosterView(hasPoster:Boolean){
        if(hasPoster){
            binding.llPosterEmptyState.visibility = View.INVISIBLE
            binding.ivPoster.visibility = View.VISIBLE
        }else{
            binding.llPosterEmptyState.visibility = View.VISIBLE
            binding.ivPoster.visibility = View.INVISIBLE
        }
    }
    private fun checkPermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 권한이 없는 경우 권한 요청 다이얼로그를 표시
                requestStoragePermission()
                Log.d("TAG01", "checkPermissionAndOpenGallery: ")
            } else {
                // 권한이 이미 있는 경우 갤러리에 접근할 수 있는 로직을 수행
                accessGallery()
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 권한이 없는 경우 권한 요청 다이얼로그를 표시
                requestStoragePermission()

            } else {
                // 권한이 이미 있는 경우 갤러리에 접근할 수 있는 로직을 수행
                accessGallery()
            }
        }

    }
    private fun requestStoragePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            ) {
                // 권한을 이전에 거부한 경우
                showPermissionRationaleDialog()
            } else {
                // 권한 요청
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    MY_PERMISSIONS_REQUEST_READ_MEDIA_IMAGES
                )
            }
        }else{

        }
    }
    private fun showPermissionRationaleDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.permission_denied))
            .setPositiveButton("이동") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun openAppSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = android.net.Uri.parse("package:" +requireContext() .packageName)
        startActivity(intent)
    }

    private fun accessGallery(){
        // 갤러리 열기 Intent 생성
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        launcher.launch(intent)

    }

    private val launcher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data!!.data
            if (imageUri != null) {
                try {
                    updateImageView(imageUri!!)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun updateImageView(imageUri:Uri){
        adminExhibitionViewModel.setPosterUri(imageUri)
        adminExhibitionViewModel.setIsChanged(true)
        Glide.with(this)
            .load(imageUri)
            .into(binding.ivPoster)
        setPosterView(true)
    }
}