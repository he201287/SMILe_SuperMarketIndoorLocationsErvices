package com.bonhomme.jb.smile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class anonymousUser extends AppCompatActivity {

    private Button mSignOutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.anonymous_user);

        mSignOutBtn = findViewById(R.id.anonymous_user_sign_out);

        mSignOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.signOut();

                Intent loginScreen = new Intent(anonymousUser.this, MainActivity.class);
                startActivity(loginScreen);
                setContentView(R.layout.activity_main);
            }
        });
    }


}
