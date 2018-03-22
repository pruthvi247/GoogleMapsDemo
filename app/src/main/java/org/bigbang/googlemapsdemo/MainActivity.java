package org.bigbang.googlemapsdemo;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity.class";
    public static final int ERROR_DIALOG_REQUEST = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(isServiceOk()){

            inIt();
            inItGeoPoints();
            inItProfile();
        }
    }
private void inIt(){
    Button button = (Button) findViewById(R.id.mapButton);
    button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this,FirstMapActivity.class);
            startActivity(intent);


        }
    });

    }

    private void inItGeoPoints(){
        Button button = (Button) findViewById(R.id.geoPointButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,GeoPointActivity.class);
                startActivity(intent);


            }
        });}
    private void inItProfile(){
        Button button = (Button) findViewById(R.id.profleButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,ProfileActivity.class);
                startActivity(intent);


            }
        });}
// this method checks for google play service availability
    public boolean isServiceOk(){

        Log.d(TAG,"is Serviceok() : checking for googleservice version");
        int availability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (availability == ConnectionResult.SUCCESS){
            Log.d(TAG,"is Serviceok() :  googleservice is available");
            return true;
        }else if(GoogleApiAvailability.getInstance().isUserResolvableError(availability)){
            Log.d(TAG,"is Serviceok() :  googleservice has error  and is resolvable we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,availability,ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this,"Can not launch maps",Toast.LENGTH_SHORT);
        }

        return false;
    }



}
