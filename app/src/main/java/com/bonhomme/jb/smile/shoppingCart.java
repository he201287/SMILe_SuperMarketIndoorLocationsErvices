package com.bonhomme.jb.smile;

import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class shoppingCart extends AppCompatActivity {

    private String fireBaseUid;
    private Button mBackToMain;
    private Button mAddItemBtn;
    private Button mClearShoppingListBtn;
    private EditText mListItem;
    private ListView mShoppinhCartListView;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> mArrayList;

    protected void onStart(){
        super.onStart();
        getShoppingListContent();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);


        mBackToMain = findViewById(R.id.backToMain);
        mBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backToMain = new Intent(shoppingCart.this, MainActivity.class);
                saveListContent();
                finish();
                startActivity(backToMain);
            }
        });

        mListItem = findViewById(R.id.list_item);
        mAddItemBtn = findViewById(R.id.add_item);
        mShoppinhCartListView = findViewById(R.id.shopping_cart_item);
        mArrayList = new ArrayList<>();

        mAdapter = new ArrayAdapter<>(shoppingCart.this, android.R.layout.simple_list_item_1, mArrayList);

        mShoppinhCartListView.setAdapter(mAdapter);

        mAddItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mArrayList.add(mListItem.getText().toString());
                mAdapter.notifyDataSetChanged();
            }
        });

        mClearShoppingListBtn = findViewById(R.id.clear_shopping_cart);
        mClearShoppingListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mArrayList.clear();
                mAdapter.notifyDataSetChanged();
            }
        });

    }

    private void getShoppingListContent() {
        fireBaseUid  = FirebaseAuth.getInstance().getUid();
        DocumentReference mDocumentReference = FirebaseFirestore.getInstance()
                .collection("users").document(fireBaseUid)
                .collection("shoppingCart").document("shoppinList");

        mDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {

                    System.out.println("Document data: " + documentSnapshot.getData());

                    Map<String, Object> test = documentSnapshot.getData();

                    mShoppinhCartListView = findViewById(R.id.shopping_cart_item);
                    mShoppinhCartListView.setAdapter(mAdapter);
                    ArrayList<String> distinctValue = new ArrayList<String>();

                    for(String key: test.keySet()) {

                        Object value = test.get(key);

                        String values = value.toString();

                        distinctValue = new ArrayList(Arrays.asList(values.replaceAll("[\\[|\\]]", "").split(",")));

                        for (int i = 0; i < distinctValue.size(); i++) {
                            System.out.println("TEST OIEHGOIEHGIHEGI OIHEOIGIOEGIHOE " + distinctValue.get(i));
                            mArrayList.add(distinctValue.get(i));
                            mAdapter.notifyDataSetChanged();
                        }
                        //String.valueOf(value)



                        System.out.println("Key = " + key + ", Value = " + value);
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

    private void saveListContent() {
        fireBaseUid  = FirebaseAuth.getInstance().getUid();
        DocumentReference mDocumentReference = FirebaseFirestore.getInstance()
                .collection("users").document(fireBaseUid)
                .collection("shoppingCart").document("shoppinList");

        mShoppinhCartListView = findViewById(R.id.shelf_list_item);

        if(mArrayList.isEmpty()) {
            return;
        } else {
            for(int i=0; i < mArrayList.size(); i++) {
                System.out.println("INFO" + mArrayList.get(i));
            }
            System.out.println("INFO" + mArrayList);
            Map<String, Object> shelfData = new HashMap<String, Object>();
            shelfData.put("shoppingCartItem", mArrayList);

            mDocumentReference.set(shelfData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("SUCCESS", "Success ! Your document has been saved");
                    Toast.makeText(shoppingCart.this, "Your shelf has been successfully saved to the cloud !",
                            Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure( Exception e) {
                    Log.d("FAILURE", "Error while uploading the document", e);
                }
            });

        }
    }

}
