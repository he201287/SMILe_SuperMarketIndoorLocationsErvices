/*Vous trouverez les liens vers les licenses nécessaires pour l'utilisation du code dans README.md*/
package com.bonhomme.jb.smile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
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

import com.estimote.mustard.rx_goodness.rx_requirements_wizard.Requirement;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory;
//import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;
//import com.estimote.proximity_sdk.api.ProximityObserver;
//import com.estimote.proximity_sdk.api.ProximityObserverBuilder;
//import com.estimote.proximity_sdk.api.ProximityZone;
//import com.estimote.proximity_sdk.api.ProximityZoneBuilder;
//import com.estimote.proximity_sdk.api.ProximityZoneContext;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public class MainActivity extends AppCompatActivity {
    private static final String IS_ADMIN_KEY = "isAdmin";
    private static final String CHANNEL_ID = "foregroundService";
    private FirebaseAuth mAuth;
    private String fireBaseUid;
    private String mSearchedItem;
    private ArrayList<String> mArrayList;
    private SearchView mShelfSearchView;
    private Button mSignOutBtn;
    private Button mProfileBtn;
    private Button mAdminBtn;
    private ImageButton mShelfBtn;
    private ImageButton mDairyShelfBtn;
    private ImageButton mShoppingCartBtn;
    private IndoorLocationView mLocationView;

    private LocationPosition mLocalPos;
    private LocationPosition mTestPos;
    private IndoorCloudManager mCldMng;
    private com.estimote.indoorsdk.EstimoteCloudCredentials mCldCred;
    private Notification mNotification;
    private Boolean TEMP = false;

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 01;


    private ScanningIndoorLocationManager mIndoorLocationManager;

    protected void onStart(){

        mAdminBtn.setVisibility(View.INVISIBLE);
        mAdminBtn.setEnabled(false);
        getAdminState();
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

    //Request the permission for the Location access
    //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    //   requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
    //}

        // Authenticate the app to access the Estimote Cloud
        mCldCred = new com.estimote.indoorsdk.EstimoteCloudCredentials("smile-0bg", "9e0f13942025ac504966bb6eb77e5a4d");
        mCldMng = new IndoorCloudManagerFactory().create(this, mCldCred);

        createNotifChannel();
        mNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
               .setSmallIcon(R.drawable.ic_shopping_cart)
               .setContentTitle("SMILe")
               .setContentText("The application will continue scanning while running in background, it may impact your battery usage")
               .build();


        mCldMng.getLocation("smileindoorloc-17s", new CloudCallback<Location>() {
            @Override
            public void success(Location location) {
                //do smthng with the location object
                mLocationView = findViewById(R.id.indoor_location_view);
                mLocationView.setLocation(location);

                // Initialize the IndoorLocationManager
                // With foreground() .withScannerInForegroundService(mNotification)
                // Default scanner:  .withDefaultScanner()
                mIndoorLocationManager = new IndoorLocationManagerBuilder(getApplicationContext(), location, mCldCred)
                        .withScannerInForegroundService(mNotification)
                        .build();

                startPositioning();

                // Setting location listener to update location on the map
                mIndoorLocationManager.setOnPositionUpdateListener(new OnPositionUpdateListener() {
                    @Override
                    public void onPositionUpdate(LocationPosition localPosition) {

                        mLocationView.updatePosition(localPosition);
                        Log.d("UPDATED POS", "POS : " + localPosition);

                        LocationPosition origin = new LocationPosition(0,0,0.0);
                        LocationPosition shelfPos25 = new LocationPosition(2.5,4, 0.0);
                        //LocationPosition testPos3 = new LocationPosition(1,1,0.0);
                        LocationPosition shelfPos = new LocationPosition(2.5, 3.5, 0.0);
                        mTestPos = shelfPos25;


                        if(getTEMP() == true) {
                            mLocationView.setCustomPoints(Arrays.asList(origin,shelfPos, shelfPos25));
                            //System.out.println("TEMP IS TRUE");
                        }

                        mLocalPos = localPosition;

                        if(distanceTo() < 1 && distanceToY() < 1) {
                            Log.d("POSITION INFO ", "VOUS ETES A MOINS D'1 METRES SUR L'AXE DES X et Y DE LA POSITION DE TEST");
                        }
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

        //Wizard

        // Checks if all the requirements are fulfilled (Bluetooth, location, ...).
        RequirementsWizardFactory.createEstimoteRequirementsWizard().fulfillRequirements(
                this,
                new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        //mProxObs.addProximityZone(mFruitProxZone).start();
                        //startPositioning();
                        return null;
                    }
                },

                new Function1<List<? extends Requirement>, Unit>() {
                    @Override
                    public Unit invoke(List<? extends Requirement> requirements) {
                        return null;
                    }
                },

                new Function1<Throwable, Unit>() {
                    @Override
                    public Unit invoke(Throwable throwable) {
                        return null;
                    }
                }
        );

        mShelfSearchView = findViewById(R.id.search_view);
        mShelfSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchFunction();
                //System.out.println("TEST TEMP VARIABLE VALUE :" + TEMP);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchFunction();
                //System.out.println("TEST TEMP VARIABLE VALUE :" + TEMP);
                return false;
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

        mShelfBtn = findViewById(R.id.fruit_shelf);
        mShelfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shelfManagement = new Intent(MainActivity.this, shelfActivity.class);
                startActivity(shelfManagement);
            }
        });

        mDairyShelfBtn = findViewById(R.id.dairy_shelf);
        mDairyShelfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shelfManagement = new Intent(MainActivity.this, shelfActivity.class);
                startActivity(shelfManagement);
            }
        });

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

    private void searchFunction() {
        mShelfSearchView = findViewById(R.id.search_view);
        mSearchedItem = String.valueOf(mShelfSearchView.getQuery());
        System.out.println("TEST RESEARCHED ITEM: " + mSearchedItem);
        mArrayList = new ArrayList<>();
        DocumentReference mDocumentReference = FirebaseFirestore.getInstance().collection("shelves").document("fruit_shelf");
        mDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
           @Override
           public void onSuccess(DocumentSnapshot documentSnapshot) {
               if(documentSnapshot.exists()) {
                   //System.out.println("Document data: " + documentSnapshot.getData());
                   Map<String, Object> test = documentSnapshot.getData();
                   ArrayList<String> distinctValues = new ArrayList<String>();

                   for(String key: test.keySet()) {
                       Object value = test.get(key);
                       String values = value.toString();
                       distinctValues = new ArrayList(Arrays.asList(values.replaceAll("[\\[|\\]]", "").split(",")));

                       for (int i = 0; i < distinctValues.size(); i++) {
                           //System.out.println("TEST: " + distinctValue.get(i));
                           mArrayList.add(distinctValues.get(i));
                       }

                   }
                   TEMP = mArrayList.contains(mSearchedItem) ? true : false;
                   //System.out.println("TEST: " + mArrayList);
                   System.out.println("TEST: " + TEMP);
                   Log.d("SUCCESS", "SHELF DATA HAS BEEN RETRIEVED");
               }
           }
       }).addOnFailureListener(new OnFailureListener() {
           @Override
           public void onFailure( Exception e) {
               Log.d("ERROR", "FAILED TO RETRIEVE THE SHELF DATA");
           }
       });

    }

    private void signOut() {
        mAuth.signOut();
    }

    private double distanceTo() {
        double mDistanceToX = mTestPos.getX() - mLocalPos.getX();
        return mDistanceToX;
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

    public Boolean getTEMP() {
        return TEMP;
    }
}
