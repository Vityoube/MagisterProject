package vkalashnykov.org.busapplication.fragment;

import android.app.LauncherActivity;
import android.app.ListFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import vkalashnykov.org.busapplication.R;
import vkalashnykov.org.busapplication.api.domain.Route;

public class ClientRoutesListFragment extends ListFragment {
    private OnChooseRouteFromListListener listener;

    private FirebaseListAdapter<Route> routeAdapter;
    private DatabaseReference selectedRouteRef;
    /* TODO: add possibility to pass the Selected Route Reference to ClientMapFragment
    so it can display the route and current driver position
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client_routes_list,null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Query routesQuery= FirebaseDatabase.getInstance()
                .getReference().child("routes")
                .orderByKey();
        FirebaseListOptions listOptions=new FirebaseListOptions.Builder<Route>()
                .setLayout(R.layout.route_item)
                .setQuery(routesQuery,Route.class)
                .build();
        routeAdapter=new FirebaseListAdapter<Route>(listOptions) {
            @Override
            protected void populateView(View v, Route model, int position) {
                TextView routeDriverName=v.findViewById(R.id.driverName);
                routeDriverName.setText(model.getDriverName());
            }
        };
        ListView routesList=getListView();
        routesList.setAdapter(routeAdapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedRouteRef=routeAdapter.getRef(position);
                listener.passRouteToMap(selectedRouteRef.getKey());
                for (int i=0;i<parent.getCount();i++){
                    if (i==position){
                        parent.getChildAt(i).setBackgroundColor(Color.GREEN);
                    } else {
                        parent.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                    }
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        routeAdapter.startListening();
    }


    @Override
    public void onStop() {
        super.onStop();
        routeAdapter.stopListening();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener=(OnChooseRouteFromListListener)context;
        } catch (ClassCastException e){
            Log.e("ClientRoutesList","Failed to instantiate listener: "+e.toString());
        }
    }
}
