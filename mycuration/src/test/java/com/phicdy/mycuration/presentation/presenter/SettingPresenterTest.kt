package com.phicdy.mycuration.presentation.presenter

import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.phicdy.mycuration.data.repository.AdditionalSettingApi
import com.phicdy.mycuration.presentation.view.SettingView
import com.phicdy.mycuration.util.PreferenceHelper
import org.junit.Before
import org.junit.Test


class SettingPresenterTest {

    private lateinit var mockView: SettingView
    private lateinit var helper: PreferenceHelper
    private lateinit var mockContext: Context
    private val additionalSettingApi = mock<AdditionalSettingApi>()

    @Before
    fun setup() {
        mockView = mock()

        mockContext = mock()
        whenever(mockContext.getSharedPreferences("FilterPref", Context.MODE_PRIVATE))
                .thenReturn(mock())
        PreferenceHelper.setUp(mockContext)
        helper = PreferenceHelper
    }

    @Test
    fun initViewIsCalledWhenActivityCreated() {
        val presenter = SettingPresenter(mockView, helper, additionalSettingApi, arrayOf(), arrayOf(), arrayOf(),
                arrayOf(), arrayOf(), arrayOf(), arrayOf(), arrayOf())
        presenter.activityCreate()
        verify(mockView, times(1)).initView()
    }

    @Test
    fun initListenerIsCalledWhenActivityCreated() {
        val presenter = SettingPresenter(mockView, helper, additionalSettingApi, arrayOf(), arrayOf(), arrayOf(),
                arrayOf(), arrayOf(), arrayOf(), arrayOf(), arrayOf())
        presenter.activityCreate()
        verify(mockView, times(1)).initListener()
    }
}