package vkalashnykov.org.busapplication.components;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import vkalashnykov.org.busapplication.ClientMainActivity;
import vkalashnykov.org.busapplication.R;

public class CreateRequestDialog extends Dialog implements View.OnClickListener {

    private ClientMainActivity activity;
    private  Dialog dialog;
    private Button cancelButton, submitButton;
    private EditText numberSeats, trunk, salonTrunk;

    public CreateRequestDialog(@NonNull Context context) {
        super(context);
        this.activity=(ClientMainActivity)context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.create_request_dialog);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        submitButton = (Button) findViewById(R.id.submitButton);
        cancelButton.setOnClickListener(this);
        submitButton.setOnClickListener(this);
        numberSeats=findViewById(R.id.seatsNumber);
        trunk=findViewById(R.id.trunk);
        salonTrunk=findViewById(R.id.salonTrunk);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cancelButton:
                activity.cancelRequestCreation();
                dismiss();
                break;
            case R.id.submitButton:
                int seatsNumberValue=Integer.parseInt(numberSeats.getText().toString());
                int trunkValue=Integer.parseInt(trunk.getText().toString());
                int salonTrunkValue=Integer.parseInt(salonTrunk.getText().toString());
                if (seatsNumberValue>0 && trunkValue>0 && salonTrunkValue>0 && salonTrunkValue<10
                        && trunkValue<10 && seatsNumberValue<10){
                    activity.saveRequest(seatsNumberValue,trunkValue,salonTrunkValue);
                    dismiss();

                }

                break;
        }
    }
}
