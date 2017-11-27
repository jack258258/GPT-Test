package com.example.user.gpt_test;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */

    //okhttp connection
    private OkHttpClient client = new OkHttpClient();
    private String url = "http://172.104.110.249/api/Login";
    private Request request;
    private static final MediaType CONTENT_TYPE = MediaType.parse("application/x-www-form-urlencoded");

    // UI references.
    private EditText mPhoneView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    //SharedPreferences
    private SharedPreferences settings;
    private static final String data_token = "DATA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mPhoneView = (EditText) findViewById(R.id.phone);
        mPasswordView = (EditText) findViewById(R.id.password);

        mProgressView = findViewById(R.id.login_progress);         //進度bar


        settings = getSharedPreferences(data_token,0);            //讀取Sharedpreference 物件內容getString(key, "unknow")，讀取被寫入的資料
        String token = settings.getString("token", "");

        Log.e("TOKEN", token);
        Button SignInButton = (Button) findViewById(R.id.sign_in_button);
        SignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mPhoneView.setError(null);              //改phone
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String phone = mPhoneView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(phone)) {     //須改為電話
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        }else if (TextUtils.isEmpty(password)){
            mPasswordView.setError(getString(R.string.error_empty_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            Log.d("OKHTTP", "Post function call");
            client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("user_account", mPhoneView.getText().toString())      //.add("鍵", "值")
                    .add("user_password", mPasswordView.getText().toString())
                    .build();

            request = new Request.Builder().url(url).post(formBody).build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.i("連線狀態", "Failure");

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // 連線成功
                    String result = response.body().string();
                    Log.d("OkHttp result", result);

                    String json = result;
                    try {
                        JSONObject parentObject = new JSONObject(json);
                        String message = parentObject.getString("message");
                        String token = parentObject.getString("token");
                        Boolean success = parentObject.getBoolean("success");
                        Log.d("JSON:", message);
                        Log.d("JSON:", token);
                        Log.d("JSON:",String.valueOf(success));
                        //And then read attributes like
                        if(success.equals(false)) {
                            runOnUiThread(new Runnable() {
                                @Override

                                public void run() {
                                    //收取資料成功，所以讓ProgressBar  進度條消失
                                    mProgressView.setVisibility(View.GONE);
                                    new AlertDialog.Builder(LoginActivity.this)
                                            .setTitle("訊息")
                                            .setMessage("登入失敗")
                                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.dismiss();
                                                    showProgress(false);
                                                }
                                            })
                                            .show();
                                }
                            });}else{
                            saveData(token);
                            Log.d("Token", "token 儲存成功");

                            Intent intent = new Intent();
                            intent.setClass(LoginActivity.this, MapsActivity.class);
                            startActivity(intent);
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /*private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }*/

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            //mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            /*mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });*/

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /*
        *           使用Sharedperference 儲存token
        */
    public void saveData(String token){
        settings = getSharedPreferences(data_token,0);
        settings.edit()
                .putString("token", token)        //寫入Sharedpreference 物件內容putString(key, value)  getText()讀取EDIT上的資料
                .commit();
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i("onResume","onResume");
    }
}
