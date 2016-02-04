package com.example.prefixa_01.odgtest1;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by diego on 2/3/2016.
 */
public class JSONTask extends AsyncTask<String,String,String> {

    public String userID;
    public String token;

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = reader.readLine())!=null){
                buffer.append(line);
            }
            String jsonString = buffer.toString();
            JSONObject myJSONO = new JSONObject(jsonString);
            userID = myJSONO.getString("identity");
            token = myJSONO.getString("token");
            return buffer.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (JSONException je){
            je.printStackTrace();
        }finally {
            if (connection!= null){
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        MainActivity.UIdentity.setUName(userID);
        MainActivity.UIdentity.setToken(token);
    }
}
