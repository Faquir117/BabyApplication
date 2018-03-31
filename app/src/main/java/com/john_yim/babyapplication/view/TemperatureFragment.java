package com.john_yim.babyapplication.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.john_yim.babyapplication.R;

import static com.john_yim.babyapplication.BabyService.TEMPERATURE_BELL;
import static com.john_yim.babyapplication.BabyService.temperatureModeFlag;
import static com.john_yim.babyapplication.MainActivity.serviceBinder;
import static com.john_yim.babyapplication.SettingsActivity.maxTemperature;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TemperatureFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TemperatureFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TemperatureFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "temperature";
    public static boolean viewFlag;

    // TODO: Rename and change types of parameters
    private String temperature;
    private TextView temperatureTextView;
    private TextView temperatureWarningText;
    private TemperatureCurveView temperatureCurveView;


    public TemperatureFragment() {
        // Required empty public constructor
        viewFlag = false;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param temperature baby's temperature.
     * @return A new instance of fragment TemperatureFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TemperatureFragment newInstance(String temperature) {
        TemperatureFragment fragment = new TemperatureFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, temperature);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            temperature = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_temperature, container, false);
        viewFlag = true;
        this.temperatureTextView = rootView.findViewById(R.id.temperatureValue);
        this.temperatureCurveView = rootView.findViewById(R.id.temperatureCurve);
        this.temperatureWarningText = rootView.findViewById(R.id.alertTemperature);
        this.temperatureWarningText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                temperatureModeFlag = !temperatureModeFlag;
            }
        });
        this.temperatureCurveView.thermometric(rootView);
        this.temperature = String.format("%.1f", maxTemperature);
        this.temperatureWarningText.setText(this.temperature);
        this.temperatureTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceBinder.pauseBell(TEMPERATURE_BELL);
            }
        });
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewFlag = false;
    }
}
