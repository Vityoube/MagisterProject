package vkalashnykov.org.busapplication.components;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import vkalashnykov.org.busapplication.R;

public class MySeekbar extends LinearLayout {
    private SeekBar Seekbar = null;
    private TextView seekbarValue=null;

    public MySeekbar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        getActivity().getLayoutInflater()
                .inflate(R.layout.seekbar, this);
    }

    private Activity getActivity() {
        return (Activity) getContext();
    }

    public int getProgress() {
        return getSeekbar().getProgress();
    }

    public void setProgress(int progress) {
        getSeekbar().setProgress(progress);
    }

    public void setMax(List<String> intervals) {
        seekbarValue=(TextView)findViewById(R.id.seekbarValue);
        getSeekbar().setMax(intervals.size() );
        getSeekbar().setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekbarValue.setText(String.valueOf(progress));
                if (getId()==R.id.seatsNumber && progress>0) {
                    MySeekbar minimumSeatNumberSeekbar =
                            (MySeekbar)getActivity().findViewById(R.id.minimumSeatsNumber);
                    minimumSeatNumberSeekbar.getSeekbar().setMax(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    private SeekBar getSeekbar() {
        if (Seekbar == null) {
            Seekbar = (SeekBar) findViewById(R.id.seekbar);
        }

        return Seekbar;
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener){
        getSeekbar().setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    public TextView getSeekbarValue() {
        return seekbarValue;
    }
}