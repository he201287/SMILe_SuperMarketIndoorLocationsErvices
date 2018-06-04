/*Vous trouverez les liens vers les licenses nécessaires pour l'utilisation du code dans README.md*/
package com.bonhomme.jb.smile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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

public class registerUser extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button mSignUpBtn;
    private EditText mEmailField;
    private EditText mPswdField;
    private final String TAG = "Email & password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        mAuth = FirebaseAuth.getInstance();

        mEmailField = findViewById(R.id.user_email_field);
        mPswdField = findViewById(R.id.user_pswd_field);

        mSignUpBtn = findViewById(R.id.user_sign_up);
        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userSignUp();
                Intent backToLoginScreen = new Intent(registerUser.this, LoginActivity.class);
                finish();
                startActivity(backToLoginScreen);
            }
        });
    }

    private void userSignUp() {

        String email = mEmailField.getText().toString();
        String pswd = mPswdField.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, pswd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            // successfully Signed in
                            Log.d(TAG, "Create new user: Success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(registerUser.this, "User has been registered successfully.",
                                    Toast.LENGTH_SHORT).show();
                            //TODO update the layout
                            Intent backToLoginScreen = new Intent(registerUser.this, LoginActivity.class);
                            finish();
                            startActivity(backToLoginScreen);
                        } else {
                            // If failure then
                            Log.w(TAG, "Create new user: failure", task.getException());
                            Toast.makeText(registerUser.this, "Sign up failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
