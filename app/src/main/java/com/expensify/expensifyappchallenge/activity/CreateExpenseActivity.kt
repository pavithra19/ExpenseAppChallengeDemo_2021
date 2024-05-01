package com.expensify.expensifyappchallenge.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputFilter
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.expensify.expensifyappchallenge.R
import com.expensify.expensifyappchallenge.constants.APIConstants
import com.expensify.expensifyappchallenge.constants.PreferenceConstants
import com.expensify.expensifyappchallenge.constants.StringConstants
import com.expensify.expensifyappchallenge.model.APIResponseHandler
import com.expensify.expensifyappchallenge.model.Expense
import com.expensify.expensifyappchallenge.service.APIRequestCallback
import com.expensify.expensifyappchallenge.service.APIService
import com.expensify.expensifyappchallenge.util.ExpensifyUtil
import com.expensify.expensifyappchallenge.viewfilter.EditTextInputFilter
import kotlinx.android.synthetic.main.activity_create_expense.*
import kotlinx.android.synthetic.main.common_toolbar.toolbar
import java.math.BigDecimal
import java.util.*
import java.util.regex.Pattern


class CreateExpenseActivity: MainActivity(), APIRequestCallback
{
    lateinit var mAPIService: APIService

    private var expenseObj: Expense = Expense()

