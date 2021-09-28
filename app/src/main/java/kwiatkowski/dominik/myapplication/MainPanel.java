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
    private DocumentSnapshot userSnap;
    private FirebaseInstance firebaseInstance;

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
        firebaseInstance = FirebaseInstance.getFirebaseInstance();
        // setup button listeners
        View.OnClickListener buttonListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Button b = (Button)v;
                firebaseInstance.sendToDatabase(b.getText().toString(), expenses.getText().toString());
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
                firebaseInstance.sendToDatabase(category.getText().toString(), expenses.getText().toString());
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
}