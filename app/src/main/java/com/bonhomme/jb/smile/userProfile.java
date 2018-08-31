/*Vous trouverez les liens vers les licenses nécessaires pour l'utilisation du code dans README.md*/
package com.bonhomme.jb.smile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private Button mDeleteUserBtn;
    private Button mBackToMainBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        mUserFirstNameView = findViewById(R.id.user_firstName);
        mUserLastNameView = findViewById(R.id.user_lastName);
        mUserBirthDayView = findViewById(R.id.user_birthday);
        mUserEmailView = findViewById(R.id.user_email);
        mUserIsAdminView = findViewById(R.id.user_isAdmin);

        mDeleteUserBtn = findViewById(R.id.del_user_account);
        mDeleteUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backToLoginScreen = new Intent(userProfile.this, LoginActivity.class);
                deleteUser();
                finish();
                startActivity(backToLoginScreen);
            }
        });
        mBackToMainBtn = findViewById(R.id.backToMain);
        mBackToMainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backToMain = new Intent(userProfile.this, MainActivity.class);
                finish();
                startActivity(backToMain);
            }
        });
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

    private void deleteUser() {
        fireBaseUid  = FirebaseAuth.getInstance().getUid();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    FirebaseFirestore.getInstance().collection("users").document(fireBaseUid)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("User document", "Deleted");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("User document", "Failed to delete user document");
                                }
                            });
                    Log.d("User account status", "User account has been successfully deleted.");
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("User account status", "Fail to delete User account");
                    }
                });
    }
}
