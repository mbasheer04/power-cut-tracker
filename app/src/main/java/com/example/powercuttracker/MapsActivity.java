package com.example.powercuttracker;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.example.powercuttracker.database_api.APIClient;
import com.example.powercuttracker.database_api.APIInterface;
import com.example.powercuttracker.database_api.User;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.powercuttracker.databinding.ActivityMapsBinding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.location.Location;
import android.provider.Settings;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;


import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private static final String TAG = "MapsActivity";
    private PlacesClient mPlacesClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private String androidID;

    // Interface for Database API
    private APIInterface apiInterface;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    private Marker currentLocationMarker;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private LatLng mHomeLocation;
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        androidID = Settings.Secure.getString(this.getContentResolver(),Settings.Secure.ANDROID_ID);
        //db_api = new RestApiApplication();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Retrieves the toolbar. Uses binding syntax as opposed to getViewByID().
        Toolbar toolbar = binding.toolbar;

        // Initialize the Places client
        String apiKey = BuildConfig.MAPS_API_KEY; //Retrieves API Key
        Places.initialize(getApplicationContext(), apiKey);
        mPlacesClient = Places.createClient(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialise Database API interface using Retrofit
        apiInterface = APIClient.getRetrofitInstance().create(APIInterface.class);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // Enable the zoom controls for the map
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Prompt the user for permission, and start regular location updates.
        getLocationPermission();
        startLocationUpdates();

        // Location button retrieves device location on click.
        FloatingActionButton locationButton = binding.locationButton;
        FloatingActionButton homeButton = binding.homeButton;
        locationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                moveToCurrentLocation();
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TestApi();
                //moveToHome();
            }
        });
    }

    /**
     * Creates a LocationRequest and uses the FusedLocationProvider to request regular location updates.
     */
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(5000).build();

        // Location callback is used every 5000ms once location is ready to be received. Retrieves the last
        // known location using FusedLocationProvider, updates the global mLastKnownLocation variable, and
        // updates the location of the "Current Location" marker.
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult.getLastLocation() != null) {
                    mLastKnownLocation = locationResult.getLastLocation();
                    double latitude = mLastKnownLocation.getLatitude();
                    double longitude = mLastKnownLocation.getLongitude();

                    // Update the current location marker or add a new one
                    if (currentLocationMarker == null) {
                        currentLocationMarker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .title("Current Location")
                                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("blue_circle",60,60))));
                    } else {
                        currentLocationMarker.setPosition(new LatLng(latitude, longitude));
                    }
                }
            }
        };
        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void moveToHome(){
        if(mHomeLocation != null){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mHomeLocation.latitude,
                            mHomeLocation.longitude), DEFAULT_ZOOM));
        } else {
            setHome();
        }
    }

    // Method for testing API connection.
    public void TestApi(){
        apiInterface = APIClient.getRetrofitInstance().create(APIInterface.class);

        Call<User> call = apiInterface.getUserById(2L);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    System.out.println(user.getAndroid_id());
                    System.out.println("Data received.");
                } else {
                    System.out.println("Error.");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // Handle network error
                System.out.println("Network error");
            }
        });
    }

    private void setHome(){
//        try {
//            ResultSet rs = db.selectSingle("user", "android_id", androidID);
//            if (!rs.first()){
//                db.insertSingle("user","home_lat,home_long,android_id",
//                        "" + mLastKnownLocation.getLatitude() + "," +
//                                mLastKnownLocation.getLongitude() + "," + androidID);
//                rs = db.selectSingle("user", "android_id", androidID);
//            }
//            mHomeLocation = new LatLng(rs.getDouble("home_lat"),rs.getDouble("home_long"));
//            rs.close();
//        } catch (SQLException e){
//
//        }
    }

    private void moveToCurrentLocation(){
        if(mLastKnownLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        }
    }

    /**
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
    private void getLocationPermission() {
        mLocationPermissionGranted = false;
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    public Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}