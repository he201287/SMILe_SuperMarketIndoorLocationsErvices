/*Vous trouverez les liens vers les licenses nécessaires pour l'utilisation du code dans README.md*/
package com.bonhomme.jb.smile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.estimote.proximity_sdk.api.ProximityObserver;
import com.estimote.proximity_sdk.api.ProximityObserverBuilder;
import com.estimote.proximity_sdk.api.ProximityZone;
import com.estimote.proximity_sdk.api.ProximityZoneBuilder;
import com.estimote.proximity_sdk.api.ProximityZoneContext;

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
    private Boolean TEMP = false;
    private Boolean TMP = false;
    private Boolean TEMPO = false;

    //Firebase Variables
    private FirebaseAuth mAuth;
    private String fireBaseUid;

    //Indoor Variables
    private com.estimote.indoorsdk.EstimoteCloudCredentials mCldCred;
    private IndoorCloudManager mCldMng;
    private ScanningIndoorLocationManager mIndoorLocationManager;
    private IndoorLocationView mLocationView;
    private LocationPosition mLocalPos;
    private LocationPosition mTestPos;

    //Proximity Variables
    private com.estimote.proximity_sdk.api.EstimoteCloudCredentials mProxCldCred;
    private ProximityObserver mProxObs;
    private ProximityObserver.Handler obsHandler;
    private ProximityZone mFruitProxZone;
    private ProximityZone mDairyProxZone;
    private ProximityZone mWineProxZone;
    private ProximityZone mDoorProxZone;
    private Boolean fruitFound;
    private Boolean dairyFound;
    private Boolean wineFound;


    private Notification mNotification;
    private String mSearchedItem;
    private ArrayList<String> mFruitArrayList;
    private ArrayList<String> mDairyArrayList;
    private ArrayList<String> mWineArrayList;
    private ArrayList<String> mFruitFoundArray;
    private ArrayList<String> mDairyFoundArray;
    private ArrayList<String> mWineFoundArray;
    private ArrayList<String> mShoppingCartArrayList;
    private SearchView mShelfSearchView;
    private Button mSignOutBtn;
    private Button mProfileBtn;
    private Button mAdminBtn;
    private ImageButton mShelfBtn;
    private ImageButton mDairyShelfBtn;
    private ImageButton mWineShelfBtn;
    private ImageButton mShoppingCartBtn;

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

        getShelvesProducts();
        getShoppingListContent();

        mAuth = FirebaseAuth.getInstance();

        // Authenticate the app to access the Estimote Cloud
        mCldCred = new com.estimote.indoorsdk.EstimoteCloudCredentials("smile-0bg", "9e0f13942025ac504966bb6eb77e5a4d");
        mCldMng = new IndoorCloudManagerFactory().create(this, mCldCred);

        mProxCldCred = new com.estimote.proximity_sdk.api.EstimoteCloudCredentials("smile-0bg", "9e0f13942025ac504966bb6eb77e5a4d");

        // Call to the create createNotifChannel function
        createNotifChannel();

        // Creates a new notification for the app which will be displayed in the notification channel created above
        mNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
               .setSmallIcon(R.drawable.ic_shopping_cart)
               .setContentTitle("SMILe")
               .setContentText("The application will continue scanning while running in background, it may impact your battery usage")
               .build();

        // Proximity observer, scans the beacons.
        // Can change the scanning mode to reduce battery usage / Scanning efficiency
        mProxObs = new ProximityObserverBuilder(getApplicationContext(), mProxCldCred)
                .withLowLatencyPowerMode() // Most reliable mode, but drains battery faster
                .onError(new Function1<Throwable, Unit>() { // Catches errors (scan, cloud connections, ...)
                    @Override
                    public Unit invoke(Throwable throwable) {
                        Log.d("Error", "ProximityObserverBuilder Error : " + throwable);
                        return null;
                    }
                })
                .withScannerInForegroundService(mNotification)
                .build();

        // Create a beacon zone for the fruitZone tagged beacon.
        mFruitProxZone = new ProximityZoneBuilder()
                .forTag("fruitZone")
                .inNearRange() // Can be changed to .inFarRange() or .inCustomRange()
                .onEnter(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext proximityZoneContext) {
                        searchfruitArrayFunction();
                        return null;
                    }
                })
                .onExit(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext proximityZoneContext) {
                        mShelfBtn = findViewById(R.id.fruit_shelf);
                        mShelfBtn.setBackgroundColor(Color.LTGRAY);
                        mFruitFoundArray.clear();
                        return null;
                    }
                })
                .build();

        mDairyProxZone = new ProximityZoneBuilder()
                .forTag("dairyZone")
                .inNearRange()
                .onEnter(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext proximityZoneContext) {
                        searchdairyArrayFunction();
                        Log.d("dairyZone", "dairyZone entered");
                        return null;
                    }
                })
                .onExit(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext proximityZoneContext) {
                        mDairyShelfBtn = findViewById(R.id.dairy_shelf);
                        mDairyShelfBtn.setBackgroundColor(Color.LTGRAY);
                        mDairyFoundArray.clear();
                        return null;
                    }
                })
                .build();

        mWineProxZone = new ProximityZoneBuilder()
                .forTag("wineZone")
                .inNearRange()
                .onEnter(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext proximityZoneContext) {
                        searchwineArrayFunction();
                        Log.d("wineZone", "wineZone entered");
                        return null;
                    }
                })
                .onExit(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext proximityZoneContext) {
                        mWineShelfBtn = findViewById(R.id.wine_shelf);
                        mWineShelfBtn.setBackgroundColor(Color.LTGRAY);
                        mWineFoundArray.clear();
                        return null;
                    }
                })
                .build();

        mDoorProxZone = new ProximityZoneBuilder()
                .forTag("Entrance")
                .inFarRange()
                .onEnter(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext proximityZoneContext) {
                        return null;
                    }
                })
                .onExit(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext proximityZoneContext) {
                        return null;
                    }
                })
                .build();

        //Wizard
        // Checks if all the requirements are fulfilled (Bluetooth, location, ...).
        RequirementsWizardFactory.createEstimoteRequirementsWizard().fulfillRequirements(
                this,
                // If the requirements are fulfilled
                new Function0<Unit>() {
                    @Override
                    public Unit invoke() {

                        // Get the location from Estimote cloud and displays it in the indoor_location_view
                        // It will Initialize the positioning system
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
                                        LocationPosition dairyShelf = new LocationPosition(2.0,3, 0.0);
                                        //LocationPosition testPos3 = new LocationPosition(1,1,0.0);
                                        LocationPosition shelfPos = new LocationPosition(2.5, 3.5, 0.0);
                                        LocationPosition shelfPos25 = new LocationPosition(2.0, 1.5, 0.0);
                                        mTestPos = shelfPos25;

                                        if(getTEMP() == true) {
                                            mLocationView.setCustomPoints(Arrays.asList(origin,shelfPos, shelfPos25));
                                            //System.out.println("TEMP IS TRUE");
                                        } else if (getTMP() == true) {
                                            mLocationView.setCustomPoints(Arrays.asList(origin,dairyShelf));
                                        } else if (getTEMPO() == true) {
                                            mLocationView.setCustomPoints(Arrays.asList(origin));
                                        }

                                        mLocalPos = localPosition;

                                        if(distanceTo() < 1 && distanceToY() < 1) {
                                            Log.d("POSITION INFO ", "VOUS ETES A MOINS D'1 METRES SUR L'AXE DES X et Y DE LA POSITION DE TEST");
                                        }
                                    }
                                    // Hide the user's position if the user is outside the location
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
                                        Toast.LENGTH_LONG).show();
                            }
                        });

                        // Start observing the zones previously created
                        obsHandler = mProxObs.startObserving(mFruitProxZone, mDairyProxZone, mWineProxZone, mDoorProxZone);

                        return null;
                    }
                },
                // If the requirements are not fulfilled
                new Function1<List<? extends Requirement>, Unit>() {
                    @Override
                    public Unit invoke(List<? extends Requirement> requirements) {
                        Toast.makeText(MainActivity.this, "Error one or several requirements are missing, " +
                                        "scanning won't work until all requirements are fulfilled: " + requirements.toString(),
                                Toast.LENGTH_LONG).show();
                        return null;
                    }
                },
                // if an error has occurred
                new Function1<Throwable, Unit>() {
                    @Override
                    public Unit invoke(Throwable throwable) {
                        Toast.makeText(MainActivity.this, "Whoops some error has occurred: " + throwable.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return null;
                    }
                }
        );

        // Initialize the search view and add a listener
        mShelfSearchView = findViewById(R.id.search_view);
        mShelfSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchFunction();
                //System.out.println("TEST TEMP VARIABLE VALUE :" + TEMP);
                if (getTEMP() == true) {
                    mShelfBtn = findViewById(R.id.fruit_shelf);
                    mShelfBtn.setBackgroundColor(Color.GREEN);
                }

                if(getTMP() == true) {
                    mDairyShelfBtn = findViewById(R.id.dairy_shelf);
                    mDairyShelfBtn.setBackgroundColor(Color.GREEN);
                }

                if(getTEMPO() == true) {
                    mWineShelfBtn = findViewById(R.id.wine_shelf);
                    mWineShelfBtn.setBackgroundColor(Color.GREEN);
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchFunction();
                if (getTEMP() == true) {
                    mShelfBtn = findViewById(R.id.fruit_shelf);
                    mShelfBtn.setBackgroundColor(Color.GREEN);
                }

                if(getTMP() == true) {
                    mDairyShelfBtn = findViewById(R.id.dairy_shelf);
                    mDairyShelfBtn.setBackgroundColor(Color.GREEN);
                }

                if(getTEMPO() == true) {
                    mWineShelfBtn = findViewById(R.id.wine_shelf);
                    mWineShelfBtn.setBackgroundColor(Color.GREEN);
                }

                return false;
            }
        });

        // Initialize the profile button and add an onClick listener
        mProfileBtn = findViewById(R.id.user_profile);
        mProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToUserProfile = new Intent(MainActivity.this, userProfile.class);
                startActivity(goToUserProfile);
            }
        });

        // Initialize the admin button and add an onClick listener
        mAdminBtn = findViewById(R.id.user_admin);
        mAdminBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toAdminProfile = new Intent(MainActivity.this, adminUser.class);
                startActivity(toAdminProfile);
            }
        });

        // Initialize the shopping cart button and add an onClick listener
        mShoppingCartBtn = findViewById(R.id.user_shopping_list);
        mShoppingCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToShoppingCart = new Intent(MainActivity.this, shoppingCart.class);
                startActivity(goToShoppingCart);
            }
        });

        // Initialize the fruit shelf button and add an onClick listener
        mShelfBtn = findViewById(R.id.fruit_shelf);
        mShelfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shelfManagement = new Intent(MainActivity.this, shelfActivity.class);
                shelfManagement.putExtra("documentName", "fruit_shelf");
                shelfManagement.putExtra("fieldName", "fruit_array");
                startActivity(shelfManagement);
            }
        });

        // Initialize the dairy shelf button and add an onClick listener
        mDairyShelfBtn = findViewById(R.id.dairy_shelf);
        mDairyShelfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shelfManagement = new Intent(MainActivity.this, shelfActivity.class);
                shelfManagement.putExtra("documentName", "dairy_shelf");
                shelfManagement.putExtra("fieldName", "dairy_array");
                startActivity(shelfManagement);
            }
        });

        // Initialize the wine shelf button and add an onClick listener
        mWineShelfBtn = findViewById(R.id.wine_shelf);
        mWineShelfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shelfManagement = new Intent(MainActivity.this, shelfActivity.class);
                shelfManagement.putExtra("documentName", "wine_shelf");
                shelfManagement.putExtra("fieldName", "wine_array");
                startActivity(shelfManagement);
            }
        });

        // Initialize the sign out button and add an onClick listener
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

    // Determines whether or not the current user is an Admin
    // If the current user is an Admin, It will display the Admin buttons
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

    // Creates a notification channel in order to display the app notification
    // Mandatory for Android O
    private void createNotifChannel() {
        // Check the version of Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel indoorLocation = new NotificationChannel(CHANNEL_ID , "ForeGroundService", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(indoorLocation);
        }

    }

    private void getShelvesProducts() {
        mFruitArrayList = new ArrayList<>();
        mDairyArrayList = new ArrayList<>();
        mWineArrayList = new ArrayList<>();

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
                            mFruitArrayList.add(distinctValues.get(i).replaceAll("\\s+", ""));
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure( Exception e) {
                Log.d("ERROR", "FAILED TO RETRIEVE THE SHELF DATA");
            }
        });

        DocumentReference mdairyDocumentReference = FirebaseFirestore.getInstance().collection("shelves").document("dairy_shelf");
        mdairyDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                            mDairyArrayList.add(distinctValues.get(i).replaceAll("\\s+", ""));
                        }
                    }
                    //Log.d("arrayStatus", "Status: " + mDairyArrayList);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure( Exception e) {
                Log.d("ERROR", "FAILED TO RETRIEVE THE SHELF DATA");
            }
        });

        DocumentReference mWineDocumentReference = FirebaseFirestore.getInstance().collection("shelves").document("wine_shelf");
        mWineDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                            mWineArrayList.add(distinctValues.get(i).replaceAll("\\s+", ""));
                        }
                    }
                    //Log.d("arrayStatus", "Status: " + mWineArrayList);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure( Exception e) {
                Log.d("ERROR", "FAILED TO RETRIEVE THE SHELF DATA");
            }
        });
    }

    // Function used to search an item in the supermarket
    // It will search the different shelves and will indicate the product position on the map if found
    private void searchFunction() {
        mShelfSearchView = findViewById(R.id.search_view);
        mSearchedItem = String.valueOf(mShelfSearchView.getQuery());
        System.out.println("TEST RESEARCHED ITEM: " + mSearchedItem);
        if(mSearchedItem.isEmpty()) {
            mShelfBtn = findViewById(R.id.fruit_shelf);
            mShelfBtn.setBackgroundColor(Color.LTGRAY);
            mDairyShelfBtn = findViewById(R.id.dairy_shelf);
            mDairyShelfBtn.setBackgroundColor(Color.LTGRAY);
            mWineShelfBtn = findViewById(R.id.wine_shelf);
            mWineShelfBtn.setBackgroundColor(Color.LTGRAY);
        } else {
            // Check if the searched item is in the shelves
            TEMP = mFruitArrayList.contains(mSearchedItem);
            TMP = mDairyArrayList.contains(mSearchedItem);
            TEMPO = mWineArrayList.contains(mSearchedItem);
        }
    }

    private void searchfruitArrayFunction() {
        mFruitFoundArray = new ArrayList<>();
        for (int i = 0; i < mShoppingCartArrayList.size(); i++) {
            fruitFound = mFruitArrayList.contains(mShoppingCartArrayList.get(i));
            if (fruitFound == true) {
                mShelfBtn = findViewById(R.id.fruit_shelf);
                mShelfBtn.setBackgroundColor(Color.GREEN);
                mFruitFoundArray.add(mShoppingCartArrayList.get(i));
                Toast.makeText(MainActivity.this, "Fruit found: " + mFruitFoundArray,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void searchdairyArrayFunction() {
        mDairyFoundArray = new ArrayList<>();
        for (int i = 0; i < mShoppingCartArrayList.size(); i++) {
            dairyFound = mDairyArrayList.contains(mShoppingCartArrayList.get(i));
            if(dairyFound == true) {
                mDairyShelfBtn = findViewById(R.id.dairy_shelf);
                mDairyShelfBtn.setBackgroundColor(Color.GREEN);
                mDairyFoundArray.add(mShoppingCartArrayList.get(i));
                Toast.makeText(MainActivity.this, "Dairy Found: " + mDairyFoundArray,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void searchwineArrayFunction() {
        mWineFoundArray = new ArrayList<>();
        for (int i = 0; i < mShoppingCartArrayList.size(); i++) {
            wineFound = mWineArrayList.contains(mShoppingCartArrayList.get(i));
            if (wineFound == true) {
                mWineShelfBtn = findViewById(R.id.wine_shelf);
                mWineShelfBtn.setBackgroundColor(Color.GREEN);
                mWineFoundArray.add(mShoppingCartArrayList.get(i));
                Toast.makeText(MainActivity.this, "Wine Found: " + mWineFoundArray,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getShoppingListContent() {
        fireBaseUid  = FirebaseAuth.getInstance().getUid();
        mShoppingCartArrayList = new ArrayList<>();

        DocumentReference mDocumentReference = FirebaseFirestore.getInstance()
                .collection("users").document(fireBaseUid)
                .collection("shoppingCart").document("shoppingList");

        mDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {

                    System.out.println("Document data: " + documentSnapshot.getData());
                    Map<String, Object> test = documentSnapshot.getData();
                    ArrayList<String> distinctValue = new ArrayList<String>();

                    for(String key: test.keySet()) {
                        Object value = test.get(key);
                        String values = value.toString();
                        distinctValue = new ArrayList(Arrays.asList(values.replaceAll("[\\[|\\]]", "").trim().split(",")));

                        for (int i = 0; i < distinctValue.size(); i++) {
                            //System.out.println("TEST " + distinctValue.get(i));
                            mShoppingCartArrayList.add(distinctValue.get(i).replaceAll("\\s+", "")); // Regex: removes white spaces
                        }
                    }
                    Log.d("SUCCESS", "Shopping Cart Content " + mShoppingCartArrayList);
                    Log.d("SUCCESS", "USER'S SHOPPING CART CONTENT HAS BEEN RETRIEVED");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure( Exception e) {
                Log.d("ERROR", "FAILED TO RETRIEVE THE SHOPPING LIST DATA");
            }
        });
    }

    private void signOut() {
        mAuth.signOut();
    }

    // Return the distance from your local position to a designated position on the X axis
    private double distanceTo() {
        double mDistanceToX = mTestPos.getX() - mLocalPos.getX();
        return mDistanceToX;
    }

    // Return the distance from your local position to a designated position on the Y axis
    private double distanceToY() {
        double mDistanceToY = mTestPos.getY() - mLocalPos.getY();
        return mDistanceToY;
    }

    // Allows the user to be located and move on the map
    protected void startPositioning() {
        mIndoorLocationManager.startPositioning();
        Toast.makeText(MainActivity.this, "You can now move on the map",
                Toast.LENGTH_SHORT).show();
        Log.d("POSITIONING", "STARTING");
    }

    // Stop the process when you switch to another activity or quit the app
    @Override
    protected void onStop() {
        mIndoorLocationManager.stopPositioning();
        super.onStop();
    }

    // Stop observing the zones
    @Override
    protected void onDestroy() {
        obsHandler.stop();
        super.onDestroy();
    }

    public Boolean getTEMP() {
        return TEMP;
    }

    public Boolean getTMP() {
        return TMP;
    }

    public  Boolean getTEMPO() {
        return TEMPO;
    }
}
