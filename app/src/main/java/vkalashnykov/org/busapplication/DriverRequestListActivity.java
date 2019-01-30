package vkalashnykov.org.busapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import vkalashnykov.org.busapplication.api.domain.Client;
import vkalashnykov.org.busapplication.api.domain.Request;

public class DriverRequestListActivity extends AppCompatActivity {
    private ListView requestList;
    private FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
    private DatabaseReference requestsRef;
    private FirebaseListAdapter<String> requestFirebaseListAdapter;
    String driverKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_list);
        requestList=findViewById(R.id.requestList);
        driverKey=getIntent().getStringExtra("DRIVER_KEY");
        requestsRef=firebaseDatabase.getReference().child("drivers").child(driverKey).child("requestIds");
        initializeListView();

    }

    private void initializeListView() {
        requestList=findViewById(R.id.requestList);
        setupListView(requestList);

    }

    private void setupListView(ListView requestList) {
        Query requestQuery = requestsRef.orderByKey();
        FirebaseListOptions listOptions = new FirebaseListOptions.Builder<String>()
                .setLayout(R.layout.request_item)
                .setQuery(requestQuery, String.class)
                .build();
        requestFirebaseListAdapter = new FirebaseListAdapter<String>(listOptions) {
            @Override
            protected void populateView(final View v, String model, int position) {
                final TextView requestLink = v.findViewById(R.id.requestLink);
                DatabaseReference requestRef=firebaseDatabase.getReference().child("requests")
                        .child(model);
                requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Request request=dataSnapshot.getValue(Request.class);
                        String requestId=dataSnapshot.getKey();
                        setRequestLinkLabel(requestId,request.getCreateDate(),requestLink);
                        setOnClickListenerForLink(v,dataSnapshot.getKey());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

        };
        requestList.setAdapter(requestFirebaseListAdapter);
    }

    private void setRequestLinkLabel(final String requestId, final String createDate,
                                     final TextView requestLink) {
        DatabaseReference clientsRef=FirebaseDatabase.getInstance().getReference().child("clients");
        clientsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot clientSnapshot: dataSnapshot.getChildren()){
                    Client client=clientSnapshot.getValue(Client.class);
                    if (client.getRequestIds()!=null && client.getRequestIds().contains(requestId)){
                        requestLink.setText(requestLink.getText()
                                +client.getFirstName()+" "+client.getLastName()+" "
                        + createDate);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setOnClickListenerForLink(View v, final String requestKey) {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToRequestDetails=new Intent(DriverRequestListActivity.this
                        ,DriverRequestDetailsActivity.class);
                goToRequestDetails.putExtra("REQUEST_KEY",requestKey);
                goToRequestDetails.putExtra("DRIVER_KEY",driverKey);
                startActivity(goToRequestDetails);

            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        requestFirebaseListAdapter.startListening();
    }


    @Override
    public void onStop() {
        super.onStop();
        requestFirebaseListAdapter.stopListening();
    }
}
