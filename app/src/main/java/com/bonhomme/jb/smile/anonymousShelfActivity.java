package com.bonhomme.jb.smile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class anonymousShelfActivity extends AppCompatActivity {
    private Button mBackToMain;
    private ListView mShelfList;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> mArrayList;
    private String mDocumentName;

    @Override
    protected void onStart(){
        super.onStart();
        getShelfContent();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonymous_shelf);

        mBackToMain = findViewById(R.id.backToMain);
        mBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backToMain = new Intent(anonymousShelfActivity.this, anonymousUser.class);
                finish();
                startActivity(backToMain);
            }});

        // get the extra data passed with the shelves intent to assign the correct documentName
        mDocumentName = getIntent().getStringExtra("documentName");
        //Log.d("SUCCESS", "EXTRA !  " + mDocumentName);

        mShelfList = findViewById(R.id.shelf_list_item);
        mArrayList = new ArrayList<>();
        mAdapter = new ArrayAdapter<>(anonymousShelfActivity.this, android.R.layout.simple_list_item_1, mArrayList);
        mShelfList.setAdapter(mAdapter);
    }

    private void getShelfContent() {
        DocumentReference mDocumentReference = FirebaseFirestore.getInstance().collection("shelves").document(mDocumentName);
        mDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    System.out.println("Document data: " + documentSnapshot.getData());

                    Map<String, Object> test = documentSnapshot.getData();

                    System.out.println("VALUES" + test.values());

                    mShelfList = findViewById(R.id.shelf_list_item);
                    mShelfList.setAdapter(mAdapter);
                    ArrayList<String> distinctValue = new ArrayList<String>();

                    for(String key: test.keySet()) {
                        Object value = test.get(key);
                        String values = value.toString();
                        distinctValue = new ArrayList(Arrays.asList(values.replaceAll("[\\[|\\]]", "").trim().split(",")));
                        for (int i = 0; i < distinctValue.size(); i++) {
                            //System.out.println("TEST " + distinctValue.get(i));
                            mArrayList.add(distinctValue.get(i).replaceAll("\\s+", "")); // Regex: removes white spaces
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                    Log.d("SUCCESS", "SHELF DATA HAS BEEN RETRIEVED");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure( Exception e) {
                Log.d("ERROR", "FAILED TO RETRIEVE THE SHELF DATA");
            }
        });
    }
}
