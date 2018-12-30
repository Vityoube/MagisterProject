package vkalashnykov.org.busapplication.layouts;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import vkalashnykov.org.busapplication.R;

public class DriverBusCurrentDetails extends LinearLayout {

    private TextView seatsOccupiedTextView;
    private TextView seatsTextView;
    private TextView trunkOccupiedTextView;
    private TextView trunkTextView;
    private TextView salonTrunkOccupiedTextView;
    private TextView salonTrunkTextView;

    public DriverBusCurrentDetails(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        getActivity().getLayoutInflater()
                .inflate(R.layout.driver_bus_current_details, this);
    }

    private Activity getActivity() {
        return (Activity) getContext();
    }

    public void setSeatsOccupied(int seatsOccupied){
        seatsOccupiedTextView=findViewById(R.id.seatsOccupied);
        seatsOccupiedTextView.setText(String.valueOf(seatsOccupied));
    }

    public void setSeats(int seats){
        seatsTextView=findViewById(R.id.seats);
        seatsTextView.setText(String.valueOf(seats));
    }

    public void setTrunkOccupied(int trunkOccupied){
        trunkOccupiedTextView=findViewById(R.id.trunkOccupied);
        trunkOccupiedTextView.setText(String.valueOf(trunkOccupied));
    }

    public void setTrunk(int trunk){
        trunkTextView=findViewById(R.id.trunk);
        trunkTextView.setText(String.valueOf(trunk));
    }

    public void setSalonTrunk(int salonTrunk){
        salonTrunkTextView=findViewById(R.id.salonTrunk);
        salonTrunkTextView.setText(String.valueOf(salonTrunk));
    }

    public void setSalonTrunkOccupied(int salonTrunkOccupied){
        salonTrunkOccupiedTextView=findViewById(R.id.salonTrunkOccupied);
        salonTrunkOccupiedTextView.setText(String.valueOf(salonTrunkOccupied));
    }


}
