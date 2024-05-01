package com.expensify.expensifyappchallenge.adapter

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView
import com.expensify.expensifyappchallenge.R
import com.expensify.expensifyappchallenge.database.DatabaseContract
import kotlinx.android.synthetic.main.expense_list_component.view.*
import java.text.DecimalFormat

class ListCursorAdapter(context: Context?, cursor: Cursor?) : CursorAdapter(context, cursor, androidx.cursoradapter.widget.CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)
{
    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View
    {
        val view: View = LayoutInflater.from(context).inflate(R.layout.expense_list_component, null)
        view.tag = getViewForItem(view)
        return view
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?)
    {
        val holder = view?.tag as ViewHolder

        holder.expense_date.text = cursor?.getString(cursor.getColumnIndex(DatabaseContract.Expenses.CREATED_DATE))
        holder.merchant_name.text = cursor?.getString(cursor.getColumnIndex(DatabaseContract.Expenses.MERCHANT))

        val amountInDollars = cursor?.getLong(cursor.getColumnIndex(DatabaseContract.Expenses.AMOUNT))!!.toDouble().div(100)
        val amountFormat = DecimalFormat("#,###,##0.00")

        if (amountInDollars < 0) // Positive expense amount is sent as negative from the server so converting it to a positive amount
        {
            holder.expense_amount.text = "$${amountFormat.format(amountInDollars * -1)}"
        }
        else if (amountInDollars > 0) // Negative expense amount is sent as positive from the server so enclosing with parentheses
        {
            holder.expense_amount.text = "$(${amountFormat.format(amountInDollars)})"
        }
        else
        {
            holder.expense_amount.text = "$0.00"
        }
    }

    private fun getViewForItem(view: View) : ViewHolder
    {
        val holder = ViewHolder()

        holder.expense_date = view.expense_date
        holder.merchant_name = view.merchant_name
        holder.expense_amount = view.expense_amount

        return holder
    }

    class ViewHolder
    {
        lateinit var expense_date: TextView
        lateinit var merchant_name: TextView
        lateinit var expense_amount: TextView
    }

}