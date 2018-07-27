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

import vkalashnykov.org.busapplication.api.domain.Client;

public class ClientUserDetailsActivity extends AppCompatActivity {
    private String email;
    private String userKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_user_details);
        TextView userLogin=(TextView)findViewById(R.id.userLogin);
        email=getIntent().getStringExtra("USER_EMAIL");
        userKey=getIntent().getStringExtra("USER_KEY");
        userLogin.setText(email);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference=database.getReference();
        DatabaseReference clientRef = reference.child("clients").child(userKey);
        final TextView firstName=(TextView)findViewById(R.id.userFirstName);
        final TextView lastName=(TextView)findViewById(R.id.userLastName);
        clientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Client client=dataSnapshot.getValue(Client.class);
                firstName.setText(client.getFirstName());
                lastName.setText(client.getLastName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ClientUserDetailsActivity.this,R.string.databaseError,
                        Toast.LENGTH_SHORT).show();
                firstName.setText("#ERROR");
                lastName.setText("#ERROR");
            }
        });

    }
}
