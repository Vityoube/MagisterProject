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

import vkalashnykov.org.busapplication.api.domain.Client;
import vkalashnykov.org.busapplication.api.domain.Driver;

public class SignupActivity extends AppCompatActivity {

    EditText username;
    EditText password;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String BUS = "BusApplication";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String role=getIntent().getStringExtra("ROLE");
        if("driver".equals(role))
            setContentView(R.layout.activity_driver_signup);
        else if ("client".equals(role))
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
        username= (EditText) findViewById(R.id.usernameInput);
        password=(EditText) findViewById(R.id.passwordInput);

        signupDriver(username.getText().toString(),password.getText().toString());


    }

    private void signupDriver(final String email, final String password) {
        if (!isOnline()) {
            Toast.makeText(this, R.string.networkError,
                    Toast.LENGTH_SHORT).show();
        } else {
            String confirmPassword=((EditText)findViewById(R.id.passwordInput2)).getText().toString();
            if(!confirmPassword.equals(password)){
                Toast.makeText(this, R.string.passwordsNotMatch, Toast.LENGTH_SHORT).show();
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
                                                    DatabaseReference driversRef = reference.child("drivers");
                                                    EditText firstName = (EditText) findViewById(R.id.firstNameInput);
                                                    EditText lastName = (EditText) findViewById(R.id.lastNameInput);
                                                    EditText age = (EditText) findViewById(R.id.ageInput);
                                                    EditText busSize = (EditText) findViewById(R.id.busSizeInput);
                                                    Driver driver = new Driver(username.getText().toString(),
                                                            firstName.getText().toString(),
                                                            lastName.getText().toString(),
                                                            Integer.parseInt(String.valueOf(age.getText())),
                                                            Integer.parseInt(String.valueOf(busSize.getText()))
                                                    );
                                                    String key = driversRef.push().getKey();
                                                    driversRef.child(key).setValue(driver);
                                                    Log.d(BUS, "Successfully write to Database user: " + username.getText().toString());
                                                    Toast.makeText(SignupActivity.this, R.string.sign_up_success,
                                                            Toast.LENGTH_SHORT).show();
                                                    final Intent intent = new Intent(SignupActivity.this, MainActivity.class);
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
                                                                            driver.getFirstName()+" "+driver.getLastName());
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
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void signupClient(View view) {
        username= (EditText) findViewById(R.id.usernameInput);
        password=(EditText) findViewById(R.id.passwordInput);
        String confirmPassword=((EditText)findViewById(R.id.passwordInput3)).getText().toString();
        String firstName=((EditText)findViewById(R.id.firstNameInput)).getText().toString();
        String lastname=((EditText)findViewById(R.id.lastNameInput)).getText().toString();
        if(!confirmPassword.equals(password.getText().toString())){
            Toast.makeText(this, R.string.passwordsNotMatch, Toast.LENGTH_SHORT).show();
        } else {
            signupClient(username.getText().toString(),password.getText().toString(),firstName,lastname);
        }
    }

    private void signupClient(final String email, final String password,
                              final String firstName, final String lastname) {
        if(!isOnline()){
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
                                                                        client.getFirstName()+" "+client.getLastName());
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
}
