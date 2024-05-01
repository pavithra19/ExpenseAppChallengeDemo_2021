package com.expensify.expensifyappchallenge.database

import android.net.Uri
import android.provider.BaseColumns

object DatabaseContract
{
    val BASE_CONTENT_URI = Uri.parse("content://com.expensify.expensifyappchallenge")

    internal val PATH_EXPENSE_LIST           =   "path_expense_list"

    internal interface ExpenseColumns
    {
        val CREATED_DATE get() = "created_date"
        val MERCHANT get() = "merchant"
        val AMOUNT get() = "amount"
        val TRANSACTION_ID get() = "transaction_id"
    }

    object Expenses : ExpenseColumns, BaseColumns
    {
        val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EXPENSE_LIST).build()
        const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.expensify.expenses"

        fun buildUri(categoryId: String?): Uri
        {
            return CONTENT_URI.buildUpon().appendPath(categoryId).build()
        }
    }
}