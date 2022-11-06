package com.bignerdranch.android.photogallery

import android.content.Context
import androidx.preference.PreferenceManager
import retrofit2.http.Query

private const val PREF_SEARCH_QUERY = "searchQuery"
object QueryPreferences {
    fun getStoredQuery (context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(PREF_SEARCH_QUERY, "")!!
    }

    fun setStoredQuery (context:Context, query: String){
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(PREF_SEARCH_QUERY,query)
            .apply()
    }
}