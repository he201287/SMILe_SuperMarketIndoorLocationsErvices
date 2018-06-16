/*Vous trouverez les liens vers les licenses nécessaires pour l'utilisation du code dans README.md*/
package com.bonhomme.jb.smile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private final String TAG = "Email & Password login";
    private Button mSignInBtn;
    private Button mSignInWithMailPswdBtn;
    private Button mRegisterBtn;
    private EditText mEmailField;
    private EditText mPswdField;

    //TODO TEXT VIEWS WITH ERROR HANDLING

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mSignInBtn = findViewById(R.id.activity_main_anonymous_sign_in);
        mSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anonymousSignIn();
            }
        });

        mSignInWithMailPswdBtn = findViewById(R.id.activity_main_sign_in_email_pswd);
        mSignInWithMailPswdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userSignIn();
            }
        });

        mRegisterBtn = findViewById(R.id.register_user);
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRegister();
            }
        });


        mEmailField = findViewById(R.id.user_email_field);
        mEmailField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mPswdField = findViewById(R.id.user_pswd_field);
        mPswdField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    private void anonymousSignIn() {
        // start the anonymous sign in
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    // if success
                    Log.d(TAG, "anonymousSignIn:success");
                    FirebaseUser user = mAuth.getCurrentUser();

                    Intent anonymousUser = new Intent(LoginActivity.this, anonymousUser.class);
                    startActivity(anonymousUser);
                } else {
                    // if failure
                    Log.w(TAG, "anonymousSignIn:failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void userSignIn() {

        String email = mEmailField.getText().toString();
        String pswd = mPswdField.getText().toString();

        if (email.isEmpty() && pswd.isEmpty()) {
            return;
        } else {
            Toast.makeText(this, "Loading, please wait...", Toast.LENGTH_SHORT).show();
            mAuth.signInWithEmailAndPassword(email, pswd)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d("Login with email and pswd", "userSignIn() : " + task.isSuccessful());
                    if (!task.isSuccessful()) {
                        Log.d(TAG, "Problem signing in: " + task.getException());
                        Toast.makeText(LoginActivity.this, "ERROR: " + task.getException(), Toast.LENGTH_SHORT).show();
                    } else {
                        Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
                        finish();
                        startActivity(mainActivity);
                    }
                }
            });
        }
    }

    private void userRegister() {
        Intent registerNewUser = new Intent(LoginActivity.this, registerUser.class);
        startActivity(registerNewUser);
    }
}
