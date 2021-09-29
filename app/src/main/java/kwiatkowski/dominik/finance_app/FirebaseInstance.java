package kwiatkowski.dominik.finance_app;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private ArrayList<String> pendingEmail = new ArrayList<>();
    private ArrayList<String> friendList = new ArrayList<>();

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
                    friendList = (ArrayList<String>) userSnap.get("friendList");

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

    public static void deleteInstance()
    {
        instance = null;
    }

    public void sendDataToDatabase(String name, String sum)
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
        // Create email list including self email
        ArrayList<String> friendAndMyEmail = friendList;
        friendAndMyEmail.add(0,mAuth.getCurrentUser().getEmail());

        for(String email: friendAndMyEmail) {
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
                            ((ExpenditureListActivity) context).appendExpenditureList(data,email);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            });
        }
    }

    // Create listener which will proceed all changes within current user doc. It is important in
    // case of friend list, which can change during using.
    // TODO Add similar listener within expensive list.
    public ListenerRegistration createUserDocListener(Context context)
    {
        ListenerRegistration registration =  userDoc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    ArrayList<String> pendingEmailNew = (ArrayList<String>) snapshot.get("pendingRequest");
                    AddFriendActivity friendActivity = (AddFriendActivity) context;
                    if (pendingEmailNew == null) {
                        friendActivity.clearAddFriendLayout();
                        pendingEmail.clear();
                    } else {
                        for (String email : pendingEmailNew) {
                            if (!pendingEmail.contains(email)) {
                                pendingEmail.add(email);
                                friendActivity.addEmailToFriendLayout(email);
                            }
                            friendActivity.removeDeletedEmails(pendingEmailNew);
                        }
                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
        return  registration;
    }

    // Send all friend request. Note that this function should not be done this way. It should be
    // done by firestore actions to be more secure, but i can't use it for free, so i use this not
    // secure solution.
    public void sendFriendRequest(String email){
        DocumentReference futureFriendDoc = database.collection("users").document(email);
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

                        ArrayList<String> sendRequest =  (ArrayList<String>) userSnap.get("sendRequest");
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
    }

    public void menageFriendData(boolean friendAdd, String friendEmail, Context context)
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
        DocumentReference futureFriendDoc = database.collection("users").document(friendEmail);
        ((AddFriendActivity)context).removeDeletedEmails(pendingEmail);
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

    public String getEmail()
    {
        return mAuth.getCurrentUser().getEmail();
    }

    public void removePendingEmail(String email)
    {
        pendingEmail.remove(email);
    }

    public ArrayList<String> getPendingEmails()
    {
        return this.pendingEmail;
    }
}