    private var expenseYear: Int = 0
    private var expenseMonth: Int = 0
    private var expenseDay: Int = 0

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_create_expense)

        if (savedInstanceState != null)
        {
            expenseObj = savedInstanceState.getSerializable(StringConstants.expenseObj) as Expense
        }

        init()
    }

    override fun onSaveInstanceState(outState: Bundle)
    {
        super.onSaveInstanceState(outState)

        saveExpenseObj(false) // Saving user entered data in Expense object to maintain the App state.

        outState.putSerializable(StringConstants.expenseObj, expenseObj)
    }

    private fun init()
    {
        // API Request Handler is initialized
        mAPIService = APIService(this, this)

        // Numeric limitation for Expense Amount field is set
        expense_amount.filters = arrayOf<InputFilter>(EditTextInputFilter(8, 2))

        setupToolbar()
        updateExpenseObjView()
    }

    private fun setupToolbar()
    {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Close icon is overridden in place of default back button.
        val backButton = ContextCompat.getDrawable(this, R.drawable.ic_close)
        backButton?.setTint(ContextCompat.getColor(this, R.color.white))//, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(backButton)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        dismissKeyboard()

        val itemId = item.itemId

        if (itemId == android.R.id.home)
        {
            finish()
        }
        else if (itemId == 1)
        {
            saveExpenseObj()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menu?.add(Menu.NONE, 1, Menu.NONE, resources.getString(R.string.save))?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return super.onCreateOptionsMenu(menu)
    }

    private fun dismissKeyboard()
    {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    private fun initExpenseDateView()
    {
        if (TextUtils.isEmpty(expenseObj.created))
        {
            getTodayDate() // By default, current date is set in the Expense date field.
            expense_date?.text = "$expenseYear-${String.format("%02d", expenseMonth)}-${String.format("%02d", expenseDay)}"
            expenseObj.created = expense_date?.text.toString()
        }
        else
        {
            expense_date?.text = expenseObj.created
            val date = expenseObj.created?.split("-".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()

            expenseYear = Integer.parseInt(date?.get(0))
            expenseMonth = Integer.parseInt(date?.get(1))
            expenseDay = Integer.parseInt(date?.get(2))
        }

        expense_date?.setOnClickListener { onExpenseDateClick() }
    }

    // Updating values of Expense object in the respective fields
    private fun updateExpenseObjView()
    {
        initExpenseDateView()

        if (!TextUtils.isEmpty(expenseObj.merchant))
        {
            merchant_name?.setText(expenseObj.merchant)
        }

        if (!TextUtils.isEmpty(expenseObj.amount.toString()))
        {
            expense_amount?.setText(expenseObj.amount.toString())
        }
    }

    private fun onExpenseDateClick()
    {
        val mDatePickerDialog = DatePickerDialog(this, expenseDateListener, expenseYear, expenseMonth - 1, expenseDay)
        mDatePickerDialog.show()
    }

    private val expenseDateListener = DatePickerDialog.OnDateSetListener { view, calYear, monthOfYear, dayOfMonth ->

        expenseYear = calYear
        expenseMonth = monthOfYear + 1
        expenseDay = dayOfMonth

        expense_date?.text = "$expenseYear-${String.format("%02d", expenseMonth)}-${String.format("%02d", expenseDay)}"
        expenseObj.created = expense_date?.text.toString()
    }

    private fun getTodayDate()
    {
        val calendar = Calendar.getInstance()

        expenseYear = calendar.get(Calendar.YEAR)
        expenseMonth = calendar.get(Calendar.MONTH) + 1
        expenseDay = calendar.get(Calendar.DAY_OF_MONTH)
    }

    /*
    ** "uploadExpense" = Expense object will be saved for SaveInstanceState and
     ** while uploading data to the server. To differentiate the case this variable is used.
     */
    private fun saveExpenseObj(uploadExpense: Boolean = true)
    {
        var canSave = true

        if (TextUtils.isEmpty(merchant_name.text.toString()))
        {
            if (uploadExpense)
            {
                Toast.makeText(this, resources.getString(R.string.validate_merchant_name), Toast.LENGTH_SHORT).show()
                canSave = false
            }
        }
        else
        {
            expenseObj.merchant = merchant_name.text.toString()
        }

        if (isValidAmount(expense_amount.text.toString()))
        {
            expenseObj.amount = convertToCents()
        }
        else
        {
            expenseObj.amount = 0
            expense_amount?.setText("0.00")
        }

        if (uploadExpense && canSave)
        {
            callExpenseSaveAPI()
        }
    }

    // Server accepts Expense amount in 'Cents' so converting the amount entered by the user.
    private fun convertToCents(): Long
    {
        val amount = expense_amount?.text.toString()
        return (BigDecimal(amount) * BigDecimal(100)).toLong()
    }

    private fun callExpenseSaveAPI()
    {
        if (ExpensifyUtil.haveNetworkConnection(this))
        {
            showHideProgressBar()

            val additionalParams = "command=CreateTransaction" + "&authToken=${mPref.getString(PreferenceConstants.AUTHTOKEN, "")}" +
                    "&created=${expenseObj.created}&amount=${expenseObj.amount}&merchant=${expenseObj.merchant}"

            mAPIService.sendPOSTRequest(APIConstants.CREATE_EXPENSE, "api", additionalParams)
        }
        else
        {
            Toast.makeText(this, R.string.check_your_network_connection_message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidAmount(value: String): Boolean
    {
        if (!TextUtils.isEmpty(value.trim()))
        {
            try
            {
                // If 0 is entered by the user then Amount is modified as "0.00"
                if (value.compareTo(BigDecimal.ZERO.toString()) == 0)
                {
                    return false
                }

                val pattern = Pattern.compile("-?\\d+(\\.\\d+)?") // Check if any value is entered except "-" or "."
                if(!pattern.matcher(value).matches())
                {
                    return false
                }
            }
            catch (exception: NumberFormatException)
            {
                return false
            }
        }
        else
        {
            return false
        }

        return true
    }

    private fun showHideProgressBar(isShow: Boolean = true)
    {
        loading_progress_bar?.visibility = if (isShow) View.VISIBLE else View.GONE
        root_view?.visibility = if (isShow) View.GONE else View.GONE
    }

    override fun notifySuccessResponse(apiConstant: Int, response: String)
    {
        showHideProgressBar(false)
        finish()
    }

    override fun notifyErrorResponse(apiConstant: Int, responseHolder: APIResponseHandler)
    {
        showAPIErrorMessage(responseHolder.code, responseHolder.message)
    }
}