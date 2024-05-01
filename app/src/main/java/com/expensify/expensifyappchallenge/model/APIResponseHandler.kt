package com.expensify.expensifyappchallenge.model

import java.io.Serializable

class APIResponseHandler : Serializable
{
    var code: Int = -1
    var message: String? = null
}