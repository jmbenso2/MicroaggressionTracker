package com.example.jonolaptop.mapapplication;

import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ReadActivity extends AppCompatActivity {

    private Integer tuid;
    private String description;
    private Double lat;
    private Double lng;
    private Integer hp;
    private TextView text;
    private TextView countText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        //Receive intent containing position
        Intent intent = getIntent();
        tuid = intent.getIntExtra("tuid", 0);
        description = intent.getStringExtra("description");
        lat = intent.getDoubleExtra("lat", 0.00);
        lng = intent.getDoubleExtra("lng", 0.00);
        hp = intent.getIntExtra("hp", 5);

        // Get ref to text view
        text = (TextView)findViewById(R.id.textView);
        countText = (TextView)findViewById(R.id.textViewHugCount);
        // Display text
        text.setText(description);
        countText.setText("Hugs needed: " + hp);
    }

    public void onBack(View view) {
        finish();
    }

    public void onHug(View view) {
        //Change thread policy to make things simple
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            //Prepare to connect
            String urlString = "http://csis.svsu.edu/~jmbenso2/cs403/hug.php";
            String inData = "tuid="+tuid.toString();

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

    public void onDelete(View view) {
        //Change thread policy to make things simple
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            //Prepare to connect
            String urlString = "http://csis.svsu.edu/~jmbenso2/cs403/delete.php";
            String inData = "tuid="+ URLEncoder.encode(tuid.toString(),"UTF-8");

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
