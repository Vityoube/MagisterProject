package vkalashnykov.org.busapplication;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

import vkalashnykov.org.busapplication.api.domain.BusInformation;
import vkalashnykov.org.busapplication.api.domain.Request;

public class DriverRequestDetailsActivity extends AppCompatActivity {
    private EditText from,to,passengersNumber,trunk,salonTrunk,status,creationDate;
    private FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
    private DatabaseReference requestRef;
    private DatabaseReference driverRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_details);
        from=findViewById(R.id.from);
        to=findViewById(R.id.to);
        passengersNumber=findViewById(R.id.passengers);
        trunk=findViewById(R.id.trunk);
        salonTrunk=findViewById(R.id.salonTrunk);
        status=findViewById(R.id.status);
        creationDate=findViewById(R.id.creationDate);
        String requestKey=getIntent().getStringExtra("REQUEST_KEY");
        String driverKey=getIntent().getStringExtra("DRIVER_KEY");
        driverRef=firebaseDatabase.getReference().child("drivers").child(driverKey);
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

    public void rejectRequest(View view) {
        requestRef.child("status").setValue(getString(R.string.driver_canceled));
    }

    public void acceptRequest(View view) {
        final DatabaseReference driverBusInformationRef=driverRef.child("busInformation");
        driverBusInformationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                BusInformation busInformation=dataSnapshot.getValue(BusInformation.class);
                int passengersValue=Integer.parseInt(passengersNumber.getText().toString());
                int trunkValue=Integer.parseInt(trunk.getText().toString());
                int salonTrunkValue=Integer.parseInt(salonTrunk.getText().toString());
                busInformation.setOccupiedSeats(busInformation.getOccupiedSeats()+passengersValue);
                busInformation.setOccupiedTrunk(busInformation.getOccupiedTrunk()+trunkValue);
                busInformation.setOccupiedSalonTrunk(busInformation.getOccupiedSalonTrunk()
                        +salonTrunkValue);
                requestRef.child("status").setValue(getString(R.string.approved));
                driverBusInformationRef.setValue(busInformation);
                addRequestToRoute();
                Intent returnToDriverMain=new Intent(DriverRequestDetailsActivity.this,
                        DriverMainActivity.class);
                returnToDriverMain.putExtra("USER_KEY",driverRef.getKey());
                returnToDriverMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(returnToDriverMain);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addRequestToRoute() {
        driverRef.child("currentRoute").child("acceptedRequests")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Request> acceptedRequests=(ArrayList<Request>)dataSnapshot.getValue();
                        if (acceptedRequests==null)
                            acceptedRequests=new ArrayList<>();
                        final List<Request> finalAcceptedRequests = (ArrayList<Request>)acceptedRequests;
                        requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Request acceptedRequest=dataSnapshot.getValue(Request.class);
                                finalAcceptedRequests.add(acceptedRequest);
                                driverRef.child("currentRoute").child("acceptedRequests")
                                        .setValue(finalAcceptedRequests);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }
}
