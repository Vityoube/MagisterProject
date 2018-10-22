package vkalashnykov.org.busapplication.fragment;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import vkalashnykov.org.busapplication.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateRequestFragment extends DialogFragment {


    public CreateRequestFragment() {
        // Required empty public constructor
    }

    public interface CreateRequestFragmentListener {
        public void onSubmitClick(DialogFragment dialogFragment);
    }

    private CreateRequestFragmentListener listener;




    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater=getActivity().getLayoutInflater();
        builder.setView(layoutInflater.inflate(R.layout.create_request,null))
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CreateRequestFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener=(CreateRequestFragmentListener)context;
        } catch (ClassCastException e){
            Log.e("ClientCreateRequest",e.toString());
        }

    }
}
