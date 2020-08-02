package com.morgat.eleone.accounts.views

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.morgat.eleone.activities.MainMenuActivity
import com.morgat.eleone.R
import com.morgat.eleone.utils.Variables
import com.morgat.eleone.accounts.viewmodels.LoginViewModel
import com.morgat.eleone.application.ElevenApp.Companion.preffs
import com.morgat.eleone.activities.BaseActivity
import com.morgat.eleone.components.LoadingProgressBar
import com.morgat.eleone.dialog.CustomSpinnerDialog
import com.morgat.eleone.models.UserModel
import kotlinx.android.synthetic.main.activity_registration_form.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class RegistrationFormActivity : BaseActivity() {

    private lateinit var loadingProgressBar: LoadingProgressBar
    private lateinit var loginViewModel: LoginViewModel
    private var selectedGender: String? = null
    private var loginType: String? = null
    private var imageUrl: String? = null
    private var lName: String? = null
    private var fName: String? = null
    private var fbid: String? = null
    var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_form)

        sharedPreferences = getSharedPreferences(Variables.pref_name, Context.MODE_PRIVATE)

        loadingProgressBar = LoadingProgressBar(this)
        window.statusBarColor = ContextCompat.getColor(this, R.color.signup_start_gradient_color)

        fbid = intent.extras?.getString("fbid")
        fName = intent.extras?.getString("fname")
        lName = intent.extras?.getString("lname")
        imageUrl = intent.extras?.getString("imageUrl")
        loginType = intent.extras?.getString("loginType")

        firstNameEditText.setText(fName)
        lastNameEditText.setText(lName)



        genderGroup.setOnCheckedChangeListener { radioGroup, i ->
            if (maleRadioButton.isChecked) selectedGender = "Male"
            if (femaleRadioButton.isChecked) selectedGender = "Female"
            if (othersRadioButton.isChecked) selectedGender = "Others"
        }

        loginViewModel = ViewModelProvider.NewInstanceFactory().create(LoginViewModel::class.java)

        loginViewModel.registrationLiveResponse.observe(this, Observer {
            loadingProgressBar.hideProgress()
            if (it != null) {
                parseData(it)
            } else Toast.makeText(this@RegistrationFormActivity, "Error while registering.", Toast.LENGTH_LONG).show()
        })
    }

    fun onBackPressButtonClicked(view: View) {
        finish()
    }

    fun onSignupClicked(view: View) {
        if (areCredentialsValid()) {
            loadingProgressBar.showProgress()
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            loginViewModel.signup(fbid!!, firstName = firstNameEditText.text.toString(),
                    lastName = lastNameEditText.text.toString(),
                    imageUrl = imageUrl!!,
                    loginType = loginType!!,
                    gender = selectedGender!!,
                    age = ageEditText.text.toString(),
                    version = packageInfo.versionName
            )
        }
    }

    private fun areCredentialsValid(): Boolean {
        if (firstNameEditText.text.toString().isEmpty()) {
            firstNameEditText.error = "Please Enter First Name"
            return false
        }

        if (lastNameEditText.text.toString().isEmpty()) {
            lastNameEditText.error = "Please Enter Last Name"
            return false
        }


        if (ageEditText.text.toString().isEmpty()) {
            ageEditText.error = "Please select age"
            return false
        }

        if (ageEditText.text.toString() == getString(R.string.select_age)) {
            ageEditText.error = "Please select age"
            return false
        }


        if (selectedGender == null) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    private fun parseData(loginData: String) {
        try {
            val jsonObject = JSONObject(loginData)
            val code: String = jsonObject.optString("code")
            if (code == "200") {
                val jsonArray: JSONArray = jsonObject.getJSONArray("msg")
                val userJsonObject = jsonArray.getJSONObject(0)
                val userModel = Gson().fromJson(userJsonObject.toString(),UserModel::class.java)
                preffs?.userModel = userModel
                Toast.makeText(this@RegistrationFormActivity, "Registered Successfully", Toast.LENGTH_LONG).show()
                navigateToMainMenuActivity()
            } else {
                Toast.makeText(this, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun navigateToMainMenuActivity() {
        val intent = Intent(this, MainMenuActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    fun onAgeClicked(view: View) {
        val ageList = ArrayList<String>()
        for (x in 18..100) {
            ageList.add("$x")
        }
        CustomSpinnerDialog(
                this,
                ageList,
                object : CustomSpinnerDialog.CustomSpinnerDialogListener {
                    override fun onItemSelected(item: String) {
                        ageEditText.setText(item)
                    }

                }).show()
    }
}