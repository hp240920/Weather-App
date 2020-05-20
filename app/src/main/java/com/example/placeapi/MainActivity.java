package com.example.placeapi;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    EditText place_text;
    TextView display;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        place_text = findViewById(R.id.place_text);
        display = findViewById(R.id.display);

        Places.initialize(getApplicationContext(), "AIzaSyCeiT6TyJQoBPHtgcU_ymy1-_JIumuQHOU");

        place_text.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ID, Place.Field.NAME);

                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,
                        fieldList).build(MainActivity.this);

                startActivityForResult(intent, 100);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100 && resultCode == RESULT_OK){
            Place place = Autocomplete.getPlaceFromIntent(data);

            place_text.setText(place.getName());

            GetWeather weather = new GetWeather();
            try {

                String placename = place.getName();
                String toExecute = "http://api.openweathermap.org/data/2.5/weather?q="+placename+"&appid=516fd01c95c4deb1256b50104ca31a73&lang=en-US";
               String result =  weather.execute(toExecute).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }else if(resultCode == AutocompleteActivity.RESULT_ERROR){
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getApplicationContext(), status.getStatusMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public class GetWeather extends AsyncTask<String, Void, String >{

        URL url;
        String result = "";
        HttpURLConnection httpURLConnection = null;
        @Override
        protected String doInBackground(String... urls) {

            try {
                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection)url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                int data  = inputStreamReader.read();

                while(data != -1){
                    char current = (char)data;
                    result += current;
                    data = inputStreamReader.read();
                }
                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {

                JSONObject jsonObject = new JSONObject(result);

                String weatherInfo = jsonObject.getString("weather");
                String main = jsonObject.getString("main");

                JSONArray arr = new JSONArray(weatherInfo);
                JSONObject arr2 = new JSONObject(main);

                String toDisplay = "";

                for (int i = 0; i < arr.length(); i++) {

                    JSONObject jsonPart = arr.getJSONObject(i);

                    String mainStr = jsonPart.getString("main");
                    String weather_des = jsonPart.getString("description");

                    toDisplay += " Weather :"+ mainStr + " \n Weather description : " + weather_des;

                }


                    String weather_temp =arr2.getString("temp");
                    String weather_feels_like = arr2.getString("feels_like");
                    String weather_min = arr2.getString("temp_min");
                    String weather_max =arr2.getString("temp_max");
                    String weather_pressure =arr2.getString("pressure");
                    String weather_humidity =arr2.getString("humidity");

                    toDisplay += "\n Temperature " + weather_temp + "\n Feels like :" + weather_feels_like + "\n Min "+ weather_min + " Max "+ weather_max
                            + "\n Pressure " + weather_pressure + "\n Humidity :" + weather_humidity;


                display.setText(toDisplay);
            } catch (JSONException e) {
                e.printStackTrace();
            }



        }

    }
}
