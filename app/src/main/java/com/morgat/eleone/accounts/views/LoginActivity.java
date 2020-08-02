package com.morgat.eleone.accounts.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.morgat.eleone.accounts.viewmodels.LoginViewModel;
import com.morgat.eleone.application.ElevenApp;
import com.morgat.eleone.activities.MainMenuActivity;
import com.morgat.eleone.R;
import com.morgat.eleone.models.UserModel;
import com.morgat.eleone.webservice.ApiRequest;
import com.morgat.eleone.listeners.CallbackListener;
import com.morgat.eleone.utils.Variables;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.gmail.samehadar.iosdialog.IOSDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.morgat.eleone.activities.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class LoginActivity extends BaseActivity {


    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    IOSDialog iosDialog;

    SharedPreferences sharedPreferences;

    TextView login_title_txt;
    private LoginViewModel loginViewModel;
    private String fbId;
    private String loginType = "gmail";
    private String imageUrl;
    private String fname;
    private String lname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (Build.VERSION.SDK_INT == 26) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
        }

        loginViewModel = new ViewModelProvider.NewInstanceFactory().create(LoginViewModel.class);
        getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        this.getWindow()
                .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_login);


        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        // if the user is already login trought facebook then we will logout the user automatically
        LoginManager.getInstance().logOut();

        iosDialog = new IOSDialog.Builder(this)
                .setCancelable(false)
                .setSpinnerClockwise(false)
                .setMessageContentGravity(Gravity.END)
                .build();

        sharedPreferences = getSharedPreferences(Variables.pref_name, MODE_PRIVATE);

        findViewById(R.id.facebook_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginType = "facebook";
                loginwithFB();
            }
        });


        findViewById(R.id.google_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginType = "gmail";
                signInWithGmail();
            }
        });


        findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        login_title_txt = findViewById(R.id.login_title_txt);
        login_title_txt.setText("You need a " + getString(R.string.app_name) + "\naccount to Continue");


        SpannableString ss = new SpannableString("By signing up, you confirm that you agree to our \n Terms of Use and have read and understood \n our Privacy Policy.");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Open_Privacy_policy();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan, 99, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView textView = (TextView) findViewById(R.id.login_terms_condition_txt);
        textView.setText(ss);
        textView.setClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());


        printKeyHash();


        loginViewModel.isRegisteredLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s.equals("1")) {
                    Call_Api_For_Signup(fbId, fname, lname, imageUrl, loginType);
                } else if (s.equals("0")) {
                    navigateToRegistrationForm();
                }
            }
        });

    }


    private void navigateToRegistrationForm() {
        Intent intent = new Intent(this, RegistrationFormActivity.class);
        intent.putExtra("fbid", fbId);
        intent.putExtra("fname", fname);
        intent.putExtra("lname", lname);
        intent.putExtra("imageUrl", imageUrl);
        intent.putExtra("loginType", loginType);
        startActivity(intent);
        overridePendingTransition(0, R.anim.slide_from_left);
    }


    public void Open_Privacy_policy() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Variables.privacy_policy));
        startActivity(browserIntent);
    }


    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(200);

    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);

    }


    // Bottom two function are related to Fb implimentation
    private CallbackManager mCallbackManager;

    //facebook implimentation
    public void loginwithFB() {

        LoginManager.getInstance()
                .logInWithReadPermissions(LoginActivity.this,
                        Arrays.asList("public_profile", "email"));

        // initialze the facebook sdk and request to facebook for login
/*
        FacebookSdk.sdkInitialize(this.getApplicationContext());
*/
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // App code
                Toast.makeText(LoginActivity.this, "Login Cancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("resp", "" + error.toString());
                Toast.makeText(LoginActivity.this, "Login Error" + error.toString(), Toast.LENGTH_SHORT).show();
            }

        });


    }

    private void handleFacebookAccessToken(final AccessToken token) {
        // if user is login then this method will call and
        // facebook will return us a token which will user for get the info of user
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        Log.e("resp_token", token.getToken() + "");
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (Variables.IS_DEBUG)
                            System.out.println("#### " + task.isSuccessful());
                        if (task.isSuccessful()) {
                            iosDialog.show();
                            if (Variables.IS_DEBUG){
                                System.out.println("## Uid " + task.getResult().getUser().getUid());
                                System.out.println("## Provider Id " + task.getResult().getUser().getProviderId());
                            }
                            final String id = Profile.getCurrentProfile().getId();
                            GraphRequest request = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject user, GraphResponse graphResponse) {

                                    Log.d("resp", user.toString());
                                    //after get the info of user we will pass to function which will store the info in our server

                                    fname = "" + user.optString("first_name");
                                    lname = "" + user.optString("last_name");


                                    if (fname.equals("") || fname.equals("null"))
                                        fname = getResources().getString(R.string.app_name);

                                    if (lname.equals("") || lname.equals("null"))
                                        lname = "";

                                    fbId = id;

                                    loginType = "facebook";

                                    imageUrl = "https://graph.facebook.com/" + id + "/picture?width=500&width=500";

                                    loginViewModel.isRegistered(fbId);
                                }
                            });

                            // here is the request to facebook sdk for which type of info we have required
                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "last_name,first_name,email");
                            request.setParameters(parameters);
                            request.executeAsync();
                        } else {
                            // If sign in fails, display a message to the user.
                            if (Variables.IS_DEBUG)
                                System.out.println("############ " + task.getException());

                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result back to the Facebook SDK
        if (requestCode == 123) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else if (mCallbackManager != null)
            mCallbackManager.onActivityResult(requestCode, resultCode, data);

    }


    //google Implimentation
    GoogleSignInClient mGoogleSignInClient;

    public void signInWithGmail() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
        if (account != null) {
            fbId = account.getId();
            fname = "" + account.getGivenName();
            lname = "" + account.getFamilyName();

            if (account.getPhotoUrl() != null) {
                imageUrl = account.getPhotoUrl().toString();
            } else {
                imageUrl = "null";
            }

            if (fname.equals("") || fname.equals("null"))
                fname = getResources().getString(R.string.app_name);

            if (lname.equals("") || lname.equals("null"))
                lname = "User";

            loginType = "gmail";

            loginViewModel.isRegistered(fbId);
        } else {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, 123);
        }

    }


    //Relate to google login
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String id = account.getId();
                String fname = "" + account.getGivenName();
                String lname = "" + account.getFamilyName();

                // if we do not get the picture of user then we will use default profile picture

                String pic_url;
                if (account.getPhotoUrl() != null) {
                    pic_url = account.getPhotoUrl().toString();
                } else {
                    pic_url = "null";
                }


                if (fname.equals("") || fname.equals("null"))
                    fname = getResources().getString(R.string.app_name);

                if (lname.equals("") || lname.equals("null"))
                    lname = "";

                Call_Api_For_Signup(id, fname, lname, pic_url, "gmail");


            }
        } catch (ApiException e) {
            Log.w("Error message", "signInResult:failed code=" + e.getMessage());
        }

    }


    // this function call an Api for Signin
    private void Call_Api_For_Signup(String id,
                                     String f_name,
                                     String l_name,
                                     String picture,
                                     String singnup_type) {


        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String appversion = null;
        if (packageInfo != null) {
            appversion = packageInfo.versionName;
        }

        JSONObject parameters = new JSONObject();
        try {

            parameters.put("fb_id", id);
            parameters.put("first_name", "" + f_name);
            parameters.put("last_name", "" + l_name);
            parameters.put("profile_pic", picture);
            parameters.put("gender", "m");
            parameters.put("version", appversion);
            parameters.put("signup_type", singnup_type);
            parameters.put("device", Variables.device);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        iosDialog.show();
        ApiRequest.callApi(this, Variables.SignUp, parameters, new CallbackListener() {
            @Override
            public void onResponse(String resp) {
                iosDialog.cancel();
                parseSignupData(resp);

            }
        });

    }


    // if the signup successfull then this method will call and it store the user info in local
    public void parseSignupData(String loginData) {
        try {
            JSONObject jsonObject = new JSONObject(loginData);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray jsonArray = jsonObject.getJSONArray("msg");
                JSONObject userdata = jsonArray.getJSONObject(0);
                UserModel userModel = new Gson().fromJson(userdata.toString(), UserModel.class);
                ElevenApp.Companion.getPreffs().setUserModel(userModel);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.commit();
                finish();

                Intent intent = new Intent(this, MainMenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            } else {
                Toast.makeText(this, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    // this function will print the keyhash of your project
    // which is very helpfull during Fb login implimentation
    public void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.i("keyhash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


}
