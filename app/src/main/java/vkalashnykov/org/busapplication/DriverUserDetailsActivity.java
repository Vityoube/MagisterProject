package vkalashnykov.org.busapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
import vkalashnykov.org.busapplication.layouts.MySeekbar;

public class DriverUserDetailsActivity extends AppCompatActivity {

    private String email;
    private String firstNameText, lastNameText, ageText, busSizeText, trunkCapacityText,
    salonCapacityText,minSeatsText, generalSeatsText;
    private String driverKey;
    private MySeekbar seatsNumberSeekbar;
    private MySeekbar trunkCapacitySeekbar;
    private MySeekbar salonTrunkSeekbar;
    private MySeekbar minimumSeatsSeekbar;
    private EditText firstNameInput;
    private EditText lastNameInput;
    private EditText ageInput;
    DatabaseReference driverRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_user_details);
        TextView userLogin = (TextView) findViewById(R.id.userLogin);
        email = getIntent().getStringExtra("USER_EMAIL");
        driverKey = getIntent().getStringExtra("DRIVER_KEY");
        userLogin.setText(email);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference();
        driverRef = reference.child("drivers").child(driverKey);
        final TextView firstName = (TextView) findViewById(R.id.userFirstName);
        final TextView lastName = (TextView) findViewById(R.id.userLastName);
        final TextView age = (TextView) findViewById(R.id.userAge);
        final TextView busSize = (TextView) findViewById(R.id.userBusSize);
        final TextView trunkCapacity = (TextView) findViewById(R.id.trunkCapacity);
        final TextView salonCapacity = (TextView) findViewById(R.id.salonCapacity);
        final TextView minSeats = (TextView) findViewById(R.id.minimumSeatsNumber);
        final TextView allSeats=(TextView)findViewById(R.id.generalBusSize);

        driverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Driver driver = dataSnapshot.getValue(Driver.class);
                firstNameText = driver.getFirstName();
                lastNameText = driver.getLastName();
                ageText = String.valueOf(driver.getAge());
                busSizeText = String.valueOf(driver.getBusSize());
                trunkCapacityText=String.valueOf(driver.getTrunkCapacity());
                salonCapacityText=String.valueOf(driver.getSalonCapacity());
                minSeatsText=String.valueOf(driver.getMinSeats());
                generalSeatsText=String.valueOf(driver.getFullNumberSeats());


                firstName.setText(firstNameText);
                lastName.setText(lastNameText);
                age.setText(ageText);
                busSize.setText(busSizeText);
                trunkCapacity.setText(trunkCapacityText);
                salonCapacity.setText(salonCapacityText);
                minSeats.setText(minSeatsText);
                allSeats.setText(generalSeatsText);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DriverUserDetailsActivity.this, R.string.databaseError,
                        Toast.LENGTH_SHORT).show();
                firstNameText = "ERROR#";
                lastNameText = "ERROR#";
                ageText = "ERROR#";
                busSizeText = "ERROR#";
            }
        });


    }

    public void setModifyMode(View view) {
        setContentView(R.layout.driver_details_modify);
        firstNameInput = (EditText) findViewById(R.id.firstName);
        lastNameInput = (EditText) findViewById(R.id.lastName);
        ageInput = (EditText) findViewById(R.id.age);
        seatsNumberSeekbar = (MySeekbar) findViewById(R.id.seatsNumber);
        trunkCapacitySeekbar = (MySeekbar) findViewById(R.id.trunkCapacity);
        salonTrunkSeekbar = (MySeekbar) findViewById(R.id.salonCapacity);
        minimumSeatsSeekbar = (MySeekbar) findViewById(R.id.minimumSeatsNumber);
        fillIntervals(seatsNumberSeekbar, 50);
        fillIntervals(trunkCapacitySeekbar, 30);
        fillIntervals(salonTrunkSeekbar, 30);
        fillIntervals(minimumSeatsSeekbar, 1);

    }

    public void saveDetails(View view) {
        if (!"".equals(firstNameInput.getText().toString()))
            driverRef.child("firstName").setValue(firstNameInput.getText().toString());
        if(!"".equals(lastNameInput.getText().toString()))
            driverRef.child("lastName").setValue(lastNameInput.getText().toString());
        if (!"".equals(ageInput.getText().toString()))
            driverRef.child("age").setValue(Integer.parseInt(ageInput.getText().toString()));

        if (seatsNumberSeekbar.getProgress()>0){
            driverRef.child("busSize").setValue(seatsNumberSeekbar.getProgress());
            driverRef.child("fullNumberSeats").setValue(
                    Math.round(seatsNumberSeekbar.getProgress()*1.8));
        }
        if (trunkCapacitySeekbar.getProgress()>0)
        driverRef.child("trunkCapacity").setValue(trunkCapacitySeekbar.getProgress());
        if (salonTrunkSeekbar.getProgress()>0)
        driverRef.child("salonCapacity").setValue(salonTrunkSeekbar.getProgress());
        if (minimumSeatsSeekbar.getProgress()>0
                && minimumSeatsSeekbar.getProgress()<=seatsNumberSeekbar.getProgress())
            driverRef.child("minSeats").setValue(minimumSeatsSeekbar.getProgress());
        setContentView(R.layout.activity_driver_user_details);
        recreate();
    }

    public void fillIntervals(MySeekbar seekbar, int maxNumber) {
        ArrayList<String> intervals = new ArrayList<>();
        for (int i = 1; i <= maxNumber; i++) {
            intervals.add(String.valueOf(i));
        }
        seekbar.setMax(intervals);
    }

    public void cancel(View view) {
        setContentView(R.layout.activity_driver_user_details);
    }

}
