package com.example.jeonsilog.widget.utils

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.jeonsilog.BuildConfig
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility

class GlobalApplication: Application() {
    companion object {
        lateinit var prefs: PreferenceUtil
        lateinit var encryptedPrefs: CryptedPreferenceUtil
        var isFinish = MutableLiveData(false)

        var exhibitionId: Int = 0
        var extraActivityReference: Int = 0
    }

    override fun onCreate() {
        prefs = PreferenceUtil(applicationContext)
        encryptedPrefs = CryptedPreferenceUtil(applicationContext)
        super.onCreate()

        getKeyHash()
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
    }

    private fun getKeyHash(){
        val keyHash = Utility.getKeyHash(this)
        Log.d("Hash", keyHash)
    }
}