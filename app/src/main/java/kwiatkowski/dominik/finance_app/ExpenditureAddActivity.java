package kwiatkowski.dominik.finance_app;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class ExpenditureAddActivity extends AppCompatActivity {
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
                firebaseInstance.sendDataToDatabase(b.getText().toString(), expenses.getText().toString());
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
                firebaseInstance.sendDataToDatabase(category.getText().toString(), expenses.getText().toString());
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

    // Function is responsible for functions when some menu item is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent i;
        switch (item.getItemId())
        {
            case R.id.signout:
                mAuth.signOut();
                FirebaseInstance.deleteInstance();
                this.finish();
                break;
            case R.id.expenditures:
                i = new Intent(getApplicationContext(), ExpenditureListActivity.class);
                startActivity(i);
                break;
            case R.id.addFriend:
                i = new Intent(getApplicationContext(), AddFriendActivity.class);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}