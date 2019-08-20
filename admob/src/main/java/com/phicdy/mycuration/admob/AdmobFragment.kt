package com.phicdy.mycuration.admob

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.phicdy.mycuration.advertisement.AdFragment

class AdmobFragment : AdFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_admob, container, false)

        val adView = rootView.findViewById<AdView>(R.id.adView)
        adView.loadAd(AdRequest.Builder().build())
        return rootView
    }

    companion object {
        fun newInstance() = AdmobFragment()
    }
}