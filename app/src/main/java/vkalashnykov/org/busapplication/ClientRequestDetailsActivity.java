package vkalashnykov.org.busapplication;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import vkalashnykov.org.busapplication.api.domain.Request;

public class ClientRequestDetailsActivity extends AppCompatActivity {
    private EditText from,to,passengersNumber,trunk,salonTrunk,status,creationDate;
    private FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
    private DatabaseReference requestRef;
    private DatabaseReference clientRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_request_details);
        from=findViewById(R.id.from);
        to=findViewById(R.id.to);
        passengersNumber=findViewById(R.id.passengers);
        trunk=findViewById(R.id.trunk);
        salonTrunk=findViewById(R.id.salonTrunk);
        status=findViewById(R.id.status);
        creationDate=findViewById(R.id.creationDate);
        String requestKey=getIntent().getStringExtra("REQUEST_KEY");
        String client=getIntent().getStringExtra("CLIENT_KEY");
        clientRef=firebaseDatabase.getReference().child("clients").child(client);
        requestRef=firebaseDatabase.getReference().child("requests").child(requestKey);
        fillRequestDetails();
    }
    private void fillRequestDetails() {
        requestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Request request=dataSnapshot.getValue(Request.class);
                from.setText(request.getFrom().toString());
                to.setText(request.getTo().toString());
                passengersNumber.setText(String.valueOf(request.getSeatsNumber()));
                trunk.setText(String.valueOf(request.getTrunk()));
                salonTrunk.setText(String.valueOf(request.getSalonTrunk()));
                status.setText(request.getStatus());
                creationDate.setText(request.getCreateDate());
                LinearLayout requestManagementPanel=findViewById(R.id.requestManagementPanel);
                if (getString(R.string.raised).equals(request.getStatus())){
                    requestManagementPanel.setVisibility(View.VISIBLE);
                } else {
                    requestManagementPanel.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void cancelRequest(View view) {
        requestRef.child("status").setValue(getString(R.string.client_canceled));
    }
}
