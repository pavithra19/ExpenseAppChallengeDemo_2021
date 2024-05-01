package com.expensify.expensifyappchallenge.service

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.expensify.expensifyappchallenge.R
import com.expensify.expensifyappchallenge.common.ExpensifyApplication
import com.expensify.expensifyappchallenge.constants.APIConstants
import com.expensify.expensifyappchallenge.constants.PreferenceConstants
import com.expensify.expensifyappchallenge.database.DataPersistHelper
import com.expensify.expensifyappchallenge.model.APIResponseHandler
import com.expensify.expensifyappchallenge.model.Transaction
import org.json.JSONException
import org.json.JSONObject


open class APIService(requestCallback: APIRequestCallback, context: Context) : APIRequestHandler
{
    //API request connection timeout is set as 2 minutes.
    private val MY_SOCKET_TIMEOUT : Int = 120 * 1000 // (in milliseconds)

    private val mAPIRequestCallback : APIRequestCallback = requestCallback
    private val mContext : Context = context

    private val domainUrl = "https://www.expensify.com"

    private fun<T> getResultObjfromJson(json: String, classOfT: Class<T>): T
    {
        return ExpensifyApplication.gson.fromJson<T>(json, classOfT)
    }

    override fun sendGETRequest(apiConstant: Int, url: String, additionalParams: String)
    {
        executeRequest(apiConstant, Request.Method.GET, constructRequestUrl(url, additionalParams))
    }

    override fun sendPOSTRequest(apiConstant: Int, url: String, additionalParams: String)
    {
        executeRequest(apiConstant, Request.Method.POST, constructRequestUrl(url, additionalParams))
    }

    // Url is constructed here along with domain, endpoint and additional params.
    private fun constructRequestUrl(url: String, additionalParams: String): String
    {
        val requestUrl = StringBuilder(domainUrl)
        requestUrl.append("/$url")
        requestUrl.append(("?"))
        requestUrl.append(additionalParams)

        return requestUrl.toString()
    }

    private fun executeRequest(apiConstant: Int, method: Int, url: String)
    {
        val request =  object : StringRequest(method, url,

            Response.Listener
            { response ->

                val jsonObj = JSONObject(response)
                val responseHolder = APIResponseHandler()
                responseHolder.code = jsonObj.optInt("jsonCode")
                responseHolder.message = jsonObj.optString("message")

                if (responseHolder.code > 200) // If status code is not success from the server, error handling is done.
                {
                    mAPIRequestCallback.notifyErrorResponse(apiConstant, responseHolder)
                }
                else
                {
                    parseSuccessResponse(apiConstant, response)
                }
            },

            Response.ErrorListener { error ->

                // Volley exception like Network, Timeout and other exceptions are handled here
                parseErrorResponse(apiConstant, error)
            })
        {}

        request.retryPolicy = DefaultRetryPolicy(MY_SOCKET_TIMEOUT, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        ExpensifyApplication.getInstance().requestQueue.add(request) // API Request is added to the volley request queue.
    }

    fun parseSuccessResponse(apiConstant: Int, response: String)
    {
        when (apiConstant)
        {
            APIConstants.LOGIN_REQUEST  ->
            {
                val jsonObj = JSONObject(response)
                val mPref = mContext.getSharedPreferences("ServicePrefs", Context.MODE_PRIVATE)

                // Authtoken is saved in the Shared preference of the application.
                mPref.edit().putString(PreferenceConstants.AUTHTOKEN, jsonObj.optString("authToken")).apply()
            }

            APIConstants.EXPENSES_LIST, APIConstants.CREATE_EXPENSE  ->
            {
                val expenseList: Transaction = getResultObjfromJson(response, Transaction::class.java)

                // Expenses are persisted in the Database here
                DataPersistHelper.persistExpensesList(expenseList, mContext.contentResolver, apiConstant == APIConstants.EXPENSES_LIST)
            }
        }

        mAPIRequestCallback.notifySuccessResponse(apiConstant, response)
    }

    fun parseErrorResponse(apiconstant: Int, error: VolleyError)
    {
        var errorCode = 0
        var errorMsg: String = mContext.getString(R.string.network_connection_error_message)

        if (error.networkResponse != null)
        {
            if (error.networkResponse.data != null)
            {
                try
                {
                    val json  = JSONObject(String(error.networkResponse.data))

                    errorCode = json.optInt("jsonCode")
                    errorMsg = json.optString("message")

                }
                catch (e: JSONException)
                {
                }
            }
        }

        val responseHolder = APIResponseHandler()
        responseHolder.code = errorCode
        responseHolder.message = errorMsg

        mAPIRequestCallback.notifyErrorResponse(apiconstant, responseHolder)
    }

}