/*Vous trouverez les liens vers les licenses nécessaires pour l'utilisation du code dans README.md*/
package com.bonhomme.jb.smile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class anonymousUser extends AppCompatActivity {

    private FirebaseAuth mAuth;


    private Button mSignOutBtn;
    private Button mRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.anonymous_user);

        mAuth = FirebaseAuth.getInstance();

        mSignOutBtn = findViewById(R.id.anonymous_user_sign_out);
        mSignOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
                // Intent loginScreen = new Intent(anonymousUser.this, LoginActivity.class);
                // startActivity(loginScreen);
                finish();
            }
        });

        mRegister = findViewById(R.id.anonymous_user_register);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerAnonymousUser = new Intent(anonymousUser.this, registerAnonymousUser.class);
                startActivity(registerAnonymousUser);
            }
        });
    }

    // TODO: Add view for the anonymous user

    private void signOut() {
        mAuth.signOut();
    }
}
