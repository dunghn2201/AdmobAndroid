package com.android.admobmodule

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdManager(private val activity: Activity) {
    private var interstitialAd: InterstitialAd? = null
    private var rewardedVideoAd: RewardedAd? = null
    private var currentNativeAd: NativeAd? = null
    private var builder: AdLoader.Builder? = null

    init {
        MobileAds.initialize(activity)
    }

    //Create full screen content callback
    val fullScreenContentCallBack = object : FullScreenContentCallback() {
        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
            super.onAdFailedToShowFullScreenContent(p0)
        }

        override fun onAdShowedFullScreenContent() {
            super.onAdShowedFullScreenContent()
        }

        override fun onAdDismissedFullScreenContent() {
            super.onAdDismissedFullScreenContent()
            interstitialAd = null
            rewardedVideoAd = null
        }
    }

    fun loadInterstitialAd(interstitialAdInitId: String) {
        InterstitialAd.load(
            activity,
            interstitialAdInitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    super.onAdLoaded(ad)
                    interstitialAd = ad
                    interstitialAd?.fullScreenContentCallback = fullScreenContentCallBack
                    interstitialAd?.show(activity)
                }
            })
    }

    fun loadRewardedVideoAd(
        AdVideoUnitId: String,
        onUserEarnedReward: ((RewardItem) -> Unit)? = null
    ) {
        val request = AdRequest.Builder().build()
        RewardedAd.load(activity, AdVideoUnitId, request, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                super.onAdLoaded(ad)
                rewardedVideoAd = ad
                rewardedVideoAd?.fullScreenContentCallback = fullScreenContentCallBack
                rewardedVideoAd?.show(activity) {
                    onUserEarnedReward?.invoke(it)
                }
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
            }
        })
    }

    fun loadNativeAd(adFrame: FrameLayout, nativeAdUnitId: String) {
        val viewOptions = VideoOptions.Builder()
            .setStartMuted(false)
            .build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(viewOptions)
            .build()
        builder = AdLoader.Builder(activity, nativeAdUnitId)
        builder?.forNativeAd { unifiedNativeAd: NativeAd ->
            val adView =
                activity.layoutInflater.inflate(R.layout.ad_unified, null) as NativeAdView
            populateUnifiedNativeAdView(unifiedNativeAd, adView)
            adFrame.removeAllViews()
            adFrame.addView(adView)
        }?.withNativeAdOptions(adOptions)
        val adLoader = builder?.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(i: LoadAdError) {
                super.onAdFailedToLoad(i)
                Log.e("NativeAdFailed", i.toString() + "")
            }
        })?.build()
        adLoader?.loadAd(AdManagerAdRequest.Builder().build())
    }


    private fun populateUnifiedNativeAdView(
        nativeAd: NativeAd,
        adView: NativeAdView
    ) {
        // You must call destroy on old ads when you are done with them,
        // otherwise you will have a memory leak.
        if (currentNativeAd != null) currentNativeAd?.destroy()
        currentNativeAd = nativeAd

        // Set the media view.
        adView.mediaView =
            adView.findViewById<View>(R.id.ad_media) as com.google.android.gms.ads.nativead.MediaView

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        nativeAd.mediaContent?.let {
            adView.mediaView?.setMediaContent(it)

        }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as TextView).text = nativeAd.callToAction
        }
        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }
        if (nativeAd.price == null) {
            adView.priceView?.visibility = View.INVISIBLE
        } else {
            adView.priceView?.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }
        if (nativeAd.store == null) {
            adView.storeView?.visibility = View.INVISIBLE
        } else {
            adView.storeView?.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }
        if (nativeAd.starRating == null) {
            adView.starRatingView?.visibility = View.INVISIBLE
        } else {
            nativeAd.starRating?.toDouble()?.let {
                (adView.starRatingView as RatingBar).rating = it.toFloat()
                adView.starRatingView?.visibility = View.VISIBLE
            }


        }
        if (nativeAd.advertiser == null) {
            adView.advertiserView?.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView?.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)
    }
}