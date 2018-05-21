package com.bonhomme.jb.smile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button mSignOutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

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

    private void signOut() {
        mAuth.signOut();
    }
}
