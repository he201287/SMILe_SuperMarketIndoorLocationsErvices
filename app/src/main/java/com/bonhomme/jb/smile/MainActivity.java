package com.bonhomme.jb.smile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static FirebaseAuth mAuth;
    private final String TAG = "EmailPassword";
    private Button mSignInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mSignInBtn = findViewById(R.id.activity_main_anonymous_sign_in);

        mSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anonymousSignIn();
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
                    // setContentView(R.layout.anonymous_user);
                    Intent anonymousUser = new Intent(MainActivity.this, anonymousUser.class);
                    startActivity(anonymousUser);
                } else {
                    // if failure
                    Log.w(TAG, "anonymousSignIn:failure", task.getException());
                    Toast.makeText(MainActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected static void signOut() {
        mAuth.signOut();
    }
}
