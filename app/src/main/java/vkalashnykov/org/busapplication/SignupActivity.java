package vkalashnykov.org.busapplication;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import vkalashnykov.org.busapplication.api.domain.BusInformation;
import vkalashnykov.org.busapplication.api.domain.Client;
import vkalashnykov.org.busapplication.api.domain.Driver;
import vkalashnykov.org.busapplication.components.MySeekbar;

public class SignupActivity extends AppCompatActivity {

    EditText username;
    EditText password;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String BUS = "BusApplication";
    private MySeekbar seatsNumberSeekbar;
    private MySeekbar trunkCapacitySeekbar;
    private MySeekbar salonTrunkSeekbar;
    private MySeekbar minimumSeatsSeekbar;
    private EditText confirmPassword;
    private String usernameText, passwordText, firstNameText, lastNameText;
    private int ageValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String role = getIntent().getStringExtra("ROLE");
        if ("driver".equals(role)) {
            setContentView(R.layout.activity_driver_signup);


        } else if ("client".equals(role))
            setContentView(R.layout.activity_client_signup);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("Logged_in", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("Logged_out", "onAuthStateChanged:signed_out");
                }
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void signupDriver(View view) {
        int seatsNumber =
                seatsNumberSeekbar.getProgress();
        int trunkCapacity
                = trunkCapacitySeekbar.getProgress();
        int salonTrunk = salonTrunkSeekbar.getProgress();
        int fullSeatsNumber = (int) Math.round(seatsNumber * 1.8);
        int minSeats = minimumSeatsSeekbar.getProgress();

        signupDriver(usernameText, passwordText, firstNameText, lastNameText, ageValue, seatsNumber,
                fullSeatsNumber, trunkCapacity, salonTrunk, minSeats);


    }

