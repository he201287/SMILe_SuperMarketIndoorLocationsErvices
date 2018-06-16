package com.bonhomme.jb.smile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

public class userProfile extends AppCompatActivity {

    private static final String FIRST_NAME_KEY = "firstName";
    private static final String LAST_NAME_KEY = "lastName";
    private static final String BIRTH_DAY_KEY = "birthDay";
    private static final String IS_ADMIN_KEY = "isAdmin";
    private static final String USER_EMAIL_KEY = "userEmail";
    private String fireBaseUid;
    TextView mUserFirstNameView;
    TextView mUserLastNameView;
    TextView mUserBirthDayView;
    TextView mUserEmailView;
    TextView mUserIsAdminView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        mUserFirstNameView = findViewById(R.id.user_firstName);
        mUserLastNameView = findViewById(R.id.user_lastName);
        mUserBirthDayView = findViewById(R.id.user_birthday);
        mUserEmailView = findViewById(R.id.user_email);
        mUserIsAdminView = findViewById(R.id.user_isAdmin);
    }

    protected void onStart(){
        super.onStart();
        getUserProfile();
    };

    private void getUserProfile() {
        fireBaseUid  = FirebaseAuth.getInstance().getUid();
        DocumentReference mDocumentReference = FirebaseFirestore.getInstance().collection("users").document(fireBaseUid);
        mDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    String firstName = documentSnapshot.getString(FIRST_NAME_KEY);
                    String lastName = documentSnapshot.getString(LAST_NAME_KEY);
                    String birthDay = documentSnapshot.getString(BIRTH_DAY_KEY);
                    String userEmail = documentSnapshot.getString(USER_EMAIL_KEY);
                    boolean isAdmin = documentSnapshot.getBoolean(IS_ADMIN_KEY);

                    mUserFirstNameView.setText("First name: "  + firstName);
                    mUserLastNameView.setText("Last name: " + lastName);
                    mUserBirthDayView.setText("Birthday: " + birthDay);
                    mUserEmailView.setText("Email: " + userEmail);
                    mUserIsAdminView.setText("Admin state: " + isAdmin);
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
