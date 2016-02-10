package com.example.prefixa_01.odgtest1;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Humberto on 08/02/2016.
 */
public class getCircleTask extends AsyncTask<String,Void, String> {

    private String urlString;

    @Override
    protected String doInBackground(String... params) {

        BufferedReader reader = null;
        StringBuffer buffer = null;
        HttpURLConnection urlConnection = null;
        urlString = params[0];

        //HTTP Get
        try{
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            InputStream stream = urlConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));
            buffer = new StringBuffer();
            String line = "";
            while ((line = reader.readLine())!=null){
                buffer.append(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return buffer.toString();
    }

    @Override
    protected void onPostExecute(String result){

    }
}
