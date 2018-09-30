package vkalashnykov.org.busapplication.fragment;

import android.app.ListFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import vkalashnykov.org.busapplication.R;
import vkalashnykov.org.busapplication.api.domain.Route;

public class ClientRoutesListFragment extends ListFragment {

    // TODO: add FirebaseListAdapter to retrieve routes from Firebase Database
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client_routes_list,null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DatabaseReference routesReference= FirebaseDatabase.getInstance().getReference().child("routes");
//        FirebaseListAdapter<Route> adapter=

    }
}
