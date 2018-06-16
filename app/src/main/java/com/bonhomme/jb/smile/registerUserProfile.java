package com.bonhomme.jb.smile;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class registerUserProfile extends AppCompatActivity {

    private static final String FIRST_NAME_KEY = "firstName";
    private static final String LAST_NAME_KEY = "lastName";
    private static final String BIRTH_DAY_KEY = "birthDay";
    private static final String IS_ADMIN_KEY = "isAdmin";
    private static final String DATA_TAG = "Uploading Data to FireBase Cloud Firestore";
    private static final String USER_ID_KEY = "userId";
    private static final String USER_EMAIL_KEY = "userEmail";
    private String fireBaseUid;
    private String fireBaseEmail;

    private Button mSignUpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user_profile);

        mSignUpBtn = findViewById(R.id.user_sign_up);
        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
                //SystemClock.sleep(500);
                Intent backToLoginScreen = new Intent(registerUserProfile.this, LoginActivity.class);
                finish();
                startActivity(backToLoginScreen);
            }
        });
    }


    private void saveUserData() {
        EditText firstNameView = findViewById(R.id.user_firstName);
        EditText lastNameView = findViewById(R.id.user_lastName);
        // TODO CHECK FOR THE DATEPICKER AND FOR A CORRECT DATETIME FORMAT NOT A STRING / CHECK IF THE INPUT IS CORRECT
        EditText bDayView = findViewById(R.id.user_bDay);

        String firstNameText = firstNameView.getText().toString();
        String lastNameText = lastNameView.getText().toString();
        String bDayText = bDayView.getText().toString();
        boolean isAdmin = false;
        fireBaseUid  = FirebaseAuth.getInstance().getUid();
        fireBaseEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        DocumentReference mDocumentReference = FirebaseFirestore.getInstance().collection("users").document(fireBaseUid);

        if(firstNameText.isEmpty() || lastNameText.isEmpty() || bDayText.isEmpty()) {
            return;
        } else {
            Map<String, Object> userData = new HashMap<String, Object>();
            userData.put(USER_ID_KEY, fireBaseUid);
            Log.d("FIREBASE_UID_FETCH_TEST", "FIREBASE_UID    " + fireBaseUid );
            userData.put(USER_EMAIL_KEY, fireBaseEmail);
            Log.d("FIREBASE_USER_EMAIL_FETCH_TEST", "FIREBASE_USER_EMAIL    " + fireBaseEmail );
            userData.put(IS_ADMIN_KEY, isAdmin);
            userData.put(FIRST_NAME_KEY, firstNameText);
            userData.put(LAST_NAME_KEY, lastNameText);
            userData.put(BIRTH_DAY_KEY, bDayText);

            SystemClock.sleep(500);
            mDocumentReference.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(DATA_TAG, "Success ! Your document has been saved");
                    Toast.makeText(registerUserProfile.this, "Your profile has been successfully saved to the cloud !",
                            Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(DATA_TAG, "Error while uploading the document", e);
                }
            });
        }
    }
}
