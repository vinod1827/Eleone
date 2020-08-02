package com.morgat.eleone.webservice

import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import com.morgat.eleone.utils.AppConstants
import org.json.JSONObject


object WebServiceHelper {


    fun sendPostRequest(
            url: String,
            tag: String = "PostRequest",
            header: Map<String, String>? = null,
            jsonObject: JSONObject,
            webServiceListener: WebServiceListener
    ) {
        Log.e(AppConstants.TAG, "Post request $url")
        AndroidNetworking.post(url)
                .addHeaders(header)
                .addJSONObjectBody(jsonObject)
                .setTag(tag)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(object : StringRequestListener {
                    override fun onResponse(response: String) {
                        Log.e(AppConstants.TAG, "Response -> $response")
                        webServiceListener.onSuccess(response)
                    }

                    override fun onError(anError: ANError?) {
                        Log.e(AppConstants.TAG, "Error -> ${anError?.message}")
                        webServiceListener.onFailure(anError?.errorBody ?: "")
                    }

                })

    }

    fun sendGetRequest(
            url: String,
            tag: String = "GetRequest",
            header: Map<String, String>,
            jsonObject: JSONObject,
            webServiceListener: WebServiceListener
    ) {
        Log.e(AppConstants.TAG, "Get request $url")
        AndroidNetworking.get(url)
                .addHeaders(header)
                .addQueryParameter(jsonObject)
                .setTag(tag)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(object : StringRequestListener {
                    override fun onResponse(response: String) {
                        Log.e(AppConstants.TAG, "Response -> $response")
                        webServiceListener.onSuccess(response)
                    }

                    override fun onError(anError: ANError?) {
                        Log.e(AppConstants.TAG, "Error -> ${anError?.message}")
                        webServiceListener.onFailure(anError?.errorBody ?: "")
                    }

                })

    }

    interface WebServiceListener {
        fun onSuccess(response: String)
        fun onFailure(errorString: String)
    }
}