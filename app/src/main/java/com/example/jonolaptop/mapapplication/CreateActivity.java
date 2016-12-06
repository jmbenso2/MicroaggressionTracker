package com.example.jonolaptop.mapapplication;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CreateActivity extends AppCompatActivity {

    private Double lat;
    private Double lng;
    private EditText inText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        //Receive intent containing position
        Intent intent = getIntent();
        lat = intent.getDoubleExtra("lat", 0.00);
        lng = intent.getDoubleExtra("lng", 0.00);

        // Get ref to text view
        inText = (EditText)findViewById(R.id.editText);

    }

    public void onCancel(View view) {
        finish();
    }

    public void onSubmit(View view) {

        //Change thread policy to make things simple
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //Grab input string
        String in = inText.getText().toString();

        try {
            //Prepare to connect
            String urlString = "http://csis.svsu.edu/~jmbenso2/cs403/create.php";
            String inData = "description="+URLEncoder.encode(in,"UTF-8")+"&lat="+lat.toString()+"&lng="+lng.toString();

            //Get url and connect
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setConnectTimeout(15000);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");

            //Write
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(inData);
            writer.flush();
            writer.close();

            int responseCode = connection.getResponseCode();

            finish();
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "DEBUG:" + e.toString(), Toast.LENGTH_SHORT).show();
            finish();
        }


    }

}
