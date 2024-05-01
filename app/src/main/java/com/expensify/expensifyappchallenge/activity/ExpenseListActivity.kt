package com.expensify.expensifyappchallenge.activity

import android.content.Intent
import android.database.Cursor
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.Toolbar
import com.expensify.expensifyappchallenge.R
import com.expensify.expensifyappchallenge.adapter.ListCursorAdapter
import com.expensify.expensifyappchallenge.constants.APIConstants
import com.expensify.expensifyappchallenge.constants.PreferenceConstants
import com.expensify.expensifyappchallenge.database.DatabaseContract
import com.expensify.expensifyappchallenge.database.DatabaseQueryHandler
import com.expensify.expensifyappchallenge.model.APIResponseHandler
import com.expensify.expensifyappchallenge.service.APIRequestCallback
import com.expensify.expensifyappchallenge.service.APIService
import com.expensify.expensifyappchallenge.util.ExpensifyUtil
import kotlinx.android.synthetic.main.common_list_screen.*
import kotlinx.android.synthetic.main.list_screen_toolbar.*
import java.util.*

class ExpenseListActivity : MainActivity(), APIRequestCallback, DatabaseQueryHandler.AsyncQueryListener
{
    private var mToolbar: Toolbar? = null
    lateinit var filterArray: Array<String>

    private var expenseFilterViewPosition = 0

