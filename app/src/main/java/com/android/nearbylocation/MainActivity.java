package com.android.nearbylocation;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.android.nearbylocation.dialog.DialogInfo;
import com.android.nearbylocation.dialog.Progress;
import com.android.nearbylocation.model.NearByApiResponse;
import com.android.nearbylocation.modelPlaceDetails.PlaceDetailsResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location location;
    private int PROXIMITY_RADIUS = 8000;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    NearByApiResponse nearByApiResponse;
    PlaceDetailsResponse placeDetailsResponse;
    ConnectionUtility connectionUtility;

    SearchableSpinner spnrPlaceType;
    String arrPlaceType[] = {"select","accounting","airport","amusement_park","aquarium","art_gallery","atm","bakery","bank","bar","beauty_salon	",
            "bicycle_store","book_store","bowling_alley","bus_station","cafe","campground","car_dealer",
            "car_rental","car_repair","car_wash","casino","cemetery","church","city_hall","clothing_store","convenience_store","courthouse",
            "dentist","department_store","doctor","electrician","electronics_store","embassy","fire_station","florist","funeral_home","furniture_store",
            "gas_station","gym","hair_care","hardware_store","hindu_temple","home_goods_store","hospital","insurance_agency",
            "jewelry_store","laundry","lawyer","library","liquor_store","local_government_office","locksmith","lodging","meal_delivery","meal_takeaway",
            "mosque","movie_rental","movie_theater","moving_company","museum","night_club","painter","park","parking","pet_store","pharmacy","physiotherapist",
            "plumber","police","post_office","real_estate_agency","restaurant","roofing_contractor","rv_park","school","shoe_store","shopping_mall","spa","stadium",
            "storage","store","subway_station","synagogue","taxi_stand","train_station","transit_station","travel_agency","university","veterinary_care","zoo",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //To check permissions above M as below it making issue and gives permission denied on samsung and other phones.
        connectionUtility = new ConnectionUtility(this);
        spnrPlaceType = (SearchableSpinner) findViewById(R.id.spnrPlaceType);

        spnrPlaceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position > 0)
                {
                    findPlaces(arrPlaceType[position].toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }

        if(connectionUtility.isConnectingToInternet())
        {
            //To check google play service available
            if (!isGooglePlayServicesAvailable())
            {
                Toast.makeText(this, "Google Play Services not available.", Toast.LENGTH_SHORT).show();
                finish();
            }
            else
            {
                if (isLocationOrGPSAvailable())
                {
                    // when the map is ready to be used.
                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    supportMapFragment.getMapAsync(this);//this line of code initially called the OnMapReadyCallback
                }
                else
                {
                    Toast.makeText(this, "Please enable the location!", Toast.LENGTH_SHORT).show();
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            }
        }
        else
        {
            final AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setTitle("No Internet Connection..")
                    .setMessage("Make sure the data connection is on!")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private void findPlaces(String strType)
    {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?sensor=true&location="+location.getLatitude()+","+location.getLongitude()
                +"&radius="+PROXIMITY_RADIUS+"&type="+strType+"&key=AIzaSyCHpKE86Yoz41v8H5zXFA0jq0LIzCzSE70";
        AsyncHttpClient client = new AsyncHttpClient();

        client.post(this, url, null, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Gson gson = new Gson();
                nearByApiResponse = gson.fromJson(new String(responseBody),NearByApiResponse.class);

                googleMap.clear();
                if(nearByApiResponse.getResults().size() > 0){
                    try {
                        googleMap.clear();
                        // This loop will go through all the results and add marker on each location.
                        for (int i = 0; i < nearByApiResponse.getResults().size(); i++) {
                            Double lat = nearByApiResponse.getResults().get(i).getGeometry().getLocation().getLat();
                            Double lng = nearByApiResponse.getResults().get(i).getGeometry().getLocation().getLng();
                            String placeName = nearByApiResponse.getResults().get(i).getName();
                            String placeID = nearByApiResponse.getResults().get(i).getPlaceId();
                            String vicinity = nearByApiResponse.getResults().get(i).getVicinity();
                            MarkerOptions markerOptions = new MarkerOptions();
                            LatLng latLng = new LatLng(lat, lng);
                            // Location of Marker on Map
                            markerOptions.position(latLng);
                            // Title for Marker
                            markerOptions.title(placeName);
                            markerOptions.snippet(placeID);
                            // Color or drawable for marker
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                            // add marker
                            Marker m = googleMap.addMarker(markerOptions);
                            // move map camera
                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            googleMap.animateCamera(CameraUpdateFactory.zoomTo(12));

                            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker)
                                {
                                    String placeid = marker.getSnippet();
                                    Log.e("placeid",placeid);
                                    openPlaceDetailsDialog(placeid);
                                    return false;
                                }
                            });
                        }
                    } catch (Exception e) {
                        Log.d("onResponse", "There is an error");
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("onFailure", error.toString());
                error.printStackTrace();
                PROXIMITY_RADIUS += 10000;
            }
        });

    }

    private void openPlaceDetailsDialog(String placeid)
    {

        final Progress pd = new Progress(this);
        String place_url = "https://maps.googleapis.com/maps/api/place/details/json?placeid="+placeid+"&key="+getApplication().getResources().getString(R.string.nearby_webservice_key);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(this, place_url, null, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                if(pd!=null)
                {
                    pd.show();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                try {
                    Gson gson = new Gson();
                    placeDetailsResponse = gson.fromJson(new String(responseBody), PlaceDetailsResponse.class);

                    if (placeDetailsResponse.getStatus().equalsIgnoreCase("OK"))
                    {
                        final DialogInfo dialogInfo = new DialogInfo(MainActivity.this);
                        dialogInfo.show();
                        dialogInfo.getTv_title().setText(placeDetailsResponse.getResult().getName());
                        dialogInfo.getTv_1().setText(placeDetailsResponse.getResult().getVicinity());

                        dialogInfo.getTv_ok().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                dialogInfo.dismiss();
                            }
                        });

                        dialogInfo.getTv_4().setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                String url = placeDetailsResponse.getResult().getWebsite();
                                if(url!=null)
                                {
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse(url));
                                    startActivity(i);
                                }
                            }
                        });

                        dialogInfo.getTv_2().setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                String phone_number = placeDetailsResponse.getResult().getFormattedPhoneNumber();
                                if(phone_number!=null)
                                {
                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse("tel:"+phone_number));
                                    startActivity(intent);
                                }
                            }
                        });

                        if (!placeDetailsResponse.getResult().getFormattedPhoneNumber().isEmpty() ||
                                placeDetailsResponse.getResult().getFormattedPhoneNumber() != null)
                        {
                            dialogInfo.getTv_2().setText(placeDetailsResponse.getResult().getFormattedPhoneNumber());
                        }

                        if (placeDetailsResponse.getResult().getRating() != null)
                        {
                            dialogInfo.getTv_3().setText(placeDetailsResponse.getResult().getRating().toString());
                        }

                        if (!placeDetailsResponse.getResult().getWebsite().isEmpty() ||
                                placeDetailsResponse.getResult().getWebsite() != null)
                        {
                            dialogInfo.getTv_4().setText(placeDetailsResponse.getResult().getWebsite());
                        }

                        if (!placeDetailsResponse.getResult().getOpeningHours().getWeekdayText().isEmpty() ||
                                placeDetailsResponse.getResult().getOpeningHours().getWeekdayText() != null)
                        {
                            dialogInfo.getTv_5().setText(placeDetailsResponse.getResult().getOpeningHours().getWeekdayText().toString());
                        }


                    }
                }
                catch (Exception e)
                {
                    Log.e("Error in try",new String(e.getMessage()));
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }

            @Override
            public void onFinish() {
                super.onFinish();
                if(pd!=null)
                {
                    pd.dismiss();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                googleMap.setMyLocationEnabled(true);
            }
        }
        else
        {
            buildGoogleApiClient();
            googleMap.setMyLocationEnabled(true);
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this,"Could not connect google api",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location!=null)
        {
            this.location = location;
            if(spnrPlaceType.getSelectedItemPosition() == 0)
            {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }
        }
    }



    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result, 0).show();
            }
            return false;
        }
        return true;
    }

    public boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    public boolean isLocationOrGPSAvailable()
    {
        LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try
        {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch(Exception ex) {}

        try
        {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        catch(Exception ex) {}

        if(gps_enabled || network_enabled)
        {
            // notify user
            return  true;
        }

        return false;
    }
}
