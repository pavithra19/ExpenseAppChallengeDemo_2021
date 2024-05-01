package com.expensify.expensifyappchallenge.model

import java.io.Serializable
import java.util.*

class Transaction: Serializable
{
    var transactionList: ArrayList<Expense>? = null
}

class Expense() : Serializable
{
    var transactionID: String? = null
    var created: String? = null
    var merchant: String? = null
    var amount: Long? = null

}