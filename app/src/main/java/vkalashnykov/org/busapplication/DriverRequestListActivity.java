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
import vkalashnykov.org.busapplication.api.domain.Route;

public class DriverRequestListActivity extends AppCompatActivity {
    private ListView requestList;
    private FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
    private DatabaseReference requestsRef;
    private FirebaseListAdapter<Request> requestFirebaseListAdapter;
    String driverKey;
    String currentRouteKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_list);
        requestList=findViewById(R.id.requestList);
        driverKey=getIntent().getStringExtra("DRIVER_KEY");
        requestsRef=firebaseDatabase.getReference().child("requests");
        currentRouteKey=getIntent().getStringExtra("ROUTE_KEY");
        initializeListView();

    }

    private void initializeListView() {
        requestList=findViewById(R.id.requestList);
        setupListView(requestList);

    }

    private void setupListView(ListView requestList) {
        Query requestQuery = requestsRef.orderByChild("routeKey").equalTo(currentRouteKey);
        FirebaseListOptions listOptions = new FirebaseListOptions.Builder<Request>()
                .setLayout(R.layout.request_item)
                .setQuery(requestQuery, Request.class)
                .build();
        requestFirebaseListAdapter = new FirebaseListAdapter<Request>(listOptions) {
            @Override
            protected void populateView(final View v, final Request model, int position) {
                final TextView requestLink = v.findViewById(R.id.requestLink);
                setRequestLinkLabel(model,requestLink);
                setOnClickListenerForLink(v,getRef(position).getKey());
            }

        };
        requestList.setAdapter(requestFirebaseListAdapter);
    }

    private void setRequestLinkLabel(final Request request,
                                     final TextView requestLink) {
        DatabaseReference clientsRef=FirebaseDatabase.getInstance().getReference().child("clients");
        clientsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot clientSnapshot: dataSnapshot.getChildren()){
                    Client client=clientSnapshot.getValue(Client.class);
                    String clientKey=clientSnapshot.getKey();
                    if (clientKey.equals(request.getClientKey())
                            && currentRouteKey!=null
                            && currentRouteKey.equals(request.getRouteKey()) ){
                        requestLink.setText(client.getFirstName()+" "+client.getLastName()+" "
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
