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

import java.util.List;

import vkalashnykov.org.busapplication.api.domain.Driver;
import vkalashnykov.org.busapplication.api.domain.Request;
import vkalashnykov.org.busapplication.api.domain.Route;

public class ClientRequestListActivity extends AppCompatActivity {
    private ListView requestList;
    private FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
    private DatabaseReference requestsRef;
    private DatabaseReference clientRef;
    private FirebaseListAdapter<Request> requestFirebaseListAdapter;
    private List<String> driverKeys;
    String clientKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_request_list);
        requestList=findViewById(R.id.requestList);
        clientKey =getIntent().getStringExtra("CLIENT_KEY");
        clientRef=firebaseDatabase.getReference().child("clients").child(clientKey);
        requestsRef =firebaseDatabase.getReference().child("requests");
//        getDriverKeys();
        initializeListView();
    }

    private void getDriverKeys() {
        requestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeListView() {
        requestList=findViewById(R.id.requestList);
        setupListView(requestList);

    }

    private void setupListView(ListView requestList) {
        Query requestQuery = requestsRef.orderByChild("clientKey").equalTo(clientKey);
        FirebaseListOptions listOptions = new FirebaseListOptions.Builder<Request>()
                .setLayout(R.layout.request_item)
                .setQuery(requestQuery, Request.class)
                .build();
        requestFirebaseListAdapter = new FirebaseListAdapter<Request>(listOptions) {
            @Override
            protected void populateView(final View v, Request model, int position) {
                final TextView requestLink = v.findViewById(R.id.requestLink);
                setRequestLinkLabel(model,requestLink);
                setOnClickListenerForLink(v,getRef(position).getKey());

            }

        };
        requestList.setAdapter(requestFirebaseListAdapter);
    }

    private void setRequestLinkLabel(final Request request,
                                     final TextView requestLink) {
        DatabaseReference driversRef=FirebaseDatabase.getInstance().getReference().child("drivers");
        driversRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot driverSnapshot: dataSnapshot.getChildren()){
                    Driver driver=driverSnapshot.getValue(Driver.class);
                    String driverKey=driverSnapshot.getKey();
                    if (driverKey.equals(request.getDriverKey()) && driver.getCurrentRoute()!=null
                    && driver.getCurrentRoute().getRouteKey().equals(request.getRouteKey()) ){
                        requestLink.setText(driver.getFirstName()+" "+driver.getLastName()+" "
                                + request.getCreateDate());
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
                Intent goToRequestDetails=new Intent(ClientRequestListActivity.this
                        ,ClientRequestDetailsActivity.class);
                goToRequestDetails.putExtra("REQUEST_KEY",requestKey);
                goToRequestDetails.putExtra("CLIENT_KEY",clientKey);
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
