package com.loveall.clientuser;

import android.os.Build;

import androidx.annotation.RequiresApi;

public class API {
//    String domain = "https://nimbus-loveall.suryas.xyz/";
    String domain = "https://charityplus.tech/";
//    String SECRET_KEY = "HELLOWORLD", token="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2ODEwMTgzMTAsInVzZXJfZW1haWwiOiJhZG1pbkBhZG1pbi5jb20iLCJ1c2VyX2lkIjo0LCJ1c2VyX3JvbGUiOiJhZG1pbiJ9.LUlj-46ptMOcELlLzwhkuEKlzNEgDm-4CWKOxuU-dZI";
    String login = domain+"login";
    String merchantsForUser = domain+"api/v1/getMerchantsForUser/";
    String validateCard = domain+"api/v1/subscriptions/validate";
    String createTransaction = domain+"api/v1/transactions";
    String getCardInfo = domain+"api/v1/subscriptions/";


    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getDecodedJwt(String jwt) {
        String result = "";
        String[] parts = jwt.split("[.]");
        try {
            if (parts.length >= 2) {
                byte[] payloadAsBytes = parts[1].getBytes("UTF-8");
                String decodedPayload = new String(java.util.Base64.getUrlDecoder().decode(payloadAsBytes), "UTF-8");
                result = decodedPayload;
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not decode JWT payload", e);
        }

        return result;
    }


}
