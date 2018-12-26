package vkalashnykov.org.busapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import vkalashnykov.org.busapplication.api.domain.Driver;
import vkalashnykov.org.busapplication.layouts.SeekbarWithIntervals;

public class DriverUserDetailsActivity extends AppCompatActivity {

    private String email;
    private String firstNameText,lastNameText,ageText,busSizeText;
    private String driverKey;
    private SeekbarWithIntervals seatsNumberSeekbar;
    private SeekbarWithIntervals trunkCapacitySeekbar;
    private SeekbarWithIntervals salonTrunkSeekbar;
    private SeekbarWithIntervals minimumSeatsSeekbar;
    private EditText firstNameInput;
    private EditText lastNameInput;
    private EditText ageInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_user_details);
        TextView userLogin=(TextView)findViewById(R.id.userLogin);
        email=getIntent().getStringExtra("USER_EMAIL");
        driverKey=getIntent().getStringExtra("DRIVER_KEY");
        userLogin.setText(email);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference=database.getReference();
        DatabaseReference driversRef = reference.child("drivers");
        final TextView firstName=(TextView)findViewById(R.id.userFirstName);
        final TextView lastName=(TextView)findViewById(R.id.userLastName);
        final TextView age=(TextView)findViewById(R.id.userAge);
        final TextView busSize=(TextView)findViewById(R.id.userBusSize);
        final TextView trunkCapacity=(TextView)findViewById(R.id.trunkCapacity);
        final TextView salonCapacity=(TextView)findViewById(R.id.salonCapacity);
        final TextView minSeats=(TextView)findViewById(R.id.minimumSeatsNumber);

        driversRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Driver driver=snapshot.getValue(Driver.class);
                    if (email.equals(driver.getUsername()) ){
                        firstNameText=driver.getFirstName();
                        lastNameText=driver.getLastName();
                        ageText=String.valueOf(driver.getAge());
                        busSizeText=String.valueOf(driver.getBusSize());
                        firstName.setText(firstNameText);
                        lastName.setText(lastNameText);
                        age.setText(ageText);
                        busSize.setText(busSizeText);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DriverUserDetailsActivity.this,R.string.databaseError,
                        Toast.LENGTH_SHORT).show();
                firstNameText="ERROR#";
                lastNameText="ERROR#";
                ageText="ERROR#";
                busSizeText="ERROR#";
            }
        });





    }

    public void setModifyMode(View view) {
        setContentView(R.layout.driver_details_modify);
        firstNameInput=(EditText)findViewById(R.id.firstName);
        lastNameInput=(EditText)findViewById(R.id.lastName);
        ageInput=(EditText)findViewById(R.id.age);
        seatsNumberSeekbar=(SeekbarWithIntervals)findViewById(R.id.seatsNumber);
        trunkCapacitySeekbar=(SeekbarWithIntervals)findViewById(R.id.trunkCapacity);
        salonTrunkSeekbar=(SeekbarWithIntervals)findViewById(R.id.salonCapacity);
        minimumSeatsSeekbar=(SeekbarWithIntervals)findViewById(R.id.minimumSeatsNumber);
        fillIntervals(seatsNumberSeekbar,15);
        fillIntervals(trunkCapacitySeekbar,10);
        fillIntervals(salonTrunkSeekbar,10);
        fillIntervals(minimumSeatsSeekbar,15);
//        seatsNumberSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                updateMinSeatSeekbar(progress);
//
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });



    }

    public void saveDetails(View view) {
    }

    public void fillIntervals(SeekbarWithIntervals seekbar, int maxNumber){
        ArrayList<String> intervals=new ArrayList<>();
        for (int i=1;i<=maxNumber;i++){
            intervals.add(String.valueOf(i));
        }
        seekbar.setIntervals(intervals);
    }

//    public void updateMinSeatSeekbar(int progress){
//        minimumSeatsSeekbar.setUpdate(true);
//        fillIntervals(minimumSeatsSeekbar,progress);
//        minimumSeatsSeekbar.setProgress(progress);
//        minimumSeatsSeekbar.updateThumb();
//        minimumSeatsSeekbar.setUpdate(false);
//
//    }
}
