package com.morgat.eleone.accounts.repositories

import androidx.lifecycle.MutableLiveData
import com.morgat.eleone.utils.WebApi
import com.morgat.eleone.webservice.WebServiceHelper
import org.json.JSONObject

class LoginRepository {

    val checkIfRegisteredResponse = MutableLiveData<String>()
    val registrationResponse = MutableLiveData<String>()

    fun checkIfRegistered(jsonObject: JSONObject) {
        WebServiceHelper.sendPostRequest(WebApi.CHECK_IF_REGISTERED, "", jsonObject = jsonObject,
                webServiceListener = object : WebServiceHelper.WebServiceListener {
                    override fun onSuccess(response: String) {
                        val jsonObject = JSONObject(response)
                        println("## $jsonObject")
                        if (!jsonObject.isNull("msg")) {
                            val msg = jsonObject.getInt("msg")
                            checkIfRegisteredResponse.postValue(msg.toString())
                        }
                    }

                    override fun onFailure(errorString: String) {
                        checkIfRegisteredResponse.postValue(null)
                    }

                })
    }


    fun signup(jsonObject: JSONObject){
        WebServiceHelper.sendPostRequest(WebApi.SIGNUP, "", jsonObject = jsonObject,
                webServiceListener = object : WebServiceHelper.WebServiceListener {
                    override fun onSuccess(response: String) {
                        registrationResponse.postValue(response)
                    }

                    override fun onFailure(errorString: String) {
                        registrationResponse.postValue(null)
                    }

                })
    }
}