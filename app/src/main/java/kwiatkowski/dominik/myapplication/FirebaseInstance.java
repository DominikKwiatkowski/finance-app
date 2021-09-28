package kwiatkowski.dominik.myapplication;

import android.content.Context;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class FirebaseInstance {
    private static FirebaseInstance instance;
    private FirebaseAuth mAuth;
    private FirebaseFirestore database;
    private DocumentReference userDoc;
    private DocumentReference expenditureDoc = null;
    //TAGs
    private final String TAG = "FIREBASE";
    private DocumentSnapshot userSnap;

    private Integer lastYear = Calendar.getInstance().get(Calendar.YEAR);
    private Integer lastMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
    private ArrayList<String> friendAndMyEmail;
    private FirebaseInstance()
    {
        database = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Get current user document. This document have all necessary data about current user.
        // All user documents are stored in this document collection.
        userDoc = database.collection("users").document(mAuth.getCurrentUser().getEmail());
        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    userSnap = task.getResult();
                    if (userSnap.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + userSnap.getData());
                    } else {
                        Log.d(TAG, "First logging, creating document");
                        //create one
                        Map<String, Object> data = new HashMap<>();
                        userDoc.set(data);
                    }
                    friendAndMyEmail = (ArrayList<String>) userSnap.get("friendList");
                    friendAndMyEmail.add(0,mAuth.getCurrentUser().getEmail());
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public static FirebaseInstance getFirebaseInstance()
    {
        if(instance == null)
        {
            instance = new FirebaseInstance();
        }
        return instance;
    }

    public void sendToDatabase(String name, String sum)
    {
        // Create data
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY).format(new Date());
        // Taking document name, it will by YYYY-MM
        expenditureDoc = userDoc.collection("expenses").document(timeStamp.substring(0,7));
        // Put gathered data to data object
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> nestedData = new HashMap<>();
        nestedData.put("type",name);
        nestedData.put("sum",Integer.parseInt(sum));
        data.put(timeStamp,nestedData);

        expenditureDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    // If document exist, we will update it. Otherwise we will create new document
                    // for this month. Each month have separate document.
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        updateDocument(data);
                    } else {
                        Log.d(TAG, "creating document");
                        data.put("name", timeStamp.substring(0,7));
                        expenditureDoc.set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Document created!");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error creating document", e);
                            }
                        });
                    }

                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    // updates document - it should be moved to firebase Tools...
    public void updateDocument(Map<String, Object> data)
    {
        expenditureDoc.update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "DocumentSnapshot successfully updated!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error updating document", e);
            }
        });
    }

    // This function is responsible for appending next month to the expenditure scroll view.
    // After taking first element of ordered list, value is saved in lastElement. After clicking
    // next month button, app will request for next month. It will only take one get operation per
    // user.
    public void getIElement(Context context)
    {
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
                            // UI update
                            ((ExpenditureList) context).appendExpenditureList(data,email);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            });
        }
    }
}
