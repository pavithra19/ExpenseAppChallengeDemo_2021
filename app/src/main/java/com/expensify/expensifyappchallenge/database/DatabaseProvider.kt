package com.expensify.expensifyappchallenge.database

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri

class DatabaseProvider: ContentProvider()
{
    private lateinit var mDBHelper: ExpensifyDatabase
    private val URIMATCHER = buildUriMatcher()

    override fun onCreate(): Boolean
    {
        mDBHelper = ExpensifyDatabase(context!!)
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor
    {
        val db = mDBHelper.readableDatabase

        val match = URIMATCHER.match(uri)

        val builder = buildExpandedSelection(uri, match)
        return if(selectionArgs != null) builder.where(selection, * selectionArgs).query(db, projection, sortOrder) else builder.where(selection, * emptyArray()).query(db, projection, sortOrder)
    }

    override fun getType(uri: Uri): String
    {
        when (URIMATCHER.match(uri))
        {
            EXPENSES_LIST -> return DatabaseContract.Expenses.CONTENT_TYPE

            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri
    {
        val db = mDBHelper.writableDatabase
        when (URIMATCHER.match(uri))
        {
            EXPENSES_LIST ->
            {
                db?.insertOrThrow(ExpensifyDatabase.Tables.EXPENSES_LIST, null, values)
                return DatabaseContract.Expenses.buildUri(values?.getAsString(DatabaseContract.Expenses.TRANSACTION_ID))
            }
            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int
    {
        val db = mDBHelper.writableDatabase
        val builder = buildSimpleSelection(uri)

        return if(selectionArgs != null) builder.where(selection, * selectionArgs).delete(db) else builder.where(selection, * emptyArray()).delete(db)
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int
    {
        val db = mDBHelper.writableDatabase
        val builder = buildSimpleSelection(uri)
        return if(selectionArgs != null) builder.where(selection, * selectionArgs).update(db, values) else builder.where(selection, * emptyArray()).update(db, values)
    }

    private fun buildUriMatcher(): UriMatcher
    {
        val matcher = UriMatcher(UriMatcher.NO_MATCH)
        val authority = "com.expensify.expensifyappchallenge"

        matcher.addURI(authority, DatabaseContract.PATH_EXPENSE_LIST, EXPENSES_LIST)

        return matcher
    }

    private fun buildExpandedSelection(uri: Uri, match: Int): SelectionBuilder
    {
        val builder = SelectionBuilder()

        when (match)
        {
            EXPENSES_LIST   ->
            {
                return builder.table(ExpensifyDatabase.Tables.EXPENSES_LIST)
            }

            else    -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }
    }

    private fun buildSimpleSelection(uri: Uri): SelectionBuilder
    {
        val builder = SelectionBuilder()

        when (URIMATCHER.match(uri))
        {
            EXPENSES_LIST   ->
            {
                return builder.table(ExpensifyDatabase.Tables.EXPENSES_LIST)
            }

            else    -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }
    }

    companion object
    {
        private const val EXPENSES_LIST = 1
    }

}