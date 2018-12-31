package vkalashnykov.org.busapplication.components;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import vkalashnykov.org.busapplication.R;

public class CreateRequestPanel extends LinearLayout {

    private TextView driveTimeText;

    public CreateRequestPanel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        getActivity().getLayoutInflater().inflate(R.layout.request_actions_panel,this);
    }

    private Activity getActivity() {
        return (Activity) getContext();
    }

    public void setDriveTime(long driveTime){
        driveTimeText=findViewById(R.id.driveTime);
        driveTimeText.setText("Estimated drive time: "+driveTime);
    }
}
