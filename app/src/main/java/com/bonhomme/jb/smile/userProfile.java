package com.bonhomme.jb.smile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class userProfile extends AppCompatActivity {

    private static final String FIRST_NAME_KEY = "firstName";
    private static final String LAST_NAME_KEY = "lastName";
    private static final String BIRTH_DATE_KEY = "birthDate";
    private static final String IS_ADMIN_KEY = "isAdmin";
    private static final String USER_EMAIL_KEY = "userEmail";
    private DocumentReference mDocumentReference = FirebaseFirestore.getInstance().collection("users").document("5mSUT45IYnHaXuKRJGVk");
    TextView mUserDataTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        mUserDataTextView = findViewById(R.id.user_profile_info);
    }

    protected void onStart(){
        super.onStart();
        getUserProfile();
    };

    private void getUserProfile() {
        mDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    String firstName = documentSnapshot.getString(FIRST_NAME_KEY);
                    String lastName = documentSnapshot.getString(LAST_NAME_KEY);
                    String birthDate = documentSnapshot.getString(BIRTH_DATE_KEY);
                    String userEmail = documentSnapshot.getString(USER_EMAIL_KEY);
                    boolean isAdmin = documentSnapshot.getBoolean(IS_ADMIN_KEY);

                    mUserDataTextView.setText("\"" + firstName + "\n" + lastName + "\n" + birthDate + "\n" + userEmail + "\n" + isAdmin);
                    Log.d("SUCCESS", "USER DATA HAS BEEN RETRIEVED");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("ERROR", "FAILED TO RETRIEVE THE USER DATA");
            }
        });
    }
}
