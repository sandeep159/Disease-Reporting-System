package com.example.drs;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.google.android.gms.location.LocationCallback;
import com.firebase.geofire.GeoFire;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GeoQueryEventListener, IOnLoadLocationListener {
    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentUser;
    private DatabaseReference  myLocationRef;
    private GeoFire geoFire;
    private List<LatLng> Areas;
    private IOnLoadLocationListener listener;
    private String[] AreaName=new String[10];
    private HashMap<String,String> Description;
    private String CurrentAreaChecking;
    private double currentLong;
    private double currentLat;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Dexter.withActivity(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        buildLocationRequest();
                        buildLocationCallback();
                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);


                        initArea();
                        settingGeoFire();




                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MapsActivity.this, "You must enable permission", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

    }

    private void initArea() {
          listener =this;

          FirebaseDatabase.getInstance().getReference("Area")
                .child("Location")
                  .addListenerForSingleValueEvent(new ValueEventListener() {
                      @Override
                      public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<MyLatLng> latLngList=new ArrayList<>();
                        for(DataSnapshot locationSnapshot:snapshot.getChildren()){
                            MyLatLng latLng=locationSnapshot.getValue(MyLatLng.class);
                            latLngList.add(latLng);
                        }
                        listener.onLoadLocationSuccess(latLngList);
                      }

                      @Override
                      public void onCancelled(@NonNull DatabaseError error) {
                        listener.OnLoadLocationFailed(error.getMessage());
                      }
                  });


//        Areas=new ArrayList<>();
//        Areas.add(new LatLng(11.869635,75.398138));
//        Areas.add(new LatLng(11.868768,75.402539));
//        Areas.add(new LatLng(11.869152,75.399286));
//
//        FirebaseDatabase.getInstance().getReference("Area")
//                .child("Location")
//                .setValue(Areas)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        Toast.makeText(MapsActivity.this, "Updated", Toast.LENGTH_SHORT).show();
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(MapsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });

    }

    private void settingGeoFire() {
        myLocationRef= FirebaseDatabase.getInstance().getReference("MyLocation");
        geoFire=new GeoFire(myLocationRef);

    }



    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(final LocationResult locationResult) {
                if (mMap != null) {


                    geoFire.setLocation("You", new GeoLocation(locationResult.getLastLocation().getLatitude(),
                            locationResult.getLastLocation().getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (currentUser != null) currentUser.remove();
                            currentUser = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(locationResult.getLastLocation().getLatitude(),
                                            locationResult.getLastLocation().getLongitude()))
                                    .title("Your location"));

                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUser.getPosition(), 12.0f));
                        }
                    });

                }
            }
        };

    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (fusedLocationProviderClient != null)
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        int i=0;
        for(LatLng latLng:Areas){
            mMap.addCircle(new CircleOptions().center(latLng).radius(500).strokeColor(Color.BLUE).fillColor(0x220000FF)
            .strokeWidth(5.0f));
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(AreaName[i]));
            CurrentAreaChecking=AreaName[i];
            GeoQuery geoQuery =geoFire.queryAtLocation(new GeoLocation(latLng.latitude,latLng.longitude),0.5f);
            geoQuery.addGeoQueryEventListener(MapsActivity.this);

            i+=1;
        }
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                Intent intent=new Intent(MapsActivity.this,DescriptionActivity.class);
                intent.putExtra("CLICKED_NAME",marker.getTitle());
                intent.putExtra("CLICKED_AREA_DESCRIPTION",Description.get(marker.getTitle()));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        currentLong=location.longitude;
        currentLat=location.latitude;
        sendNotification("DRS",String.format("%s entered the dangerous area",key));

    }

    @Override
    public void onKeyExited(String key) {
//        sendNotification("DRS",String.format("%s left the dangerous area",key));
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
//        sendNotification("DRS",String.format("%s moved the dangerous area",key));
    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        Toast.makeText(this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String title,String Content) {
    String NOTIFICATION_CHANNEL_ID="DRS_multiple_location";
        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

            NotificationChannel notificationChannel=new NotificationChannel(NOTIFICATION_CHANNEL_ID,"My Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("Channel Description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationManager.createNotificationChannel(notificationChannel);

        }

        Intent resultIntent=new Intent(this,DescriptionActivity.class);
        resultIntent.putExtra("CLICKED_NAME",String.valueOf(currentLong));
        resultIntent.putExtra("CLICKED_AREA_DESCRIPTION",Description.get(String.valueOf(getCurrentLong(currentLong))));
        PendingIntent pendingIntent=PendingIntent.getActivity(this,1,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(Content)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent);

        Notification notification=builder.build();
        notificationManager.notify(new Random().nextInt(),notification);


    }

    private double getCurrentLong(double currentLong){
        double min_long=100000000.0000000000;
        double TriggeredLong=0.00;
        double distance;
        for(LatLng latLng:Areas){
            distance=Math.abs(currentLong - latLng.longitude);
            if(distance<min_long){
                min_long=distance;
                TriggeredLong=latLng.longitude;
            }
        }
        return TriggeredLong;
    }

    @Override
    public void onLoadLocationSuccess(List<MyLatLng> latLngs) {
        Description=new HashMap<String, String>();
        Areas=new ArrayList<>();
        int i=0;
        for(MyLatLng myLatLng:latLngs){
            LatLng convert=new LatLng(myLatLng.getLatitude(),myLatLng.getLongitude());
            Areas.add(convert);
            AreaName[i]=myLatLng.getAreaName();
            Description.put(AreaName[i],myLatLng.getDescription());
            Description.put(String.valueOf(myLatLng.getLongitude()),myLatLng.getDescription());
            i+=1;

        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    @Override
    public void OnLoadLocationFailed(String message) {
        Toast.makeText(this, ""+message, Toast.LENGTH_SHORT).show();
    }


}