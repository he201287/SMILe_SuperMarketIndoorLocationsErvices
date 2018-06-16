package com.bonhomme.jb.smile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class registerAnonymousUser extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText mEmailField;
    private EditText mPswdField;
    private final String TAG = "Anonymous User";
    private Button mConvertAccountBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_anonymous_user);

        mAuth = FirebaseAuth.getInstance();

        mConvertAccountBtn = findViewById(R.id.anonymous_user_register);
        mConvertAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertAccount();
            }
        });

        mEmailField = findViewById(R.id.anonymous_user_email_field);
        mPswdField = findViewById(R.id.anonymous_user_pswd_field);
    }

    private void convertAccount() {
        if(!isValid()) {
            return;
        }

        // Fetch the email and password from the input textBoxes
        String email = mEmailField.getText().toString();
        String pswd = mPswdField.getText().toString();

        // Creating the Authentication credentials with mail & password
        AuthCredential credential = EmailAuthProvider.getCredential(email, pswd);

        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "registerAccount:success");
                            FirebaseUser user = task.getResult().getUser();
                            // update layout, pursue with the user registration
                            Intent continueToSignUp = new Intent(registerAnonymousUser.this, registerUserProfile.class);
                            finish();
                            startActivity(continueToSignUp);
                        } else {
                            Log.w(TAG, "registerAccount:failed", task.getException());
                            Toast.makeText(registerAnonymousUser.this, "Authentication failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean isValid() {
        boolean validity = true;

        // Check the validity of the Email String input
        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            validity = false;
        } else {
            mEmailField.setError(null);
        }

        // Check the validity of the password string input
        String pswd = mPswdField.getText().toString();
        if (TextUtils.isEmpty(pswd)) {
            mPswdField.setError("Required.");
            validity = false;
        } else {
            mPswdField.setError(null);
        }

        return validity;
    }
}
