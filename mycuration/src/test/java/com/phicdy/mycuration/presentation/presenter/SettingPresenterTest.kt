package com.phicdy.mycuration.presentation.presenter

import android.content.Context
import android.content.SharedPreferences
import com.phicdy.mycuration.data.repository.AdditionalSettingApi
import com.phicdy.mycuration.presentation.view.SettingView
import com.phicdy.mycuration.util.PreferenceHelper
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.times


class SettingPresenterTest {

    private lateinit var mockView: SettingView
    private lateinit var helper: PreferenceHelper
    private lateinit var mockContext: Context
    private val additionalSettingApi = mock(AdditionalSettingApi::class.java)

    @Before
    fun setup() {
        mockView = mock(SettingView::class.java)

        mockContext = mock(Context::class.java)
        Mockito.`when`(mockContext.getSharedPreferences("FilterPref", Context.MODE_PRIVATE))
                .thenReturn(mock(SharedPreferences::class.java))
        PreferenceHelper.setUp(mockContext)
        helper = PreferenceHelper
    }

    @Test
    fun initViewIsCalledWhenActivityCreated() {
        val presenter = SettingPresenter(mockView, helper, additionalSettingApi, arrayOf(), arrayOf(), arrayOf(),
                arrayOf(), arrayOf(), arrayOf(), arrayOf(), arrayOf())
        presenter.activityCreate()
        Mockito.verify(mockView, times(1)).initView()
    }

    @Test
    fun initListenerIsCalledWhenActivityCreated() {
        val presenter = SettingPresenter(mockView, helper, additionalSettingApi, arrayOf(), arrayOf(), arrayOf(),
                arrayOf(), arrayOf(), arrayOf(), arrayOf(), arrayOf())
        presenter.activityCreate()
        Mockito.verify(mockView, times(1)).initListener()
    }
}