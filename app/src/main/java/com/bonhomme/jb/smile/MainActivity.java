/*Vous trouverez les liens vers les licenses nécessaires pour l'utilisation du code dans README.md*/
package com.bonhomme.jb.smile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.estimote.indoorsdk_module.cloud.LocationLinearObject;
import com.estimote.indoorsdk_module.cloud.LocationPosition;
import com.estimote.indoorsdk_module.view.IndoorLocationView;
import com.estimote.internal_plugins_api.cloud.CloudCredentials;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String IS_ADMIN_KEY = "isAdmin";
    private FirebaseAuth mAuth;
    private String fireBaseUid;
    private Button mSignOutBtn;
    private Button mProfileBtn;
    private Button mAdminBtn;
    private ImageButton mShelfBtn;
    private ImageButton mShoppingCartBtn;
    private IndoorLocationView mLocationView;

    private LocationPosition mLocalPos;
    private LocationPosition mTestPos;
    private IndoorCloudManager mCldMng;
    private CloudCredentials mCldCred;
    private Notification mNotification;
    private static final String CHANNEL_ID = "foregroundService";



    protected ScanningIndoorLocationManager mIndoorLocationManager;

    protected void onStart(){
        super.onStart();
        mAdminBtn.setVisibility(View.INVISIBLE);
        mAdminBtn.setEnabled(false);
        getAdminState();
    }

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

        mAdminBtn = findViewById(R.id.user_admin);
        mAdminBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toAdminProfile = new Intent(MainActivity.this, adminUser.class);
                startActivity(toAdminProfile);
            }
        });

        mShoppingCartBtn = findViewById(R.id.user_shopping_list);
        mShoppingCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToShoppingCart = new Intent(MainActivity.this, shoppingCart.class);
                startActivity(goToShoppingCart);
            }
        });

        mShelfBtn = findViewById(R.id.shop_shelf);
        mShelfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shelfManagement = new Intent(MainActivity.this, shelfActivity.class);
                startActivity(shelfManagement);
            }
        });

        // Authenticate the app to access the Estimote Cloud
        mCldCred = new EstimoteCloudCredentials("smile-0bg", "9e0f13942025ac504966bb6eb77e5a4d");
        mCldMng = new IndoorCloudManagerFactory().create(this, mCldCred);
        //  Notification notification = new Notification.Builder(this)
        //          .setSmallIcon(R.drawable.ic_shopping_cart)
        //          .setContentTitle("SMILe")
        //          .setContentText("The application is running in background, it may impact your battery usage")
        //          .setPriority(Notification.PRIORITY_HIGH)
        //          .build();

        createNotifChannel();
        mNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
               .setSmallIcon(R.drawable.ic_shopping_cart)
               .setContentTitle("SMILe")
               .setContentText("The application will continue scanning while running in background, it may impact your battery usage")
               .build();

        mCldMng.getLocation("smile-indoor-location", new CloudCallback<Location>() {
            @Override
            public void success(Location location) {
                //do smthng with the location object
                mLocationView = findViewById(R.id.indoor_location_view);
                mLocationView.setLocation(location);

                // Initialize the IndoorLocationManager
                // With foreground() .withScannerInForegroundService(mNotification)
                // Default scanner:  .withDefaultScanner()
                // .withScannerInForegroundService(mNotification)
                mIndoorLocationManager = new IndoorLocationManagerBuilder(getApplicationContext(), location, mCldCred)
                        .withScannerInForegroundService(mNotification)
                        .build();

                startPositioning();


                // Setting location listener to update location on the map
                mIndoorLocationManager.setOnPositionUpdateListener(new OnPositionUpdateListener() {
                    @Override
                    public void onPositionUpdate(LocationPosition localPosition) {

                        LocationPosition origin = new LocationPosition(0,0,0.0);
                        LocationPosition shelfPos15 = new LocationPosition(1.5,4, 0.0);
                        LocationPosition shelfPos2 = new LocationPosition(2,4, 0.0);
                        LocationPosition shelfPos25 = new LocationPosition(2.5,4, 0.0);
                        LocationPosition shelfPos3 = new LocationPosition(3, 4, 0.0);
                        LocationPosition shelfPos35 = new LocationPosition(3.5, 4, 0.0);
                        LocationPosition testPos3 = new LocationPosition(1,1,0.0);


                        LocationLinearObject testShelf = new LocationLinearObject(1.0, 4.0, 3.0, 3.0, 3.0, 1);

                        mLocalPos = localPosition;
                        mTestPos = shelfPos25;
                        mLocationView.setCustomPoints(Arrays.asList(origin, shelfPos15, shelfPos2, shelfPos25, shelfPos3, shelfPos35, testPos3));

                        if(distanceTo() < 1 && distanceToY() < 1) {
                            Log.d("POSITION INFO ", "VOUS ETES A MOINS D'1 METRES SUR L'AXE DES X et Y DE LA POSITION DE TEST");
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

    private void getAdminState() {
        fireBaseUid  = FirebaseAuth.getInstance().getUid();
        DocumentReference mDocumentReference = FirebaseFirestore.getInstance().collection("users").document(fireBaseUid);
        mDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    boolean isAdmin = documentSnapshot.getBoolean(IS_ADMIN_KEY);
                    if (isAdmin == true) {
                        mAdminBtn.setVisibility(View.VISIBLE);
                        mAdminBtn.setEnabled(true);
                    } else {
                        return;
                    }

                    Log.d("SUCCESS", "ADMIN STATE HAS BEEN RETRIEVED");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("ERROR", "FAILED TO RETRIEVE THE USER ADMIN STATE");
            }
        });
    }

    private void createNotifChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel indoorLocation = new NotificationChannel(CHANNEL_ID , "ForeGroundService", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(indoorLocation);
        }

    }

    private void signOut() {
        mAuth.signOut();
    }

    private double distanceTo() {
        double mDistanceTo = mTestPos.getX() - mLocalPos.getX();
        return mDistanceTo;
    }

    private double distanceToY() {
        double mDistanceToY = mTestPos.getY() - mLocalPos.getY();
        return mDistanceToY;
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
