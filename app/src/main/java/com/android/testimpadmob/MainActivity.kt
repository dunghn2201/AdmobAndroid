package com.android.testimpadmob

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.android.admobmodule.AdManager

class MainActivity : AppCompatActivity() {
    var adManager: AdManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adManager = AdManager(this)
        loadRewardedVideoAd()

    }

    private fun loadRewardedVideoAd() {
        adManager?.loadRewardedVideoAd("ca-app-pub-3940256099942544/5224354917")
    }

    private fun loadInterstitialAd() {
        adManager?.loadInterstitialAd(
            "ca-app-pub-3940256099942544/1033173712"
        )
    }

    private fun loadNativeAd() {
        val frameAd: FrameLayout = findViewById(R.id.ad_frame)
        adManager?.loadNativeAd(
            frameAd,
            "ca-app-pub-3940256099942544/2247696110"
        )
    }
}