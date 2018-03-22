package com.phicdy.mycuration.presentation.presenter

import android.content.Context
import android.content.SharedPreferences
import com.phicdy.mycuration.util.PreferenceHelper
import com.phicdy.mycuration.view.SettingView
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.times


class SettingPresenterTest {

    private lateinit var mockView: SettingView
    private lateinit var helper: PreferenceHelper
    private lateinit var mockContext: Context

    @Before
    fun setup() {
        mockView = Mockito.mock(SettingView::class.java)

        mockContext = Mockito.mock(Context::class.java)
        Mockito.`when`(mockContext.getSharedPreferences("FilterPref", Context.MODE_PRIVATE))
                .thenReturn(Mockito.mock(SharedPreferences::class.java))
        PreferenceHelper.setUp(mockContext)
        helper = PreferenceHelper
    }

    @Test
    fun initViewIsCalledWhenActivityCreated() {
        val presenter = SettingPresenter(helper, arrayOf(), arrayOf(), arrayOf(),
                arrayOf(), arrayOf(), arrayOf(), arrayOf(), arrayOf())
        presenter.setView(mockView)
        presenter.activityCreate()
        Mockito.verify(mockView, times(1)).initView()
    }

    @Test
    fun initListenerIsCalledWhenActivityCreated() {
        val presenter = SettingPresenter(helper, arrayOf(), arrayOf(), arrayOf(),
                arrayOf(), arrayOf(), arrayOf(), arrayOf(), arrayOf())
        presenter.setView(mockView)
        presenter.activityCreate()
        Mockito.verify(mockView, times(1)).initListener()
    }
}