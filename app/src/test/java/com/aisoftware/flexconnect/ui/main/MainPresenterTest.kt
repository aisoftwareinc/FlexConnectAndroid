package com.aisoftware.flexconnect.ui.main

import android.support.test.filters.SmallTest
import com.aisoftware.flexconnect.util.SharedPrefUtil
import junit.framework.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify

@SmallTest
class MainPresenterTest {

    private lateinit var presenter: MainPresenterImpl
    private lateinit var view: MainView
    private lateinit var interactor: MainInteractor
    private lateinit var sharedPrefUtil: SharedPrefUtil

    @Before
    fun setUp() {
        sharedPrefUtil = Mockito.mock(SharedPrefUtil::class.java)
        assertNotNull(sharedPrefUtil)

        view = Mockito.mock(MainView::class.java)
        assertNotNull(view)

        interactor = Mockito.mock(MainInteractor::class.java)
        assertNotNull(interactor)

        presenter = MainPresenterImpl(view, interactor, sharedPrefUtil)
        assertNotNull(presenter)
    }

    @Test
    fun testInitializeLoading() {
        `when`(sharedPrefUtil.userPrefExists()).thenReturn(true)
        presenter.initialize()
        verify(view).initializeViewLoading()
        verify(view).navigateToDashboard()
    }

    @Test
    fun testInitializeDefault() {
        `when`(sharedPrefUtil.userPrefExists()).thenReturn(false)
        presenter.initialize()
        verify(view).initializeViewDefault()
    }

    @Test
    fun testSubmitClickedAuthFlow() {
        `when`(view.isNetworkAvailable()).thenReturn(true)
        val phoneNumber = "9999999999"

        presenter.submitClicked("", phoneNumber)
        verify(view).isNetworkAvailable()
        verify(interactor).fetchAuthCode(phoneNumber)
        verify(interactor).fetchTimerInterval()
    }

    @Test
    fun testSubmitClickedAuthFlowNoNetwork() {
        `when`(view.isNetworkAvailable()).thenReturn(false)
        val phoneNumber = "9999999999"

        presenter.submitClicked("", phoneNumber)
        verify(view).isNetworkAvailable()
        verify(view).showNetworkAvailabilityError()
    }

    @Test
    fun testSubmitClickedAuthFlowInvalidPhone() {
        `when`(view.isNetworkAvailable()).thenReturn(true)
        val phoneNumber = ""

        presenter.submitClicked("", phoneNumber)
        verify(view).showErrorDialog()
    }

    @Test
    fun testSubmitClickedDashboardFlow() {
        val phoneNumber = "9999999999"
        val authCode = "12345"

        `when`(view.isNetworkAvailable()).thenReturn(true)
        presenter.submitClicked("", phoneNumber)
        presenter.onAuthFetchSuccess(authCode, phoneNumber)


        presenter.submitClicked(authCode, phoneNumber)
        verify(view).navigateToDashboard()
    }

    @Test
    fun testSubmitClickedDashboardFlowFailedAuth() {
        val phoneNumber = "9999999999"
        val authCode = "12345"

        `when`(view.isNetworkAvailable()).thenReturn(true)
        presenter.submitClicked("", phoneNumber)
        presenter.onAuthFetchSuccess(authCode, phoneNumber)

        presenter.submitClicked("0000", phoneNumber)
        verify(view).initializeViewDefault()
        verify(view).showErrorDialog()
    }

    @Test
    fun testOnAuthFetchSuccess() {
        val phoneNumber = "9999999999"
        val authCode = "12345"

        presenter.onAuthFetchSuccess(authCode, phoneNumber)
        verify(view).showAuthCodeInput(true)
    }

    @Test
    fun testOnFetchFailure() {
        presenter.onFetchFailure("")
        verify(view).showErrorDialog()
    }
}