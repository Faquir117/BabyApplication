package com.john_yim.babyapplication.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.john_yim.babyapplication.R;

import static com.john_yim.babyapplication.BabyService.RATE_BELL;
import static com.john_yim.babyapplication.BabyService.modeFlag;
import static com.john_yim.babyapplication.MainActivity.serviceBinder;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HeartFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HeartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HeartFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "heartRate";

    public static boolean viewFlag;

    // TODO: Rename and change types of parameters
    private String heartRate;

    private HeartRateCurveView heartRateCurve;
    private TextView heartRateValue;
    private TextView changeMode;
    public static TextView switchStatus;

    public HeartFragment() {
        // Required empty public constructor
        viewFlag = false;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param baby's heart rate.
     * @return A new instance of fragment HeartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HeartFragment newInstance(String param) {
        HeartFragment fragment = new HeartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            heartRate = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_heart, container, false);
        viewFlag = true;
        this.changeMode = rootView.findViewById(R.id.mode_change);
        this.heartRateCurve = rootView.findViewById(R.id.heartRate);
        this.heartRateValue = rootView.findViewById(R.id.heartRateValue);
        this.changeMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modeFlag = !modeFlag;
            }
        });
        this.heartRateValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceBinder.pauseBell(RATE_BELL);
            }
        });
        this.heartRateCurve.heartBeat(this.heartRateValue);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewFlag = false;
    }
}
