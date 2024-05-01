package com.expensify.expensifyappchallenge.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.expensify.expensifyappchallenge.constants.PreferenceConstants
import com.expensify.expensifyappchallenge.database.DatabaseContract

open class MainActivity: AppCompatActivity()
{
    protected lateinit var mPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        mPref = getSharedPreferences("ServicePrefs", Context.MODE_PRIVATE)
    }

    fun showAPIErrorMessage(errorCode: Int = -1, errorMsg: String?)
    {
        if (errorCode == 407) // Authtoken revoked Error code.
        {
            mPref.edit().putString(PreferenceConstants.AUTHTOKEN, "").apply()
            resetAppData()
            switchToLoginScreen()
        }
        else
        {
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
        }
    }

    fun switchToLoginScreen()
    {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    // Resetting Shared Preference values and deleting database contents on session expired cases
    private fun resetAppData()
    {
        val prefs = getSharedPreferences("ServicePrefs", Context.MODE_PRIVATE)
        val edit = prefs.edit()
        edit.clear()
        edit.commit()

        contentResolver.delete(DatabaseContract.Expenses.CONTENT_URI, null, null)
    }
}