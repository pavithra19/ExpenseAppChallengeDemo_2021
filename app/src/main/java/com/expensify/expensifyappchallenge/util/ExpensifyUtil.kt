package com.expensify.expensifyappchallenge.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object ExpensifyUtil
{
    fun haveNetworkConnection(context: Context): Boolean
    {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        if (connectivityManager != null)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork);
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                    {
                        return true
                    }
                }
            }
            else
            {
                val networkInfo = connectivityManager.activeNetworkInfo
                return networkInfo != null && networkInfo.isConnected
            }
        }

        return false
    }
}