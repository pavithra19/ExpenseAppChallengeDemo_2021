package com.expensify.expensifyappchallenge.viewfilter

import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import java.util.regex.Pattern

class EditTextInputFilter(digitsBeforeDecimal: Int?, digitsAfterDecimal: Int?) : InputFilter
{
    private val mPattern: Pattern = Pattern.compile("-?[0-9]{0," + digitsBeforeDecimal + "}+((\\.[0-9]{0," +
            digitsAfterDecimal + "})?)||(\\.)?")

    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence?
    {
        val replacement = source.subSequence(start, end).toString()
        val newValue = (dest.subSequence(0, dstart).toString() + replacement + dest.subSequence(dend, dest.length).toString())

        val matcher = mPattern.matcher(newValue)

        if (matcher.matches())
        {
            return null
        }

        if (TextUtils.isEmpty(source))
        {
            return dest.subSequence(dstart, dend)
        }
        else
        {
            return ""
        }
    }

}