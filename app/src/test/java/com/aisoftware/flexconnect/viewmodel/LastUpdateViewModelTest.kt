package com.aisoftware.flexconnect.viewmodel

import android.app.Application
import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.support.test.filters.SmallTest
import com.aisoftware.flexconnect.db.DataRepository
import com.aisoftware.flexconnect.model.LastUpdate
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@SmallTest
class LastUpdateViewModelTest {

    @Mock
    private lateinit var observer: Observer<LastUpdate>
    @Mock
    private lateinit var dataRepository: DataRepository
    @Mock
    private lateinit var application: Application

    private lateinit var liveData: MutableLiveData<LastUpdate>
    private lateinit var model: LastUpdateViewModel

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        liveData = MutableLiveData()
        model = LastUpdateViewModel(application, dataRepository)
    }

    @Test
    @Ignore
    fun testGetLastUpdate() {
        val lastUpdate = LastUpdate(1, System.currentTimeMillis().toString())
        model.getLastUpdate().observeForever(observer)
        liveData.value = lastUpdate
        verify(observer).onChanged(LastUpdate())
    }
}