    private void signupDriver(final String email, final String passwordText, final String firstNameText,
                              final String lastNameText, final int ageValue, final int seatsNumber,
                              final int fullSeatsNumber, final int trunkCapacity, final int salonTrunk,
                              final int minSeats) {
        if (!isOnline()) {
            Toast.makeText(this, R.string.networkError,
                    Toast.LENGTH_SHORT).show();
        } else {
            mAuth.createUserWithEmailAndPassword(email, passwordText)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d("User_creation", "createUserWithEmail:onComplete:" + task.isSuccessful());


                            if (!task.isSuccessful()) {
                                Toast.makeText(SignupActivity.this, R.string.user_exists,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                mAuth.signInWithEmailAndPassword(email, passwordText)
                                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {

                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                DatabaseReference reference = database.getReference();
                                                DatabaseReference driversRef = reference.child("drivers");

                                                Driver driver = new Driver(username.getText().toString(),
                                                        firstNameText,
                                                        lastNameText,
                                                        ageValue
                                                );
                                                BusInformation busInformation=
                                                        new BusInformation(
                                                                seatsNumber,
                                                                trunkCapacity,
                                                                salonTrunk,
                                                                minSeats,
                                                                fullSeatsNumber);
                                                driver.setBusInformation(busInformation);

                                                String key = driversRef.push().getKey();
                                                driversRef.child(key).setValue(driver);
                                                Log.d(BUS, "Successfully write to Database user: " + username.getText().toString());
                                                Toast.makeText(SignupActivity.this, R.string.sign_up_success,
                                                        Toast.LENGTH_SHORT).show();
                                                final Intent intent = new Intent(SignupActivity.this, DriverMainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                intent.putExtra("USER_EMAIL", email);
                                                driversRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                            Driver driver = snapshot.getValue(Driver.class);
                                                            if (email.equals(driver.getUsername())) {
                                                                intent.putExtra("USER_KEY", snapshot.getKey());
                                                                intent.putExtra("NAME",
                                                                        driver.getFirstName() + " " + driver.getLastName());
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {
                                                        Toast.makeText(SignupActivity.this, R.string.databaseError, Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                            }

                                        });
                            }

                        }
                    });
        }


    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void signupClient(View view) {
        username = (EditText) findViewById(R.id.usernameInput);
        password = (EditText) findViewById(R.id.passwordInput);
        String confirmPassword = ((EditText) findViewById(R.id.passwordInput3)).getText().toString();
        String firstName = ((EditText) findViewById(R.id.firstNameInput)).getText().toString();
        String lastname = ((EditText) findViewById(R.id.lastNameInput)).getText().toString();
        if (!confirmPassword.equals(password.getText().toString())) {
            Toast.makeText(this, R.string.passwordsNotMatch, Toast.LENGTH_SHORT).show();
        } else {
            signupClient(username.getText().toString(), password.getText().toString(), firstName, lastname);
        }
    }

    private void signupClient(final String email, final String password,
                              final String firstName, final String lastname) {
        if (!isOnline()) {
            Toast.makeText(this, R.string.networkError,
                    Toast.LENGTH_SHORT).show();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d("User_creation", "createUserWithEmail:onComplete:" + task.isSuccessful());


                            if (!task.isSuccessful()) {
                                Toast.makeText(SignupActivity.this, R.string.user_exists,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                mAuth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {

                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                DatabaseReference reference = database.getReference();
                                                DatabaseReference clientsRef = reference.child("clients");

                                                Client client = new Client(email,
                                                        firstName,
                                                        lastname
                                                );
                                                String key = clientsRef.push().getKey();
                                                clientsRef.child(key).setValue(client);
                                                Log.d(BUS, "Successfully write to Database user: " + email);
                                                Toast.makeText(SignupActivity.this, R.string.sign_up_success,
                                                        Toast.LENGTH_SHORT).show();
                                                final Intent intent = new Intent(SignupActivity.this, ClientMainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                intent.putExtra("USER_EMAIL", email);
                                                clientsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                            Client client = snapshot.getValue(Client.class);
                                                            if (email.equals(client.getUsername())) {
                                                                intent.putExtra("USER_KEY", snapshot.getKey());
                                                                intent.putExtra("NAME",
                                                                        client.getFirstName() + " " + client.getLastName());
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {
                                                        Toast.makeText(SignupActivity.this, R.string.databaseError, Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                            }

                                        });
                            }

                        }
                    });
        }
    }

    public void fillIntervals(MySeekbar seekbar, int maxNumber) {
        ArrayList<String> intervals = new ArrayList<>();
        for (int i = 1; i <= maxNumber; i++) {
            intervals.add(String.valueOf(i));
        }
        seekbar.setMax(intervals);
    }

    public void goToBusDetails(View view) {
        EditText firstName = (EditText) findViewById(R.id.firstNameInput);
        EditText lastName = (EditText) findViewById(R.id.lastNameInput);
        EditText age = (EditText) findViewById(R.id.ageInput);
        username = (EditText) findViewById(R.id.usernameInput);
        password = (EditText) findViewById(R.id.passwordInput);
        confirmPassword = ((EditText) findViewById(R.id.passwordInput2));
        String confirmPasswordText = confirmPassword.getText().toString();
        if (!confirmPasswordText.equals(password.getText().toString())) {
            Toast.makeText(this, R.string.passwordsNotMatch, Toast.LENGTH_SHORT).show();
        } else {
            usernameText = username.getText().toString();
            passwordText = password.getText().toString();
            firstNameText = firstName.getText().toString();
            lastNameText = lastName.getText().toString();
            ageValue = Integer.parseInt(age.getText().toString());
            setContentView(R.layout.driver_bus_details_signup);
            seatsNumberSeekbar = (MySeekbar) findViewById(R.id.seatsNumber);
            trunkCapacitySeekbar = (MySeekbar) findViewById(R.id.trunkCapacity);
            salonTrunkSeekbar = (MySeekbar) findViewById(R.id.salonCapacity);
            minimumSeatsSeekbar = (MySeekbar) findViewById(R.id.minimumSeatsNumber);
            fillIntervals(seatsNumberSeekbar, 50);
            fillIntervals(trunkCapacitySeekbar, 30);
            fillIntervals(salonTrunkSeekbar, 30);
            fillIntervals(minimumSeatsSeekbar, 1);
        }
    }
}
