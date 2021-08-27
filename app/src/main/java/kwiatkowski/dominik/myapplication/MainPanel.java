package kwiatkowski.dominik.myapplication;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainPanel extends AppCompatActivity {
    //firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore database;
    // UI instances
    private Button food;
    private Button clouth;
    private Button culture;
    private Button vacation;
    private Button house;
    private Button hobby;
    private Button others;
    private Button transport;
    private Button work;
    private TextInputEditText category;
    private TextInputEditText expenses;
    private DocumentReference userDoc;
    private DocumentReference expenditureDoc = null;
    //TAGs
    private final String userDataTag = "USERDATATAG";
    private final String expenditureTag = "EXPENDTAG";
    private DocumentSnapshot userSnap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get all elements from UI
        setContentView(R.layout.activity_main_panel);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        food = findViewById(R.id.food);
        clouth = findViewById(R.id.clouth);
        culture = findViewById(R.id.culture);
        vacation = findViewById(R.id.vacation);
        house = findViewById(R.id.house);
        hobby = findViewById(R.id.hobby);
        others = findViewById(R.id.others);
        category = findViewById(R.id.category);
        expenses = findViewById(R.id.expenses);
        transport = findViewById(R.id.transport);
        work = findViewById(R.id.work);

        // get Firebase instances
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

        // setup button listeners
        View.OnClickListener buttonListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Button b = (Button)v;
                sendToDatabase(b.getText().toString());
            }
        };
        food.setOnClickListener(buttonListener);
        clouth.setOnClickListener(buttonListener);
        culture.setOnClickListener(buttonListener);
        vacation.setOnClickListener(buttonListener);
        house.setOnClickListener(buttonListener);
        hobby.setOnClickListener(buttonListener);
        transport.setOnClickListener(buttonListener);
        work.setOnClickListener(buttonListener);
        others.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToDatabase(category.getText().toString());
            }
        });

        // Get current user document. This document have all necessary data about current user.
        // All user documents are stored in this document collection.
        userDoc = database.collection("users").document(mAuth.getCurrentUser().getEmail());
        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    userSnap = task.getResult();
                    if (userSnap.exists()) {
                        Log.d(userDataTag, "DocumentSnapshot data: " + userSnap.getData());
                    } else {
                        Log.d(userDataTag, "First logging, creating document");
                        //create one
                        Map<String, Object> data = new HashMap<>();
                        userDoc.set(data);
                    }
                } else {
                    Log.d(userDataTag, "get failed with ", task.getException());
                }
            }
        });
    }

    // creates menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Function is responsible for
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent i;
        switch (item.getItemId())
        {
            case R.id.signout:
                mAuth.signOut();
                this.finish();
                break;
            case R.id.expenditures:
                i = new Intent(getApplicationContext(), ExpenditureList.class);
                ArrayList<String> friendList = (ArrayList<String>) userSnap.get("friendList");
                friendList.add(0,mAuth.getCurrentUser().getEmail());
                i.putExtra("friendList", friendList);
                startActivity(i);
                break;
            case R.id.addFriend:
                i = new Intent(getApplicationContext(), AddFriend.class);
                ArrayList<String> pendingRequest = (ArrayList<String>) userSnap.get("sendRequest");
                i.putExtra("sendRequest", pendingRequest);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // updates document - it should be moved to firebase Tools...
    private void updateDocument(Map<String, Object> data)
    {
        expenditureDoc.update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(expenditureTag, "DocumentSnapshot successfully updated!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(expenditureTag, "Error updating document", e);
            }
        });
    }

    // Send data to database
    private void sendToDatabase(String name)
    {
        // Create data
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY).format(new Date());
        // Taking document name, it will by YYYY-MM
        expenditureDoc = userDoc.collection("expenses").document(timeStamp.substring(0,7));
        // Put gathered data to data object
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> nestedData = new HashMap<>();
        nestedData.put("type",name);
        nestedData.put("sum",Integer.parseInt(expenses.getText().toString()));
        data.put(timeStamp,nestedData);

        expenditureDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    // If document exist, we will update it. Otherwise we will create new document
                    // for this month. Each month have separate document.
                    if (document.exists()) {
                        Log.d(userDataTag, "DocumentSnapshot data: " + document.getData());
                        updateDocument(data);
                    } else {
                        Log.d(expenditureTag, "creating document");
                        data.put("name", timeStamp.substring(0,7));
                        expenditureDoc.set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(expenditureTag, "Document created!");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(expenditureTag, "Error creating document", e);
                            }
                        });
                    }

                } else {
                    Log.d(expenditureTag, "get failed with ", task.getException());
                }
            }
        });
    }
}