package com.example.jeonsilog.view.admin

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.view.isGone
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jeonsilog.R
import com.example.jeonsilog.base.BaseFragment
import com.example.jeonsilog.data.remote.dto.exhibition.ExhibitionInfo
import com.example.jeonsilog.data.remote.dto.review.GetReviewsExhibitionInformationEntity
import com.example.jeonsilog.databinding.FragmentAdminExhibitionBinding
import com.example.jeonsilog.repository.exhibition.ExhibitionRepositoryImpl
import com.example.jeonsilog.repository.review.ReviewRepositoryImpl
import com.example.jeonsilog.view.exhibition.ExtraActivity
import com.example.jeonsilog.viewmodel.ExhibitionViewModel
import com.example.jeonsilog.viewmodel.UpdateReviewItem
import com.example.jeonsilog.widget.utils.DateUtil
import com.example.jeonsilog.widget.utils.GlobalApplication
import com.example.jeonsilog.widget.utils.GlobalApplication.Companion.encryptedPrefs
import com.example.jeonsilog.widget.utils.GlobalApplication.Companion.exhibitionId
import com.example.jeonsilog.widget.utils.GlobalApplication.Companion.isRefresh
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class AdminExhibitionFragment : BaseFragment<FragmentAdminExhibitionBinding>(R.layout.fragment_admin_exhibition) {
    private lateinit var exhibitionRvAdapter: AdminExhibitionReviewRvAdapter
    private var exhibitionInfoData: ExhibitionInfo? = null
    private var thisExhibitionId = 0
    private var hasNextPage = true
    //감상평
    private var reviewList = mutableListOf<GetReviewsExhibitionInformationEntity>()
    private var reviewPage = 0
    private val exhibitionViewModel: ExhibitionViewModel by activityViewModels()

    override fun init() {
        isRefresh.observe(this){
            if(it){
                (activity as ExtraActivity).refreshFragment(R.id.exhibitionFragment)
                isRefresh.value = false
            }
        }

        thisExhibitionId = exhibitionId

        getExhibitionInfo() //페이지 세팅
        setBottomSheet() //바텀시트 세팅

        //감상평 - RecyclerView
        getReviewInfo()

        //포스터
        binding.ivPosterImage.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_adminExhibitionFragment_to_adminPosterFragment)
        }

        //recyclerView 페이징 처리
        binding.rvExhibitionReview.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val rvPosition = (recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                val totalCount = recyclerView.adapter?.itemCount?.minus(1)
                if(rvPosition == totalCount && hasNextPage){
                    setReviewRvByPage(totalCount)
                }
            }
        })
    }
    private fun setBottomSheet(){
        //디바이스 높이값 가져오기
        val displayMetrics = DisplayMetrics()
        val windowManager = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels

        //바텀시트 세팅
        BottomSheetBehavior.from(binding.nsvBottomSheet).apply {
            peekHeight = (screenHeight * 0.52).toInt()
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }
    private fun getExhibitionInfo(){
        exhibitionInfoData = runBlocking(Dispatchers.IO) {
            val response = ExhibitionRepositoryImpl().getExhibition(GlobalApplication.encryptedPrefs.getAT(), thisExhibitionId)
            if(response.isSuccessful && response.body()!!.check){
                response.body()!!.information
            }else{
                null
            }
        }

        Glide.with(requireContext())
            .load(exhibitionInfoData?.imageUrl)
            .into(binding.ivPosterImage)

        binding.tvExhibitionName.text = exhibitionInfoData?.exhibitionName
        //전시공간 이름
        if(exhibitionInfoData?.place?.placeName !=null){
            binding.tvPlaceName.text = exhibitionInfoData?.place?.placeName
        }else{ binding.tvPlaceName.text = getString(R.string.admin_exhibition_place_name_empty) }
        //전시공간 주소
        if(exhibitionInfoData?.place?.address !=null){
            binding.tvAddress.text = exhibitionInfoData?.place?.address
        }else{ binding.tvAddress.text = getString(R.string.admin_exhibition_place_address_empty) }
        //전시공간 전화번호
        if(exhibitionInfoData?.place?.tel !=null){
            binding.tvCall.text = exhibitionInfoData?.place?.tel
        }else{ binding.tvCall.text = getString(R.string.admin_exhibition_place_call_empty) }
        //전시공간 홈페이지
        if(exhibitionInfoData?.place?.homePage !=null){
            binding.tvHomepage.text = exhibitionInfoData?.place?.homePage
        }else{ binding.tvHomepage.text = getString(R.string.admin_exhibition_place_homepage_empty) }
        //전시회 정보
        if(exhibitionInfoData?.information !=null){
            binding.tvInformation.text = exhibitionInfoData?.information
        }else{ binding.tvInformation.text = getString(R.string.admin_exhibition_information_empty) }

        setKeywords()
    }

    private fun setKeywords(){
        var operatingKeyword = ""
        when(exhibitionInfoData?.operatingKeyword){
            "ON_DISPLAY" -> operatingKeyword = requireContext().getString(R.string.keyword_state_on)
            "BEFORE_DISPLAY" -> operatingKeyword = requireContext().getString(R.string.keyword_state_before)
        }
        var priceKeyword = ""
        when(exhibitionInfoData?.priceKeyword){
            "FREE" -> priceKeyword = requireContext().getString(R.string.keyword_free)
            else -> binding.tvKeywordSecond.isGone = true
        }

        if(operatingKeyword!=""){
            binding.tvKeywordFirst.text = operatingKeyword
            binding.tvKeywordSecond.text = priceKeyword
        }else{
            if(priceKeyword!=""){
                binding.tvKeywordSecond.isGone = true
                binding.tvKeywordFirst.text = priceKeyword
            }else {
                binding.tvKeywordFirst.isGone = true
            }
        }
    }

    //감상평 RecyclerView
    private fun getReviewInfo(){
        exhibitionRvAdapter = AdminExhibitionReviewRvAdapter(reviewList, requireContext())
        binding.rvExhibitionReview.adapter = exhibitionRvAdapter
        binding.rvExhibitionReview.layoutManager = LinearLayoutManager(context)

        setReviewRvByPage(0)

        exhibitionRvAdapter.setOnItemClickListener(object: AdminExhibitionReviewRvAdapter.OnItemClickListener{
            override fun onItemClick(v: View, data: GetReviewsExhibitionInformationEntity, position: Int) {
                //감상평 페이지로 이동
                val item = UpdateReviewItem(data, position)
                exhibitionViewModel.setReviewItem(item)
                GlobalApplication.newReviewId = data.reviewId
                Navigation.findNavController(v).navigate(R.id.action_adminExhibitionFragment_to_adminReviewFragment)
            }

            override fun deleteItem(position: Int, reviewId: Int) {
                var deleteSuccess =false
                runBlocking(Dispatchers.IO){
                    val response = ReviewRepositoryImpl().deleteReview(encryptedPrefs.getAT(), reviewId)
                    if(response.isSuccessful && response.body()!!.check){
                        deleteSuccess = true
                    }
                }
                if(deleteSuccess){
                    exhibitionRvAdapter.deleteItem(position)
                }
            }
        })
    }
    private fun setReviewRvByPage(totalCount:Int){
        var addItemCount = 0
        Log.d("review", "setReviewRvByPage: review Page: $reviewPage")
        runBlocking(Dispatchers.IO) {
            val response = ReviewRepositoryImpl().getReviews(encryptedPrefs.getAT(), thisExhibitionId, reviewPage)
            if(response.isSuccessful && response.body()!!.check){
                reviewList.addAll(response.body()!!.informationEntity.data)
                addItemCount = response.body()!!.informationEntity.data.size
                hasNextPage = response.body()!!.informationEntity.hasNextPage
            }else{
                null
            }
        }
        exhibitionRvAdapter.notifyItemRangeInserted(totalCount, addItemCount)
        reviewPage++
    }

}