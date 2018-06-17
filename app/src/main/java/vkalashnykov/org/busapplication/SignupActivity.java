package vkalashnykov.org.busapplication;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vkalashnykov.org.busapplication.domain.Driver;

public class SignupActivity extends AppCompatActivity {

    EditText username;
    EditText password;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String BUS = "BusApplication";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
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

    public void signup(View view) {
        username= (EditText) findViewById(R.id.usernameInput);
        password=(EditText) findViewById(R.id.passwordInput);

        signupUser(username.getText().toString(),password.getText().toString());


    }

    private void signupUser(String email, String password) {
        if (!isOnline()){
            Toast.makeText(this, R.string.networkError,
                    Toast.LENGTH_SHORT).show();
        } else {
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d("User_creation", "createUserWithEmail:onComplete:" + task.isSuccessful());


                            if (!task.isSuccessful()) {
                                Toast.makeText(SignupActivity.this, R.string.user_exists,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference reference=database.getReference();
                                DatabaseReference driversRef = reference.child("drivers");
                                EditText firstName=(EditText)findViewById(R.id.firstNameInput);
                                EditText lastName=(EditText)findViewById(R.id.lastNameInput);
                                EditText age=(EditText)findViewById(R.id.ageInput);
                                EditText busSize=(EditText)findViewById(R.id.busSizeInput);
                                Driver driver=new Driver(username.getText().toString(),
                                        firstName.getText().toString(),
                                        lastName.getText().toString(),
                                        Integer.parseInt(String.valueOf(age.getText())),
                                        Integer.parseInt(String.valueOf(busSize.getText()))
                                );
                                String key=driversRef.push().getKey();
                                driversRef.child(key).setValue(driver);
                                Log.d(BUS,"Successfully write to Database user: "+username.getText().toString());
                                Toast.makeText(SignupActivity.this, R.string.sign_up_success,
                                        Toast.LENGTH_SHORT).show();
                                finish();
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
}
