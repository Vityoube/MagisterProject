package vkalashnykov.org.busapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import vkalashnykov.org.busapplication.domain.Driver;

public class UserDetailsActivity extends AppCompatActivity {

    private String email;
    private String firstNameText,lastNameText,ageText,busSizeText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_user_details);
        TextView userLogin=(TextView)findViewById(R.id.userLogin);
        email=getIntent().getStringExtra("USER_EMAIL");
        userLogin.setText(email);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference=database.getReference();
        DatabaseReference driversRef = reference.child("drivers");
        final TextView firstName=(TextView)findViewById(R.id.userFirstName);
        final TextView lastName=(TextView)findViewById(R.id.userLastName);
        final TextView age=(TextView)findViewById(R.id.userAge);
        final TextView busSize=(TextView)findViewById(R.id.userBusSize);

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
                Toast.makeText(UserDetailsActivity.this,R.string.databaseError, Toast.LENGTH_SHORT);
                firstNameText="ERROR#";
                lastNameText="ERROR#";
                ageText="ERROR#";
                busSizeText="ERROR#";
            }
        });



    }
}
