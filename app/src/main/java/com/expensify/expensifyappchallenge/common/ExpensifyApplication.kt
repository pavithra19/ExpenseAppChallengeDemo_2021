package com.expensify.expensifyappchallenge.common

import android.app.Application
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.gson.Gson

class ExpensifyApplication: Application()
{
    lateinit var requestQueue: RequestQueue

    override fun onCreate()
    {
        super.onCreate()

        init()
    }

    private fun init()
    {
        mInstance = this
        requestQueue = Volley.newRequestQueue(applicationContext)
    }

    companion object
    {
        lateinit var mInstance: ExpensifyApplication
        var gson = Gson()

        fun getInstance(): ExpensifyApplication
        {
            return mInstance
        }
    }
}