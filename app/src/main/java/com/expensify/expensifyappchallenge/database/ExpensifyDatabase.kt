package com.expensify.expensifyappchallenge.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class ExpensifyDatabase(context: Context) : SQLiteOpenHelper(context, "ExpensifyDatabase.db", null, 1)
{
    override fun onCreate(db: SQLiteDatabase?)
    {
        db?.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.EXPENSES_LIST + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DatabaseContract.Expenses.TRANSACTION_ID + " TEXT NOT NULL,"
                + DatabaseContract.Expenses.CREATED_DATE + " TEXT,"
                + DatabaseContract.Expenses.MERCHANT + " TEXT,"
                + DatabaseContract.Expenses.AMOUNT + " LONG,"
                + "UNIQUE (" + DatabaseContract.Expenses.TRANSACTION_ID + ") ON CONFLICT REPLACE)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int)
    {
        db?.execSQL("DROP TABLE IF EXISTS " + Tables.EXPENSES_LIST)
        onCreate(db)
    }

    internal interface Tables
    {
        companion object
        {
            const val EXPENSES_LIST = "expenses_list"
        }
    }
}