package org.bigbang.googlemapsdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

/**
 * Created by pruthvikumar on 17/3/18.
 */

public class GeoPointActivity extends AppCompatActivity {

    private static final String TAG = "GeoPointActivity";

    private String url = "http://192.168.0.149:8080/parkCarLocationServices/webapi/myresource";
//        private String url = "https://earthquake.usgs.gov/fdsnws/event/1/query";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geopoint);
        TextView textView = (TextView)findViewById(R.id.textView);

        //String to place our result in
        String result = "";
        //Instantiate new instance of our class
        URLConnectionActivity getRequest = new URLConnectionActivity();
        //Perform the doInBackground method, passing in our url
        try {
            result = getRequest.execute(url).get();
        } catch (InterruptedException e) {
            Log.e(TAG, "exception  : " + e.getMessage().toString());
        } catch (ExecutionException e) {
            Log.e(TAG, "exception : " + e.getMessage().toString());
        }
        textView.setText(result);
    }

 /*   public String fetchGeoPoints(String requestUrl)  {
        String output="";
        try {
            URL urlString = createUrl(requestUrl);
             output = makeHttpRequest(urlString);
        }catch (IOException e){
            Log.e(TAG, "Problem in fetchGeoPoints : " + e.getMessage().toString());

        }
        return output;
    }
    *//**
     * Returns new URL object from the given string URL.
     *//*
    protected static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Problem building the URL : " + e.getMessage().toString());
        }
        return url;
    }

    protected String makeHttpRequest(URL url) throws IOException
    {

        URLConnectionActivity httpURLConnection = null;
        InputStream inputStream = null;
        String response = "";


        try {
            httpURLConnection = (URLConnectionActivity) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setReadTimeout(15000);
            httpURLConnection.connect();


            if (httpURLConnection.getResponseCode() == 200) {
                inputStream = httpURLConnection.getInputStream();
                response = readFromStream(inputStream);
            } else {
                Log.e(TAG, "error in http response");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return response;

    }
    protected static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }*/

}
