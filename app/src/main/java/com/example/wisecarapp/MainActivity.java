package com.example.wisecarapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.wisecarapp.VehicleActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //startActivity(new Intent(MainActivity.this, LoginActivity.class));

        uploadVehicleInfoByHttpClient();
    }


    private static void uploadVehicleInfoByHttpClient() {


        HttpClient httpClient = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost("http://13.211.112.83:3000/api/v1/servicerecords/testblockchaininvoke");

        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);


        try {
            reqEntity.addPart("identifier", new StringBody("12121221n"));
            reqEntity.addPart("record_type", new StringBody("uirefo"));
            reqEntity.addPart("service_center", new StringBody("gheiljesl"));
            reqEntity.addPart("service_date", new StringBody("2020-07-03"));
            reqEntity.addPart("vehicle_registration", new StringBody("weiuo"));
            reqEntity.addPart("ecrypt_hash", new StringBody("state"));
            reqEntity.addPart("service_file_location", new StringBody("year"));
            reqEntity.addPart("service_options", new StringBody("ufd"));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        postRequest.setEntity(reqEntity);
        HttpResponse response = null;
        StringBuilder s = new StringBuilder();
        try {
            response = httpClient.execute(postRequest);
            Log.e("response", String.valueOf(response));

        } catch (IOException e) {
            e.printStackTrace();
        }

        postRequest.abort();
        httpClient.getConnectionManager().shutdown();

    }

}
