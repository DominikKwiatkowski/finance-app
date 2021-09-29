package kwiatkowski.dominik.finance_app;

import android.content.Context;
import android.os.Bundle;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.ListenerRegistration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class AddFriendActivity extends AppCompatActivity {
    private FirebaseInstance firebaseInstance;
    private TextInputEditText friendEmail;
    private ListenerRegistration registration;
    private TableLayout pendingRequestLayout;
    private Context context = this;
    private String TAG = "addFriend";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        firebaseInstance = FirebaseInstance.getFirebaseInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button addFriend = findViewById(R.id.addFriendButton);
        friendEmail = findViewById(R.id.friendEmail);
        pendingRequestLayout = findViewById(R.id.pendingRequestLayout);

        registration = firebaseInstance.createUserDocListener(this);

        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            // Validate email and proceed it within base
            public void onClick(View view) {
                //email regex pattern
                String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
                //Compile regular expression to get the pattern
                Pattern pattern = Pattern.compile(regex);
                //Iterate emails array list
                String email = friendEmail.getText().toString();
                if(email.equals(firebaseInstance.getEmail()) || !pattern.matcher(email).matches())
                {
                    Log.d(TAG, "wrong email");
                    return;
                }
                // If friend already add us, no need to send him friend offer
                if(firebaseInstance.getPendingEmails().contains(email))
                {
                    firebaseInstance.menageFriendData(true, email,context);
                    return;
                }

                //check if user exist in database
                firebaseInstance.sendFriendRequest(email);
            }
        });

        for(String email : firebaseInstance.getPendingEmails())
        {
            addEmailToFriendLayout(email);
        }
    }

    // Add email to scroll view of pending reuest
    public void addEmailToFriendLayout(String email) {

        TableRow tr = new TableRow(this);
        TextView emailView = new TextView(this);
        emailView.setText(email);
        Button acceptButton = new Button(this);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseInstance.menageFriendData(true, email, context);
            }
        });
        acceptButton.setText("Accept");
        Button refuseButton = new Button(this);
        refuseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseInstance.menageFriendData(false, email, context);
            }
        });
        refuseButton.setText("Refuse");

        tr.addView(emailView);
        tr.addView(acceptButton);
        tr.addView(refuseButton);
        pendingRequestLayout.addView(tr);
    }

    // Check all fields and delete all unnecessary.
    void removeDeletedEmails(ArrayList pendingEmailNew)
    {
        for (int i = 0, j = pendingRequestLayout.getChildCount(); i < j; i++)
        {
            View view = pendingRequestLayout.getChildAt(i);
            if (view instanceof TableRow)
            {
                TableRow row = (TableRow) view;
                TextView email = (TextView) row.getChildAt(0);
                if (!pendingEmailNew.contains(email.getText()))
                {
                    firebaseInstance.removePendingEmail((String) email.getText());
                    pendingRequestLayout.removeView(view);
                }
            }
        }
    }

    public void clearAddFriendLayout()
    {
        pendingRequestLayout.removeAllViews();
    }


    @Override
    protected void onStop()
    {
        registration.remove();
        super.onStop();
    }
}