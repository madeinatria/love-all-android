package com.loveall.clientuser;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class dashboard extends AppCompatActivity {

    TextInputLayout billAmountLayout;
    TextInputEditText billAmount;
    int MERCHANT_ID;
    TextView clientName;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    API api = new API();
    String TOKEN;
    int userId = 0;

    ArrayList<Integer> merchants = new ArrayList<Integer>();




    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        clientName = findViewById(R.id.clientName);
        billAmountLayout = findViewById(R.id.billAmountLayout);
        billAmount = findViewById(R.id.billAmount);

        preferences = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        editor = preferences.edit();
        handleInternetConnectivity();
        Button logoutButton = findViewById(R.id.logout);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("MyPreferences", MODE_PRIVATE).edit();
                editor.remove("loginInfo");
                editor.apply();
                Toasty.info(getApplicationContext(), "Logout success", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();


            }
        });

        TOKEN = preferences.getString("loginInfo", "");

        try {
            JSONObject jsonObject = new JSONObject(api.getDecodedJwt(TOKEN));

            userId = jsonObject.getInt("user_id");

        } catch (JSONException e) {
            e.printStackTrace();
        }


        // Show loading indicator
        ProgressDialog progressDialog = ProgressDialog.show(this, "", "Loading...", true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(api.merchantsForUser+userId);
                    Log.e("APIENDPOINT",api.merchantsForUser+userId);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Authorization", "Bearer "+TOKEN);

                    if (conn.getResponseCode() != 200) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                try {
                                    Toasty.error(getApplicationContext(),"Error Code : "+conn.getResponseCode(),Toasty.LENGTH_LONG).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                finish();
                            }
                        });
                        throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                    StringBuilder sb = new StringBuilder();
                    String output;
                    while ((output = br.readLine()) != null) {
                        sb.append(output);
                    }

                    String jsonResponse = sb.toString();
                    Log.e("------------ERR",jsonResponse);

                    JSONObject response = new JSONObject(jsonResponse);
                    JSONArray data = response.getJSONArray("data");
                    JSONObject userData = data.getJSONObject(0).getJSONObject("User");
                    String userName = userData.getString("first_name");
                    String lastName = userData.getString("last_name");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Hide loading indicator
                            progressDialog.dismiss();

                            TextView username = findViewById(R.id.welcomeUser);
                            username.setText(userName+" "+lastName);
                        }
                    });

                    try {
                        JSONArray dataArray = new JSONObject(jsonResponse).getJSONArray("data");
                        if(dataArray.length() ==0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                    Toasty.error(getApplicationContext(),"Not a merchant account",Toasty.LENGTH_LONG).show();
                                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                    finish();
                                }
                            });
                        }
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject dataObject = dataArray.getJSONObject(i);
                            int id = dataObject.getInt("id");
                            MERCHANT_ID = id;
                            String merchantName = dataObject.getString("merchant_name");
                            String location = dataObject.getString("location");
                            Log.e("MERCHANT_NAME", String.valueOf(merchantName));
                            merchants.add(MERCHANT_ID);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    clientName.setText(merchantName+", "+location);
                                }
                            });

                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }

                    conn.disconnect();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }).start();


        findViewById(R.id.send_otp_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toasty.info(getApplicationContext(),"This feature will be available soon...",Toasty.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.scan_qr_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String bill = billAmount.getText().toString();
                if(!bill.isEmpty())
                {
                    Intent intent = new Intent(getApplicationContext(),QR_Scanner.class);
                    intent.putExtra("billAmount",bill);
                    intent.putExtra("merchant_id",String.valueOf(MERCHANT_ID));
                    startActivity(intent);

                }
                else
                {
                    billAmountLayout.setError("Enter Bill Amount");
                }


            }
        });
    }



    void handleInternetConnectivity()
    {
        final int seconds = 10;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // Internet is available
            // Your code here
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Internet Connection")
                    .setMessage("Please check your internet connection and try again.")
                    .setCancelable(false);

            final AlertDialog alertDialog = builder.create();
            alertDialog.show();

            final TextView textView = alertDialog.findViewById(android.R.id.message);
            final CountDownTimer countDownTimer = new CountDownTimer(seconds * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    textView.setText("Please check your internet connection and try again in " + millisUntilFinished / 1000 + " seconds.");
                }

                @Override
                public void onFinish() {
                    alertDialog.dismiss();
                    finish();
                }
            };

            countDownTimer.start();

            BroadcastReceiver networkReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        countDownTimer.cancel();
                        alertDialog.dismiss();
                    }
                }
            };

            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

            registerReceiver(networkReceiver, filter);
        }

    }




}