package kwiatkowski.dominik.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

public class ExpenditureList extends AppCompatActivity {
    //firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore database;
    private Vector<DocumentReference> expenditures = new Vector<DocumentReference>();
    private TableLayout expenditureTable;
    private Button older;
    private final String DOCTAG = "Firebase document";
    private Integer lastYear = Calendar.getInstance().get(Calendar.YEAR);
    private Integer lastMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
    private ArrayList<String> friendAndMyEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenditure_list);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        Intent i = getIntent();
        friendAndMyEmail = (ArrayList<String>) i.getSerializableExtra("friendList");
        expenditureTable = findViewById(R.id.expenditureTable);
        older = findViewById(R.id.older);
        older.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getIElement(false);
            }
        });
        getIElement(true);
    }

    // This function is responsible for appending next month to the expenditure scroll view.
    // After taking first element of ordered list, value is saved in lastElement. After clicking
    // next month button, app will request for next month. It will only take one get operation per
    // user.
    private void getIElement(boolean first)
    {
        Context context = this;
        String fileName = lastYear.toString() + "-" + String.format("%02d", lastMonth);
        lastMonth--;
        if(lastMonth==0)
        {
            lastMonth = 12;
            lastYear--;
        }
        for(String email:friendAndMyEmail) {
            DocumentReference doc = database.
                    collection("users").
                    document(email).
                    collection("expenses").
                    document(fileName);
            doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document= task.getResult();
                        if(document.exists()) {
                            Map<String, Object> data = document.getData();
                            SortedSet<String> keys = new TreeSet<String>(Collections.reverseOrder());
                            keys.addAll(data.keySet());
                            for (String key : keys) {
                                if (key.equals("name"))
                                    continue;
                                Map<String, Object> value = (Map<String, Object>) data.get(key);
                                TableRow tr = new TableRow(context);
                                TextView emailView = new TextView(context);
                                TextView timestampView = new TextView(context);
                                TextView typeView = new TextView(context);
                                TextView sumView = new TextView(context);
                                String type = "";
                                String sum = "";
                                for (Map.Entry<String, Object> contents : value.entrySet()) {
                                    switch (contents.getKey()) {
                                        case "type":
                                            type = contents.getValue().toString();
                                            break;
                                        case "sum":
                                            sum = contents.getValue().toString();
                                            break;
                                    }
                                }
                                emailView.setText(email);
                                timestampView.setText(key);
                                typeView.setText(type);
                                sumView.setText(sum);

                                tr.addView(emailView);
                                tr.addView(timestampView);
                                tr.addView(typeView);
                                tr.addView(sumView);
                                expenditureTable.addView(tr);
                            }
                        }
                    } else {
                        Log.d(DOCTAG, "Error getting documents: ", task.getException());
                    }
                }
            });
        }
    }
}