package kwiatkowski.dominik.finance_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Collections;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class ExpenditureListActivity extends AppCompatActivity {
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

    // Append new bundle of data to scroll view of all expenditures.
    public void appendExpenditureList(Map<String, Object> data, String email)
    {
        SortedSet<String> keys = new TreeSet<String>(Collections.reverseOrder());
        keys.addAll(data.keySet());
        for (String key : keys) {
            // skip name field
            if (key.equals("name"))
                continue;

            // Get bundle of expenditure data, key is timestamp of this data
            Map<String, Object> value = (Map<String, Object>) data.get(key);


            String type = "";
            String sum = "";
            // Get data from data budnle
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
            // Create row
            TableRow tr = new TableRow(this);
            TextView emailView = new TextView(this);
            TextView timestampView = new TextView(this);
            TextView typeView = new TextView(this);
            TextView sumView = new TextView(this);

            // Set row
            emailView.setText(email);
            timestampView.setText(key);
            typeView.setText(type);
            sumView.setText(sum);

            // Add row
            tr.addView(emailView);
            tr.addView(timestampView);
            tr.addView(typeView);
            tr.addView(sumView);
            expenditureTable.addView(tr);
        }
    }
}