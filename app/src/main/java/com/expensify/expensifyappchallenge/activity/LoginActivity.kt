package com.expensify.expensifyappchallenge.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.expensify.expensifyappchallenge.R
import com.expensify.expensifyappchallenge.constants.APIConstants
import com.expensify.expensifyappchallenge.constants.StringConstants
import com.expensify.expensifyappchallenge.model.APIResponseHandler
import com.expensify.expensifyappchallenge.service.APIRequestCallback
import com.expensify.expensifyappchallenge.service.APIService
import com.expensify.expensifyappchallenge.util.ExpensifyUtil
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: MainActivity(), APIRequestCallback
{
    lateinit var mAPIService: APIService

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        initViews()
    }

    private fun initViews()
    {
        mAPIService = APIService(this, this) // API Request Handler is initialized
        initListeners()
    }

    private fun initListeners()
    {
        username?.addTextChangedListener { userNamePasswordTextWatcher() }
        password?.addTextChangedListener { userNamePasswordTextWatcher() }
        login_button?.setOnClickListener { onLoginClick() }
    }

    // Login button is Enabled iff both Email and Password is entered by the User.
    private fun userNamePasswordTextWatcher()
    {
        login_button?.isEnabled = !TextUtils.isEmpty(username.text.toString().trim()) && !TextUtils.isEmpty(password.text.toString().trim())
    }

    private fun showLoadingScreen(isShowLoading: Boolean = true)
    {
        if (isShowLoading)
        {
            screen_overlay?.visibility = View.VISIBLE
            loading_progress_bar?.visibility = View.VISIBLE
            signin_in_hint?.visibility = View.VISIBLE

            username?.isEnabled = false
            password?.isEnabled = false
            login_button?.isEnabled = false
        }
        else
        {
            screen_overlay?.visibility = View.GONE
            loading_progress_bar?.visibility = View.GONE
            signin_in_hint?.visibility = View.GONE

            username?.isEnabled = true
            password?.isEnabled = true
            userNamePasswordTextWatcher()
        }
    }

    private fun onLoginClick()
    {
        if (ExpensifyUtil.haveNetworkConnection(this))
        {
            showLoadingScreen()

            val additionalParams = "command=Authenticate" + "&partnerName=${StringConstants.partnerName}" + "&partnerPassword=${StringConstants.partnerPassword}" +
                    "&partnerUserID=${username.text}" + "&partnerUserSecret=${password.text}"

            mAPIService.sendPOSTRequest(APIConstants.LOGIN_REQUEST, "api", additionalParams)
        }
        else
        {
            Toast.makeText(this, R.string.check_your_network_connection_message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun notifySuccessResponse(apiConstant: Int, response: String)
    {
        showLoadingScreen(false)

        when (apiConstant)
        {
            APIConstants.LOGIN_REQUEST  ->
            {
                val intent = Intent(this, ExpenseListActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

    }

    override fun notifyErrorResponse(apiConstant: Int, responseHolder: APIResponseHandler)
    {
        showLoadingScreen(false)

        showAPIErrorMessage(responseHolder.code, responseHolder.message)
    }
}