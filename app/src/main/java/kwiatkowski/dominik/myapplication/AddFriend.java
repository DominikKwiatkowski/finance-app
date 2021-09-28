package kwiatkowski.dominik.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class AddFriend extends AppCompatActivity {

    private FirebaseFirestore database;
    private FirebaseAuth mAuth;
    private DocumentReference futureFriendDoc;
    private DocumentReference userDoc;
    private TextInputEditText friendEmail;
    private ArrayList<String> pendingEmail = new ArrayList<>();
    private ArrayList<String> friendList = new ArrayList<>();
    private Context context;
    private ListenerRegistration registration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        database = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        Button addFriend = findViewById(R.id.addFriendButton);
        friendEmail = findViewById(R.id.friendEmail);
        String TAG = "addFriend";
        context = this;
        userDoc = database.collection("users").document(mAuth.getCurrentUser().getEmail());
        TableLayout pendingRequestLayout = findViewById(R.id.pendingRequestLayout);
        registration = userDoc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    ArrayList<String> pendingEmailNew = (ArrayList<String>) snapshot.get("pendingRequest");
                    if(pendingEmailNew == null)
                    {
                        pendingRequestLayout.removeAllViews();
                        pendingEmail.clear();
                    }
                    else {
                        for (String email : pendingEmailNew) {
                            if (!pendingEmail.contains(email)) {
                                pendingEmail.add(email);
                                TableRow tr = new TableRow(context);
                                TextView emailView = new TextView(context);
                                emailView.setText(email);
                                Button acceptButton = new Button(context);
                                acceptButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        menageFriendData(true, email);
                                    }
                                });
                                acceptButton.setText("Accept");
                                Button refuseButton = new Button(context);
                                refuseButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        menageFriendData(false, email);
                                    }
                                });
                                refuseButton.setText("Refuse");

                                tr.addView(emailView);
                                tr.addView(acceptButton);
                                tr.addView(refuseButton);
                                pendingRequestLayout.addView(tr);
                            }
                        }
                        for (int i = 0, j = pendingRequestLayout.getChildCount(); i < j; i++) {
                            View view = pendingRequestLayout.getChildAt(i);
                            if (view instanceof TableRow) {
                                TableRow row = (TableRow) view;
                                TextView email = (TextView) row.getChildAt(0);
                                if (!pendingEmailNew.contains(email.getText())) {
                                    pendingEmail.remove(email.getText());
                                    pendingRequestLayout.removeView(view);
                                }
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });


        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //email regex pattern
                String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
                //Compile regular expression to get the pattern
                Pattern pattern = Pattern.compile(regex);
                //Iterate emails array list
                String email = friendEmail.getText().toString();
                if(email == mAuth.getCurrentUser().getEmail() || !pattern.matcher(email).matches())
                {
                    Log.d(TAG, "wrong email");
                    return;
                }
                // If friend already add us, no need to send him friend offer
                if(pendingEmail.contains(email))
                {
                    menageFriendData(true, email);
                    return;
                }
                //check if user exist in database
                futureFriendDoc = database.collection("users").document(email);
                futureFriendDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            DocumentSnapshot document = task.getResult();
                            if(document.exists())
                            {
                                ArrayList<String> requests = (ArrayList<String>) document.get("pendingRequest");
                                if(requests == null)
                                    requests = new ArrayList<String>();
                                requests.add(mAuth.getCurrentUser().getEmail());
                                futureFriendDoc.update("pendingRequest", (List<String>)requests);

                                Intent intent = getIntent();
                                ArrayList<String> sendRequest = (ArrayList<String>) intent.getSerializableExtra("sendRequest");
                                if(sendRequest == null)
                                    sendRequest = new ArrayList<String>();
                                sendRequest.add(email);
                                userDoc.update("sendRequest",(List<String>)sendRequest);
                            }
                            else
                            {
                                Log.d(TAG, "wrong email");
                                // pop friedn does not exist message.
                            }
                        }
                    }
                });
                // if exist, append my email to his panding friend
                // add him to offer send
            }
        });
    }

    private void menageFriendData(boolean friendAdd, String friendEmail)
    {
        pendingEmail.remove(friendEmail);
        HashMap<String,Object> data = new HashMap<>();
        data.put("pendingRequest", (List<String>)pendingEmail);
        if(friendAdd)
        {
            friendList.add(friendEmail);
            data.put("friendList", friendList);
        }
        userDoc.update(data);
        futureFriendDoc = database.collection("users").document(friendEmail);
        futureFriendDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    HashMap<String,Object> data = new HashMap<>();
                    data.put("pendingRequest", (List<String>)pendingEmail);

                    List<String> sendRequest = (List<String>) document.get("sendRequest");
                    sendRequest.remove(mAuth.getCurrentUser().getEmail());
                    data.put("sendRequest", sendRequest);
                    if(friendAdd)
                    {
                        List<String> friendListOfFriend = (List<String>) document.get("friendList");
                        friendListOfFriend.add(mAuth.getCurrentUser().getEmail());
                        data.put("friendList", friendListOfFriend);
                    }
                    futureFriendDoc.update(data);
                }

            }
        });
    }

    @Override
    protected void onStop()
    {
        registration.remove();
        super.onStop();
    }
}