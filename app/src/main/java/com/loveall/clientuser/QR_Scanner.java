package com.loveall.clientuser;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class QR_Scanner extends AppCompatActivity {
    private CodeScanner mCodeScanner;
    TextView tvresult;
    public String USERID;
    String billAmount;
    API api = new API();
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    String TOKEN;
    int MERCHANT_ID;
    int discountRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        preferences = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        editor = preferences.edit();

        TOKEN = preferences.getString("loginInfo", "");
        String merchID = getIntent().getStringExtra("merchant_id");
        billAmount = getIntent().getStringExtra("billAmount");

        MERCHANT_ID = Integer.parseInt(merchID);
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(getApplicationContext(), scannerView);
        mCodeScanner.startPreview();
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       // Toast.makeText(getApplicationContext(), result.getText(), Toast.LENGTH_SHORT).show();
                        if(result != null) {
                            if(result.getText() == null) {
                                Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(getApplicationContext(),dashboard.class));
                                finish();
                            } else {
                                USERID = result.getText().toString();
                                try {
                                    int user_id = Integer.parseInt(USERID);
                                    validateCard(user_id);
                                }
                                catch (Exception e)
                                {
                                    Toasty.error(getApplicationContext(),"Invalid Card",Toasty.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(),dashboard.class));
                                    finish();

                                }

                            }


                        }
                    }
                });
            }
        });


    }

    void validateCard(int cardid)
    {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        String url = api.validateCard;

        JSONObject jsonBody = new JSONObject();
        try
        {
            jsonBody.put("merchant_id", MERCHANT_ID);
            jsonBody.put("card_id", cardid);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONObject offerObj = response.getJSONObject("offer");
                            discountRate = offerObj.getInt("discount_rate");
                            createTransaction(MERCHANT_ID,cardid, Integer.parseInt(billAmount));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ERROR",error.toString());
                        Toasty.error(getApplicationContext(),"Invalid Card",Toasty.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(),dashboard.class));
                        finish();
                    }
                })
        {
            // Pass the token in the header
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + TOKEN);
                return headers;
            }
        };

        queue.add(request);

    }


    void createTransaction(int merchant_offer_id, int card_offer_id, int amount)
    {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        String url = api.createTransaction;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("merchant_offer_id", merchant_offer_id);
            jsonBody.put("card_subscription_id", card_offer_id);
            jsonBody.put("amount", amount);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONObject response) {
                        Toasty.success(getApplicationContext(),"Offer Applied",Toasty.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(),UserInfo.class);
                        intent.putExtra("totalBill",amount);
                        intent.putExtra("discount",discountRate);
                        intent.putExtra("cardid",card_offer_id);
                        startActivity(intent);
                        finish();


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toasty.error(getApplicationContext(),"Couldn't Apply the offer.",Toasty.LENGTH_SHORT).show();
                    }
                })
        {
            // Pass the token in the header
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + TOKEN);
                return headers;
            }
        };

        queue.add(request);

    }



    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }
}