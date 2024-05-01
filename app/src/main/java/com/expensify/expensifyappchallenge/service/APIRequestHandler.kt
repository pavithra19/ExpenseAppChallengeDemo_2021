package com.expensify.expensifyappchallenge.service

import com.expensify.expensifyappchallenge.model.APIResponseHandler

// API is initiated from the activity to Service class.
interface APIRequestHandler
{
    fun sendGETRequest(apiConstant: Int, url: String, additionalParams: String)
    fun sendPOSTRequest(apiConstant: Int, url: String, additionalParams: String)
}

// Callback to the API initiated activity once Response is received.
interface APIRequestCallback
{
    fun notifySuccessResponse(apiConstant: Int, response: String)
    fun notifyErrorResponse(apiConstant: Int, responseHolder : APIResponseHandler)
}