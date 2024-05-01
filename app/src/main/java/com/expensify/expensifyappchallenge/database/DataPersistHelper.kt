package com.expensify.expensifyappchallenge.database

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.OperationApplicationException
import android.os.RemoteException
import com.expensify.expensifyappchallenge.model.Expense
import com.expensify.expensifyappchallenge.model.Transaction
import java.util.*

object DataPersistHelper
{
    fun persistExpensesList(response: Transaction, resolver: ContentResolver, isRefreshDB: Boolean)
    {
        val batch = ArrayList<ContentProviderOperation>()

        if (isRefreshDB) //Database is refreshed each time Expenses list API is fetched.
        {
            resolver.delete(DatabaseContract.Expenses.CONTENT_URI, null, null)
        }

        if (response.transactionList?.size ?: 0 > 0)
        {
            for (expense in response.transactionList!!)
            {
                batch.add(buildSingleExpenseObj(expense).build())
            }
        }

        apply(resolver, batch)
    }

    private fun buildSingleExpenseObj(expense: Expense): ContentProviderOperation.Builder
    {
        val builder = ContentProviderOperation.newInsert(DatabaseContract.Expenses.CONTENT_URI)

        builder.withValue(DatabaseContract.Expenses.TRANSACTION_ID, expense.transactionID)
        builder.withValue(DatabaseContract.Expenses.CREATED_DATE, expense.created)
        builder.withValue(DatabaseContract.Expenses.MERCHANT, expense.merchant)
        builder.withValue(DatabaseContract.Expenses.AMOUNT, expense.amount)

        return builder
    }

    private fun apply(resolver: ContentResolver, batch: ArrayList<ContentProviderOperation>)
    {
        try
        {
            resolver.applyBatch("com.expensify.expensifyappchallenge", batch)
        }
        catch (exception: RemoteException)
        {
            throw RuntimeException("Problem applying batch operation", exception)
        }
        catch (exception: OperationApplicationException)
        {
            throw RuntimeException("Problem applying batch operation", exception)
        }

    }
}