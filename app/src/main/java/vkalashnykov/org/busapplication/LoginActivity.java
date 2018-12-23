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
import vkalashnykov.org.busapplication.api.domain.Route;

public class LoginActivity extends AppCompatActivity {

    EditText email;
    EditText password;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    final FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference driversRef;
    DatabaseReference clientsRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
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
        driversRef=database.getReference().child("drivers");
        clientsRef=database.getReference().child("clients");

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

    public void goToSignUp(View view) {
        Intent intent=new Intent(this,SignupActivity.class);
        intent.putExtra("ROLE","driver");
        startActivity(intent);
    }


    public void login(View view) {
        if (!isOnline()){
            Toast.makeText(this, R.string.networkError,
                    Toast.LENGTH_SHORT).show();
        } else {
            email=(EditText)findViewById(R.id.email);
            password=(EditText)findViewById(R.id.password);

            mAuth.signInWithEmailAndPassword(email.getText().toString(),
                    password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d("Signin_complete", "signInWithEmail:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Log.w("Signin_fail", "signInWithEmail:failed", task.getException());
                                Toast.makeText(LoginActivity.this, R.string.auth_failed,
                                        Toast.LENGTH_SHORT).show();
                            } else{

                                final String userEmail=email.getText().toString();
                                Route route=null;
                                final DatabaseReference routesRef=database.getReference().child("routes");

                                final Intent intent=
                                        new Intent(LoginActivity.this,
                                                DriverMainActivity.class);
                                driversRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                            Driver driver=snapshot.getValue(Driver.class);
                                            if(userEmail.equals(driver.getUsername()) ){

                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                intent.putExtra("USER_EMAIL",email.getText().toString());
                                                intent.putExtra("USER_KEY",snapshot.getKey());
                                                intent.putExtra("NAME",
                                                        driver.getFirstName()+" "+driver.getLastName());
                                                startActivity(intent);
                                                Toast.makeText(LoginActivity.this, R.string.auth_success,
                                                        Toast.LENGTH_SHORT).show();
                                                finish();

                                            }
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Toast.makeText(LoginActivity.this,
                                                R.string.databaseError,Toast.LENGTH_SHORT).show();
                                    }
                                });
                                clientsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot snapshot :dataSnapshot.getChildren()){
                                            Client client=snapshot.getValue(Client.class);
                                            if (userEmail.equals(client.getUsername())){
                                                final Intent intent=
                                                        new Intent(LoginActivity.this,
                                                                ClientMainActivity.class);

                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                intent.putExtra("USER_EMAIL",email.getText().toString());
                                                intent.putExtra("USER_KEY",snapshot.getKey());
                                                intent.putExtra("NAME",
                                                        client.getFirstName()+" "+client.getLastName());

                                                startActivity(intent);
                                                Toast.makeText(LoginActivity.this, R.string.auth_success,
                                                        Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Toast.makeText(LoginActivity.this,
                                                R.string.databaseError,Toast.LENGTH_SHORT).show();
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

    public void goToSignUpClient(View view) {
        Intent intent=new Intent(this,SignupActivity.class);
        intent.putExtra("ROLE","client");
        startActivity(intent);
    }
}
