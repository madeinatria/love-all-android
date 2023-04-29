package com.loveall.clientuser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserInfo extends AppCompatActivity {
    TextView transStatus, card_name;
    TextView tid, username, cardno, datentime, savedAmount;
    int CARDID = 0;
    API api = new API();
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    String TOKEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        transStatus = findViewById(R.id.transactionStatus);
        tid = findViewById(R.id.transactionID);
        username = findViewById(R.id.user_name);
        cardno = findViewById(R.id.cardNumber);
        datentime = findViewById(R.id.datentime);
        savedAmount = findViewById(R.id.savedAmount);
        card_name = findViewById(R.id.card_name);
        preferences = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        editor = preferences.edit();

        TOKEN = preferences.getString("loginInfo", "");


        int billAmount = getIntent().getIntExtra("totalBill", 0);
        int discountAmount = getIntent().getIntExtra("discount", 0);
        CARDID = getIntent().getIntExtra("cardid", 0);

        savedAmount.setText("INR "+(billAmount*discountAmount/100)+" saved /-");
        tid.setText("Amount Payable : "+
                (
                    billAmount - (billAmount*discountAmount/100)
                )
        );

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    URL url = new URL(api.getCardInfo+CARDID);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Authorization", "Bearer "+TOKEN);
                    Log.e("TKN",TOKEN + " "+CARDID);

                    if (conn.getResponseCode() != 200) {
                        throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                    }
                    BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                    StringBuilder sb = new StringBuilder();
                    String output;
                    while ((output = br.readLine()) != null) {
                        sb.append(output);
                    }

                    String jsonResponse = sb.toString();

                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONObject userObject = jsonObject.getJSONObject("User");
                        String firstName = userObject.getString("first_name");
                        String lastName = userObject.getString("last_name");
                        String cardNumber = jsonObject.getString("number");
                        String cardName = jsonObject.getString("card_name");
                        String role = userObject.getString("role");
                        String fullName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase() + " " +
                                lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase();

                        Log.d("USER_INFO", "Name: " + firstName + " " + lastName + ", Card Number: " + cardNumber + ", Card Name: " + cardName + ", Role: " + role);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                username.setText(fullName);
                                cardno.setText(cardNumber);
                                card_name.setText(cardName + " plan");

                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();




    }

}