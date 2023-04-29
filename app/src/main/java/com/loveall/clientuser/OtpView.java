package com.loveall.clientuser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.google.android.material.textfield.TextInputEditText;

public class OtpView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_view);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        TextInputEditText otpEditText = findViewById(R.id.otp_edit_text);

        findViewById(R.id.verify_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otp = otpEditText.getText().toString();
                startActivity(new Intent(getApplicationContext(),UserInfo.class));
                finish();
            }
        });
    }
}