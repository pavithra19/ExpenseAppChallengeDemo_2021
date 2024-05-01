package com.expensify.expensifyappchallenge.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.expensify.expensifyappchallenge.constants.PreferenceConstants

class AppLauncherActivity : MainActivity()
{
    override fun onCreate(savedInstanceState: Bundle?){

        super.onCreate(savedInstanceState)

        init()
    }

    private fun init()
    {
        Handler(Looper.getMainLooper()).postDelayed({

            // If Authtoken is not present then Login screen is shown.
            if (TextUtils.isEmpty(mPref.getString(PreferenceConstants.AUTHTOKEN, "")))
            {
                switchToLoginScreen()
                finish()
            }
            else
            {
                val intent = Intent(this, ExpenseListActivity::class.java)
                startActivity(intent)
                finish()
            }

        }, 1000) //Application is opened after a second of showing splash screen.
    }
}