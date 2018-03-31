package com.john_yim.babyapplication.view;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.john_yim.babyapplication.R;

import static com.john_yim.babyapplication.BabyService.UV_BELL;
import static com.john_yim.babyapplication.MainActivity.serviceBinder;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UvFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UvFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UvFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "uv";
    public static boolean viewFlag;

    // TODO: Rename and change types of parameters
    private String uv;
    private TextView uvTextView;
    private UvCurveView uvCurveView;

    public UvFragment() {
        // Required empty public constructor
        viewFlag = false;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 the strength of uv.
     * @return A new instance of fragment UvFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UvFragment newInstance(String param1) {
        UvFragment fragment = new UvFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uv = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_uv, container, false);
        this.uvCurveView = rootView.findViewById(R.id.uvCurve);
        this.uvTextView = rootView.findViewById(R.id.uvValue);
        viewFlag = true;
        this.uvCurveView.uvDetection(rootView);
        this.uvTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceBinder.pauseBell(UV_BELL);
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
