package com.expensify.expensifyappchallenge.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import java.util.*

class SelectionBuilder
{
    private var mTable: String? = null
    private val mProjectionMap : HashMap<String, String> = Maps.newHashMap()
    private val mSelection = StringBuilder()
    private val mSelectionArgs : ArrayList<String> = Lists.newArrayList()

    // Return selection string for current internal state.
    private fun getSelection(): String
    {
        return mSelection.toString()
    }

    //Return selection arguments for current internal state.
    private fun getSelectionArgs(): Array<String>
    {
        return mSelectionArgs.toArray(arrayOfNulls(mSelectionArgs.size))
    }

    /**
     * Append the given selection clause to the internal state. Each clause is
     * surrounded with parenthesis and combined using AND.
     */
    fun where(selection: String?, vararg selectionArgs: String?): SelectionBuilder
    {
        if (TextUtils.isEmpty(selection))
        {
            if (selectionArgs.isNotEmpty())
            {
                throw IllegalArgumentException("Valid selection required when including arguments=")
            }

            return this
        }

        if (mSelection.isNotEmpty())
        {
            mSelection.append(" AND ")
        }

        mSelection.append("(").append(selection).append(")")

        for (arg in selectionArgs)
        {
            mSelectionArgs.add(arg.toString())
        }

        return this
    }

    fun table(table: String): SelectionBuilder
    {
        mTable = table
        return this
    }

    private fun assertTable()
    {
        if (mTable == null)
        {
            throw IllegalStateException("Table not specified")
        }
    }

    private fun mapColumns(columns: Array<String>)
    {
        for (index in 0.. columns. size - 1)
        {
            val target = mProjectionMap.get(columns[index])
            if (target != null)
            {
                columns[index] = target
            }
        }
    }

    override fun toString(): String
    {
        return ("SelectionBuilder[table=" + mTable + ", selection=" + getSelection()
                + ", selectionArgs=" + Arrays.toString(getSelectionArgs()) + "]")
    }

    //Execute query using the current internal state as WHERE clause.
    fun query(db: SQLiteDatabase, columns: Array<String>?, orderBy: String?): Cursor
    {
        return query(db, columns, null, null, orderBy, null)
    }

    //Execute query using the current internal state as `WHERE` clause.
    private fun query(db: SQLiteDatabase, columns: Array<String>?, groupBy: String?, having: String?, orderBy: String?, limit: String?): Cursor
    {
        assertTable()

        if (columns != null)
        {
            mapColumns(columns)
        }

        return db.query(mTable, columns, getSelection(), getSelectionArgs(), groupBy, having, orderBy, limit)
    }

    //Execute update using the current internal state as `WHERE` clause.
    fun update(db: SQLiteDatabase, values: ContentValues?): Int
    {
        assertTable()

        return db.update(mTable, values, getSelection(), getSelectionArgs())
    }

    //Execute delete using the current internal state as `WHERE` clause.
    fun delete(db: SQLiteDatabase): Int
    {
        assertTable()

        return db.delete(mTable, getSelection(), getSelectionArgs())
    }

    object Maps
    {
        fun <K, V> newHashMap(): HashMap<K, V>
        {
            return HashMap()
        }
    }

    object Lists
    {
        fun <E> newArrayList(): ArrayList<E>
        {
            return ArrayList()
        }
    }
}