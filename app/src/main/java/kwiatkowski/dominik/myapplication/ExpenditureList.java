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
    private FirebaseInstance firebaseInstance;
    private TableLayout expenditureTable;
    private Button older;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenditure_list);
        Context context = this;
        firebaseInstance = FirebaseInstance.getFirebaseInstance();
        expenditureTable = findViewById(R.id.expenditureTable);
        older = findViewById(R.id.older);
        older.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseInstance.getIElement(context);
            }
        });
        firebaseInstance.getIElement(context);
    }

    public void appendExpenditureList(Map<String, Object> data, String email)
    {
        SortedSet<String> keys = new TreeSet<String>(Collections.reverseOrder());
        keys.addAll(data.keySet());
        for (String key : keys) {
            if (key.equals("name"))
                continue;
            Map<String, Object> value = (Map<String, Object>) data.get(key);
            TableRow tr = new TableRow(this);
            TextView emailView = new TextView(this);
            TextView timestampView = new TextView(this);
            TextView typeView = new TextView(this);
            TextView sumView = new TextView(this);
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
}