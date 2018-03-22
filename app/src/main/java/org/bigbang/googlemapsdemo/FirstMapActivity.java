package org.bigbang.googlemapsdemo;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.bigbang.model.PlaceInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by pruthvikumar on 13/3/18.
 */

public class FirstMapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener {


    private static final String TAG = "MapActivity";

    private String url = "http://192.168.0.129:8080/parkCarLocationServices/webapi/myresource";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final Float DEFAULT_ZOOM = 15F;
    // Widgets section
    private AutoCompleteTextView mSearchText;
    private ImageView mGps;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));
    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private PlaceInfo mPlace;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstmap);
        mSearchText = (AutoCompleteTextView) findViewById(R.id.inputSearchText);
        mGps = (ImageView) findViewById(R.id.ic_gps);



        getLocationPermission();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void inIt() {
        // initializing
        Log.d(TAG, "inIt : initializing");
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mSearchText.setOnItemClickListener(mAutocompleteClickListener);


        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient,
                LAT_LNG_BOUNDS, null);
        mSearchText.setAdapter(mPlaceAutocompleteAdapter);

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER) {

                    // execute our method for geo locating
                    geoLocate();
                }
                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                getDeviceLocation();
            }
        });
        hideSoftKeyboard();
    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate () : geo locating");
        String searchString = mSearchText.getText().toString();
        Geocoder geocoder = new Geocoder(FirstMapActivity.this);
        List<Address> resultList = new ArrayList<>();

        try {
            resultList = geocoder.getFromLocationName(searchString, 1);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage().toString());
        }
        if (resultList.size() > 0) {
            Address address = resultList.get(0);
            Log.d(TAG, "geoLocate () : found location" + address.toString());
            Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT);
//            slotLocation();
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Initializing map", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;
//        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.setIndoorEnabled(false);

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
//            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            mMap.getUiSettings().setZoomControlsEnabled(true);

            inIt();


        }
    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.FirstMap);

        mapFragment.getMapAsync(FirstMapActivity.this);

    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
//                            double lat = currentLocation.getLatitude();
//                            double lng = currentLocation.getLongitude();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM, "You are here !");

                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(FirstMapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, Float zoom, String title) {
        Log.d(TAG, "moving camera");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        if (!title.equalsIgnoreCase("You are here !")) {
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title);
            Marker locMarker = mMap.addMarker(markerOptions);
            locMarker.showInfoWindow();// this line shows title with out click
            Log.d(TAG, "marked pin");
            hideSoftKeyboard();
            //=======================================================

//            Marker m1 = mMap.addMarker(new MarkerOptions()
//                    .position(new LatLng(latLng.latitude+0.0000500, latLng.latitude+0.00000500))
//                    .anchor(0.5f, 0.5f)
//                    .title("Title1"));
//
//
//            Marker m2 = mMap.addMarker(new MarkerOptions()
//                    .position(new LatLng(latLng.latitude+0.0001000,latLng.latitude+0.00000500))
//                    .anchor(0.5f, 0.5f)
//                    .title("Title2"));
//
//
//            Marker m3 = mMap.addMarker(new MarkerOptions()
//                    .position(new LatLng(latLng.latitude+0.0001500,latLng.latitude+0.00001000))
//                    .anchor(0.5f, 0.5f)
//                    .title("Title3"));

        }


    }
    private void markPin(LatLng latLng,String title){
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title);
        Marker locMarker = mMap.addMarker(markerOptions);
        locMarker.showInfoWindow();// this line shows title with out click
        Log.d(TAG, "marked pin");
        hideSoftKeyboard();
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    private void hideSoftKeyboard() {
        Log.d(TAG, "Hinding keyboard");
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    /*
        --------------------------- google places API autocomplete suggestions -----------------
     */

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hideSoftKeyboard();

            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                Log.d(TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);

            try{
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                Log.d(TAG, "onResult: name: " + place.getName());
                mPlace.setAddress(place.getAddress().toString());
                Log.d(TAG, "onResult: address: " + place.getAddress());
//                mPlace.setAttributions(place.getAttributions().toString());
//                Log.d(TAG, "onResult: attributions: " + place.getAttributions());
                mPlace.setId(place.getId());
                Log.d(TAG, "onResult: id:" + place.getId());
                mPlace.setLatlng(place.getLatLng());
                Log.d(TAG, "onResult: latlng: " + place.getLatLng());
                mPlace.setRating(place.getRating());
                Log.d(TAG, "onResult: rating: " + place.getRating());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                Log.d(TAG, "onResult: phone number: " + place.getPhoneNumber());
                mPlace.setWebsiteUri(place.getWebsiteUri());
                Log.d(TAG, "onResult: website uri: " + place.getWebsiteUri());

                Log.d(TAG, "onResult: place: " + mPlace.toString());
            }catch (NullPointerException e){
                Log.e(TAG, "onResult: NullPointerException: " + e.getMessage() );
            }

            moveCamera(new LatLng(place.getViewport().getCenter().latitude,
                    place.getViewport().getCenter().longitude), DEFAULT_ZOOM, mPlace.getName());

            slotLocation();

            places.release();
        }
    };

    protected void slotLocation(){
        String result = "";
        //Instantiate new instance of our class
        URLConnectionActivity getRequest = new URLConnectionActivity();
        //Perform the doInBackground method, passing in our url
        try {
            result = getRequest.execute(url).get();
            if (!result.isEmpty()){
                String[] latLangString = result.split(",");
                LatLng location = new LatLng(Double.parseDouble(latLangString[0]), Double.parseDouble(latLangString[1]));
                markPin(location,"async task");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "exception  : " + e.getMessage().toString());
        } catch (ExecutionException e) {
            Log.e(TAG, "exception : " + e.getMessage().toString());
        }
    }
    protected void slotLocationLoader(){
        String result = "";
        //Instantiate new instance of our class
        URLConnectionActivity getRequest = new URLConnectionActivity();
        //Perform the doInBackground method, passing in our url
        try {
            result = getRequest.execute(url).get();
            if (!result.isEmpty()){
                String[] latLangString = result.split(",");
                LatLng location = new LatLng(Double.parseDouble(latLangString[0]), Double.parseDouble(latLangString[1]));
                markPin(location,"loader task");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "exception  : " + e.getMessage().toString());
        } catch (ExecutionException e) {
            Log.e(TAG, "exception : " + e.getMessage().toString());
        }
    }

}
