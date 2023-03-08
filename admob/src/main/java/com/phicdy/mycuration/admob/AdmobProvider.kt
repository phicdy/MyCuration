package com.phicdy.mycuration.admob

import android.content.Context
import android.view.ViewGroup
import com.google.android.gms.ads.MobileAds
import com.phicdy.mycuration.advertisement.AdFragment
import com.phicdy.mycuration.advertisement.AdProvider
import com.phicdy.mycuration.advertisement.AdViewHolder
import javax.inject.Inject

class AdmobProvider @Inject constructor() : AdProvider {

    override fun init(context: Context) {
        MobileAds.initialize(context, BuildConfig.AD_APP_ID)
    }

    override fun newViewHolderInstance(parent: ViewGroup): AdViewHolder = AdmobViewHolder(parent)

    override fun newFragmentInstance(): AdFragment = AdmobFragment.newInstance()
}