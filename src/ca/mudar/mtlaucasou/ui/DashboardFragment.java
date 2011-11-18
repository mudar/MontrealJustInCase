
package ca.mudar.mtlaucasou.ui;

import ca.mudar.mtlaucasou.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DashboardFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        root.findViewById(R.id.home_btn_fire_halls).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity().getApplicationContext(),
                                FireHallsMapActivity.class);
                        startActivity(intent);
                    }
                });
        root.findViewById(R.id.home_btn_spvm_stations).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity().getApplicationContext(),
                                SpvmStationsMapActivity.class);
                        startActivity(intent);
                    }
                });
        root.findViewById(R.id.home_btn_water_supplies).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity().getApplicationContext(),
                                WaterSuppliesMapActivity.class);
                        startActivity(intent);
                    }
                });
        root.findViewById(R.id.home_btn_emergency_hostels).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity().getApplicationContext(),
                                EmergencyHostelsMapActivity.class);
                        startActivity(intent);
                    }
                });

        return root;
    }

}
