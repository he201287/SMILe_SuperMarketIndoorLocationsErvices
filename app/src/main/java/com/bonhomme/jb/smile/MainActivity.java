/*Vous trouverez les liens vers les licenses nécessaires pour l'utilisation du code dans README.md*/
package com.bonhomme.jb.smile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
    private Button mProfileBtn;
    private IndoorLocationView mLocationView;

    private LocationPosition mLocalPos;
    private LocationPosition mTestPos;
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

        mProfileBtn = findViewById(R.id.user_profile);
        mProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToUserProfile = new Intent(MainActivity.this, userProfile.class);
                startActivity(goToUserProfile);
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
                //mLocation = location;

                // Initialize the IndoorLocationManager
                // With foreground() .withScannerInForegroundService(notification)
                mIndoorLocationManager = new IndoorLocationManagerBuilder(getApplicationContext(), location, mCldCred)
                        .withDefaultScanner()
                        .build();

                startPositioning();


                // Setting location listener to update location on the map
                mIndoorLocationManager.setOnPositionUpdateListener(new OnPositionUpdateListener() {
                    @Override
                    public void onPositionUpdate(LocationPosition localPosition) {

                        LocationPosition testPos = new LocationPosition(3,1, 0.0);
                        mLocalPos = localPosition;
                        mTestPos = testPos;
                        if(distanceTo() < 1) {
                            Log.d("YOOOOOOOOOOOOOOOOOOOOOOOOOLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLOOOOOOOOOOOOOOO ", "VOUS ETES A MOINS DE 3 METRES SUR L AXE DES X DE LA POSITION DE TEST");
                        }
                        mLocationView.updatePosition(localPosition);
                        Log.d("UPDATED POS", "POS : " + localPosition);
                    }
                    @Override
                    public void onPositionOutsideLocation() {
                        mLocationView.hidePosition();
                        Log.d("UPDATED POS", "Hidden Position");
                    }
                });


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

    private double distanceTo() {
        double mDistanceTo = mTestPos.getX() - mLocalPos.getX();
        return mDistanceTo;
    }

    protected void startPositioning() {
        mIndoorLocationManager.startPositioning();
        Toast.makeText(MainActivity.this, "You can now move on the map",
                Toast.LENGTH_SHORT).show();
        Log.d("POSITIONING", "STARTING");
    }

    @Override
    protected void onStop() {
        mIndoorLocationManager.stopPositioning();
        super.onStop();

    }
}
