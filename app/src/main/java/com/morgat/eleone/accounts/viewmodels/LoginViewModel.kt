package com.morgat.eleone.accounts.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.morgat.eleone.accounts.repositories.LoginRepository
import com.morgat.eleone.utils.Variables
import com.morgat.eleone.utils.WebParameterConstants
import org.json.JSONObject

class LoginViewModel : ViewModel() {

    private var repository: LoginRepository = LoginRepository()
    var isRegisteredLiveData: LiveData<String>
    var registrationLiveResponse: LiveData<String>

    init {
        isRegisteredLiveData = repository.checkIfRegisteredResponse
        registrationLiveResponse = repository.registrationResponse
    }

    fun isRegistered(fbId: String) {
        val jsonObject = JSONObject()
        jsonObject.put(WebParameterConstants.FID, fbId)
        repository.checkIfRegistered(jsonObject)
    }

    fun signup(fbId: String, firstName: String, lastName: String, imageUrl: String, gender: String, loginType: String, version: String, age: String) {
        val jsonObject = JSONObject()
        jsonObject.put(WebParameterConstants.F_ID, fbId)
        jsonObject.put(WebParameterConstants.FIRST_NAME, firstName)
        jsonObject.put(WebParameterConstants.LAST_NAME, lastName)
        jsonObject.put(WebParameterConstants.PROFILE_PIC, imageUrl)
        jsonObject.put(WebParameterConstants.GENDER, gender)
        jsonObject.put(WebParameterConstants.VERSION, version)
        jsonObject.put(WebParameterConstants.SIGN_UP_TYPE, loginType)
        jsonObject.put(WebParameterConstants.DEVICE, Variables.device)
        jsonObject.put(WebParameterConstants.AGE, age)
        repository.signup(jsonObject)
    }
}