    lateinit var mAPIService: APIService
    lateinit var mQueryHandler: DatabaseQueryHandler

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.common_list_screen)

        init()
    }

    // API Request and DB Query Handler is initialized
    private fun init()
    {
        mAPIService = APIService(this, this)
        mQueryHandler = DatabaseQueryHandler(contentResolver, this)

        initViews()
    }

    private fun initViews()
    {
        swipe_refresh_layout?.setOnRefreshListener { refreshExpenseListView() }
        add_expense_button?.setOnClickListener { createExpenseClick() }
        add_expense_fab?.setOnClickListener { createExpenseClick() }

        setupToolbar()
        setupToolbarSpinner()
    }

    private fun createExpenseClick()
    {
        val intent = Intent(this, CreateExpenseActivity::class.java)
        startActivity(intent)
    }

    private fun setupToolbar()
    {
        mToolbar = toolbar as? Toolbar
        setSupportActionBar(mToolbar)
        actionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupToolbarSpinner()
    {
        val spinnerContainer: View = LayoutInflater.from(this).inflate(R.layout.list_screen_toolbar, mToolbar, false)
        val layoutParams = ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mToolbar?.addView(spinnerContainer, layoutParams)

        filterArray = resources.getStringArray(R.array.expense_filter)

        val filterSpinner = spinnerContainer.findViewById<View>(R.id.toolbar_spinner) as AppCompatSpinner
        filterSpinner.adapter = CustomSpinnerAdapter()

        toolbar_spinner?.onItemSelectedListener = onSelectFilterListener
    }

    // Spinner Title and Dropdown view is customized
    inner class CustomSpinnerAdapter: BaseAdapter()
    {
        override fun getCount(): Int
        {
            return filterArray.size
        }

        override fun getItem(position: Int): Any
        {
            return filterArray[position]
        }

        override fun getItemId(position: Int): Long
        {
            return position.toLong()
        }

        override fun getDropDownView(position: Int, view: View?, parent: ViewGroup?): View?
        {
            var itemView = view

            if (itemView == null || itemView.tag.toString() != "DROPDOWN")
            {
                itemView = layoutInflater.inflate(R.layout.dropdown_item_custom_view, parent, false)
                itemView.tag = "DROPDOWN"
            }

            val textView = itemView?.findViewById<TextView>(android.R.id.text1)
            textView?.text = filterArray[position]

            // To Highlight the current selected filter in Dropdown view
            textView?.typeface = if (expenseFilterViewPosition == position) Typeface.DEFAULT_BOLD else Typeface.DEFAULT

            return itemView
        }

        override fun getView(position: Int, view: View?, parent: ViewGroup?): View?
        {
            var itemView = view
            if (itemView == null || itemView.tag.toString() != "NON_DROPDOWN")
            {
                itemView = layoutInflater.inflate(R.layout.toolbar_spinner_title, parent, false)
                itemView.tag = "NON_DROPDOWN"
            }

            itemView?.findViewById<TextView>(android.R.id.text1)?.text = filterArray[position]

            return itemView
        }
    }

    private var onSelectFilterListener = object : AdapterView.OnItemSelectedListener
    {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long)
        {
            expenseFilterViewPosition = pos
            refreshExpenseListView(false)
        }

        override fun onNothingSelected(p0: AdapterView<*>?)
        {

        }
    }

    // "isFromSwipeToRefresh" = To differentiate the loading view this variable is used.
    private fun refreshExpenseListView(isFromSwipeToRefresh: Boolean = true)
    {
        if (ExpensifyUtil.haveNetworkConnection(this))
        {
            if (isFromSwipeToRefresh)
            {
                swipe_refresh_layout?.isRefreshing = true
            }
            else
            {
                showHideProgressBar()
            }

            when (expenseFilterViewPosition)
            {
                0 -> {
                    getExpensesListFromAPI(getDate(null))
                }

                1 -> {
                    getExpensesListFromAPI(getDate(1))
                }

                2 -> {
                    getExpensesListFromAPI(getDate(7))
                }

                3 -> {
                    getExpensesListFromAPI(getDate(30))
                }
            }
        }
        else
        {
            // If there is no internet connection on launch of this activity, Error view is shown instead of Toast message.
            if (swipe_refresh_layout?.visibility == View.GONE && add_expense_button?.visibility == View.GONE)
            {
                internet_connectivity_error_message?.visibility = View.VISIBLE
            }
            else
            {
                Toast.makeText(this, R.string.check_your_network_connection_message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDate(days: Int?): Array<Int>
    {
        val calendar = Calendar.getInstance()

        if (days != null)
        {
            calendar.add(Calendar.DAY_OF_YEAR, -days)
        }

        val dateArray = Array(3){0}

        dateArray[0] = calendar.get(Calendar.YEAR)
        dateArray[1] = calendar.get(Calendar.MONTH) + 1
        dateArray[2] = calendar.get(Calendar.DAY_OF_MONTH)

        return dateArray
    }

    private fun showHideProgressBar(isShow: Boolean = true)
    {
        if (isShow)
        {
            loading_progress_bar?.visibility = View.VISIBLE
            add_expense_button?.visibility = View.GONE
            add_expense_fab?.visibility = View.GONE
            swipe_refresh_layout?.visibility = View.GONE
            list_view?.visibility = View.GONE
        }
        else
        {
            loading_progress_bar?.visibility = View.GONE
        }

        internet_connectivity_error_message?.visibility = View.GONE
    }

    private fun showListView(isShowEmptyView: Boolean = true)
    {
        if (isShowEmptyView)
        {
            add_expense_button?.visibility = View.VISIBLE

            add_expense_fab?.visibility = View.GONE
            swipe_refresh_layout?.visibility = View.GONE
            list_view?.visibility = View.GONE
        }
        else
        {
            swipe_refresh_layout?.visibility = View.VISIBLE
            list_view?.visibility = View.VISIBLE
            add_expense_fab?.visibility = View.VISIBLE

            add_expense_button?.visibility = View.GONE
        }
    }

    private fun getExpensesListFromAPI(startDate: Array<Int>?)
    {
        val additionalParams = "command=Get" + "&authToken=${mPref.getString(PreferenceConstants.AUTHTOKEN, "")}" + "&returnValueList=transactionList"

        var startDateParam = ""

        if (startDate != null)
        {
            startDateParam = "&startDate=${startDate[0]}-${String.format("%02d", startDate[1])}-${String.format("%02d", startDate[2])}"
        }

        val todayDate = getDate(null)
        val endDateParam = "&endDate=${todayDate[0]}-${String.format("%02d", todayDate[1])}-${String.format("%02d", todayDate[2])}"

        mAPIService.sendGETRequest(APIConstants.EXPENSES_LIST, "api", additionalParams + startDateParam + endDateParam)
    }

    private fun fetchListFromDB()
    {
        mQueryHandler.startQuery(DatabaseContract.Expenses.CONTENT_URI, null)
    }

    override fun onQueryComplete(token: Int, cookie: Any?, cursor: Cursor?)
    {
        showHideProgressBar(false)

        if (cursor?.count == 0) // No Expenses available from the API response
        {
            showListView()
        }
        else
        {
            showListView(false)

            list_view.adapter = ListCursorAdapter(this, cursor)
        }
    }

    override fun notifySuccessResponse(apiConstant: Int, response: String)
    {
        swipe_refresh_layout?.isRefreshing = false

        when (apiConstant)
        {
            APIConstants.EXPENSES_LIST ->
            {
                fetchListFromDB()
            }
        }
    }

    override fun notifyErrorResponse(apiConstant: Int, responseHolder: APIResponseHandler)
    {
        showHideProgressBar(false)
        swipe_refresh_layout?.isRefreshing = false

        showAPIErrorMessage(responseHolder.code, responseHolder.message)
    }
}