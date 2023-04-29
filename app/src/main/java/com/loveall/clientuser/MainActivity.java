package com.loveall.clientuser;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import es.dmoral.toasty.Toasty;


public class MainActivity extends AppCompatActivity {
    private TextInputLayout emailTextInputLayout, passwordTextInputLayout;
    private TextInputEditText emailEditText, passwordEditText;
    private Button loginUserButton;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ProgressDialog progressDialog;

    API api = new API();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        emailTextInputLayout = findViewById(R.id.emailTextInputLayout);
        passwordTextInputLayout = findViewById(R.id.passwordTextInputLayout);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginUserButton = findViewById(R.id.loginUserButton);

        preferences = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        editor = preferences.edit();



        String token = preferences.getString("loginInfo", "");

        if (!token.isEmpty()) {
            validateUser(token);

        }

        loginUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Validate the email and password
                if (email.isEmpty()) {
                    emailTextInputLayout.setError("Email is required");
                    emailEditText.requestFocus();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailTextInputLayout.setError("Please enter a valid email");
                    emailEditText.requestFocus();
                    return;
                }

                if (password.isEmpty()) {
                    passwordTextInputLayout.setError("Password is required");
                    passwordEditText.requestFocus();
                    return;
                }

                loginUser(email, password);


            }
        });

        findViewById(R.id.contactus).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String phoneNumber = "+918595984485";
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(intent);

            }
        }
        );
    }

    public void loginUser(String email, String password)
    {


        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        String url = api.login;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);
        progressDialog.show();



        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            String token = response.getString("token");
                            editor.putString("loginInfo", token);
                            boolean success = editor.commit();
                            Log.i("TOKEN",token);
                            if (success) {

                                validateUser(token);
                                Toasty.success(getApplicationContext(), "Login success",Toasty.LENGTH_SHORT).show();


                            }

                        } catch (JSONException e) {

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        if (error.networkResponse != null) {
                            switch (error.networkResponse.statusCode) {
                                case 400:
                                    Toasty.error(getApplicationContext(), "Invalid request", Toasty.LENGTH_SHORT).show();
                                    break;
                                case 401:
                                    Toasty.error(getApplicationContext(), "Invalid credentials", Toasty.LENGTH_SHORT).show();
                                    break;
                                case 500:
                                    Toasty.error(getApplicationContext(), "Internal server error", Toasty.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toasty.error(getApplicationContext(), "Error : "+String.valueOf(error.networkResponse.statusCode), Toasty.LENGTH_SHORT).show();
                                    break;
                            }
                        } else {
                            Toasty.error(getApplicationContext(), "Network error", Toasty.LENGTH_SHORT).show();
                        }
                    }
                });

        queue.add(request);


    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    void validateUser(String token)
    {
        try{
            JSONObject jsonObject = new JSONObject(api.getDecodedJwt(token));
            int userId = jsonObject.getInt("user_id");


            Intent intent = new Intent(getApplicationContext(), dashboard.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
            finish();

        }
        catch (Exception e)
        {
            Toasty.info(getApplicationContext(), String.valueOf(e), Toast.LENGTH_LONG).show();
            Log.e("ERRRR", String.valueOf(e));

        }
    }
}