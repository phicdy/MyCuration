package com.phicdy.mycuration.presentation.presenter

import android.content.Context
import com.phicdy.mycuration.data.preference.PreferenceHelper
import com.phicdy.mycuration.data.repository.AdditionalSettingApi
import com.phicdy.mycuration.domain.setting.SettingInitialData
import com.phicdy.mycuration.presentation.view.SettingView
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever


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
        val presenter = SettingPresenter(mockView, helper, additionalSettingApi, SettingInitialData(arrayOf(), arrayOf(), arrayOf(),
                arrayOf(), arrayOf(), arrayOf(), arrayOf(), arrayOf(), arrayOf(), arrayOf()))
        presenter.activityCreate()
        verify(mockView, times(1)).initView()
    }

    @Test
    fun initListenerIsCalledWhenActivityCreated() {
        val presenter = SettingPresenter(mockView, helper, additionalSettingApi, SettingInitialData(arrayOf(), arrayOf(), arrayOf(),
                arrayOf(), arrayOf(), arrayOf(), arrayOf(), arrayOf(), arrayOf(), arrayOf()))
        presenter.activityCreate()
        verify(mockView, times(1)).initListener()
    }
}