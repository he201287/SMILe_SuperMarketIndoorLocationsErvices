package com.bonhomme.jb.smile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.estimote.indoorsdk.EstimoteCloudCredentials;
import com.estimote.indoorsdk.IndoorLocationManagerBuilder;
import com.estimote.indoorsdk_module.algorithm.OnPositionUpdateListener;
import com.estimote.indoorsdk_module.algorithm.ScanningIndoorLocationManager;
import com.estimote.indoorsdk_module.cloud.CloudCallback;
import com.estimote.indoorsdk_module.cloud.EstimoteCloudException;
import com.estimote.indoorsdk_module.cloud.IndoorCloudManager;
import com.estimote.indoorsdk_module.cloud.IndoorCloudManagerFactory;
import com.estimote.indoorsdk_module.cloud.Location;
import com.estimote.indoorsdk_module.cloud.LocationPosition;
import com.estimote.indoorsdk_module.view.IndoorLocationView;
import com.estimote.internal_plugins_api.cloud.CloudCredentials;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button mSignOutBtn;
    private IndoorLocationView mLocationView;

    private Location mLocation;
    private IndoorCloudManager mCldMng;
    private CloudCredentials mCldCred;

    protected ScanningIndoorLocationManager mIndoorLocationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mSignOutBtn = findViewById(R.id.user_sign_out);
        mSignOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
                Intent backToLoginScreen = new Intent(MainActivity.this, LoginActivity.class);
                finish();
                startActivity(backToLoginScreen);
                finish();
            }
        });

        // Authenticate the app to access the Estimote Cloud
        mCldCred = new EstimoteCloudCredentials("smile-0bg", "9e0f13942025ac504966bb6eb77e5a4d");
        mCldMng = new IndoorCloudManagerFactory().create(this, mCldCred);
        //final Notification notification = new Notification.Builder(this)
        //        .setContentTitle("fhrh")
        //        .setContentText("hgrherh")
        //        .setPriority(Notification.PRIORITY_HIGH)
        //        .build();

        mCldMng.getLocation("smile-indoor-location", new CloudCallback<Location>() {
            @Override
            public void success(Location location) {
                //do smthng with the location object
                mLocationView = findViewById(R.id.indoor_location_view);
                mLocationView.setLocation(location);
                mLocation = location;
                LocationPosition testPos = new LocationPosition(3,1, 2);
                //if(postition.distanceTo(testPos) < 5 ) {
//
                //}
                // Initialize the IndoorLocationManager
                // or foreground here foreground() .withScannerInForegroundService(notification)
                //mIndoorLocationManager.startPositioning();
                mIndoorLocationManager = new IndoorLocationManagerBuilder(getApplicationContext(), location, mCldCred)
                        .withDefaultScanner()
                        .build();
                yolo();
                // Setting location listener to update location on the map
                mIndoorLocationManager.setOnPositionUpdateListener(new OnPositionUpdateListener() {
                    @Override
                    public void onPositionUpdate(LocationPosition locationPosition) {
                        mLocationView.updatePosition(locationPosition);
                        Toast.makeText(MainActivity.this, "Position" + locationPosition, Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onPositionOutsideLocation() {
                        mLocationView.hidePosition();
                        Toast.makeText(MainActivity.this, "A PU MVT", Toast.LENGTH_SHORT).show();
                    }
                });


                // Toast.makeText(MainActivity.this, "MOUVEMENT0000000000001111111111111111", Toast.LENGTH_SHORT).show();
                //mIndoorLocationManager.startPositioning();
                // Toast.makeText(MainActivity.this, "MOUVEMENT00000000000000000000000000222222222222222", Toast.LENGTH_SHORT).show();
                // mIndoorLocationManager.stopPositioning();
            }
            @Override
            // If it fails to load the location
            public void failure(EstimoteCloudException e) {
                Toast.makeText(MainActivity.this, "Error While loading the location: " + e.getErrorCode(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signOut() {
        mAuth.signOut();
    }

    protected void yolo() {
        mIndoorLocationManager.startPositioning();
    }
//
    //@Override
    //protected void onStop() {
    //    super.onStop();
    //    mIndoorLocationManager.stopPositioning();
    //}
}
