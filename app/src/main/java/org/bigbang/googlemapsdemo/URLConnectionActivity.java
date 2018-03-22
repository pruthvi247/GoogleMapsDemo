package org.bigbang.googlemapsdemo;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

/**
 * Created by pruthvikumar on 18/3/18.
 */

public class URLConnectionActivity extends AsyncTask<String, Void, String> {

    private static final String TAG = "URLConnectionActivity";
//    private String url = "http://192.168.0.129:8080/parkCarLocationServices/webapi/myresource";
    public static String outPutResponse ="";
    private String url = "";
    public static final String REQUEST_METHOD = "GET";
    public static final int READ_TIMEOUT = 15000;
    public static final int CONNECTION_TIMEOUT = 15000;

    @Override
    protected String doInBackground(String... params) {
        String url = params[0];
        String outPutResponse = fetchGeoPoints(url);
        return outPutResponse;

    }

    @Override
    protected void onPostExecute(String outPutResponse) {

        super.onPostExecute(outPutResponse);
    }

    public String fetchGeoPoints(String requestUrl)  {
        String output="";
        try {
            URL urlString = createUrl(requestUrl);
            output = makeHttpRequest(urlString);
        }catch (IOException e){
            Log.e(TAG, "Problem in fetchGeoPoints : " + e.getMessage().toString());

        }
        return output;
    }
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

        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        String response = "";


        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            httpURLConnection.setReadTimeout(READ_TIMEOUT);
            httpURLConnection.connect();
            int responseCode = httpURLConnection.getResponseCode();

            Log.e(TAG, "Response Codd : "+httpURLConnection.getResponseCode());


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
    }


}
