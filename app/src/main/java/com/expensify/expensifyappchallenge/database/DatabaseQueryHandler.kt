package com.expensify.expensifyappchallenge.database

import android.content.AsyncQueryHandler
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import java.lang.ref.WeakReference

class DatabaseQueryHandler(resolver: ContentResolver, listener: AsyncQueryListener) : AsyncQueryHandler(resolver)
{
    private var mListener: WeakReference<AsyncQueryListener>? = null

    /**
     * Interface to listen for completed query operations.
     */
    interface AsyncQueryListener
    {
        fun onQueryComplete(token: Int, cookie: Any?, cursor: Cursor?)
    }

    init
    {
        setQueryListener(listener)
    }

    /**
     * Assign the given AsyncQueryListener to receive query events from
     * asynchronous calls. Will replace any existing listener.
     */
    private fun setQueryListener(listener: AsyncQueryListener)
    {
        mListener = WeakReference(listener)
    }

    /**
     * Begin an asynchronous query with the given arguments. When finished,
     * AsyncQueryListener#onQueryComplete(int, Object, Cursor) is
     * called if a valid AsyncQueryListener is present.
     */
    fun startQuery(uri: Uri, projection: Array<String>?)
    {
        startQuery(-1, null, uri, projection, null, null, null)
    }

    override fun onQueryComplete(token: Int, cookie: Any?, cursor: Cursor?)
    {
        val listener = if (mListener == null) null else mListener!!.get()

        listener?.onQueryComplete(token, cookie, cursor) ?: cursor?.close()

    }
}