package com.john_yim.babyapplication.view;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.john_yim.babyapplication.R;

import static com.john_yim.babyapplication.MainActivity.serviceBinder;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WebFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WebFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static WebView webView;
    private EditText videoUrlText;
    private Button gotoVideoButton;
    private ImageView turnLeftButton;
    private ImageView turnRightButton;
    private Button stopButton;


    // TODO: Rename and change types of parameters
    public static String videoUrl = "http://192.168.43.179:8080/?action=stream";


    public WebFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment WebFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WebFragment newInstance() {
        WebFragment fragment = new WebFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView =  inflater.inflate(R.layout.fragment_web, container, false);
        videoUrlText = rootView.findViewById(R.id.video_url);
        webView = rootView.findViewById(R.id.video_web);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);// 支持缩放
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        gotoVideoButton = rootView.findViewById(R.id.goto_video);
        turnLeftButton = rootView.findViewById(R.id.rotate_left);
        turnRightButton = rootView.findViewById(R.id.rotate_right);
        stopButton = rootView.findViewById(R.id.stop_rotate);
        gotoVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String urlString = videoUrlText.getText().toString();
                if (urlString != null && !urlString.equals("")) {
                    webView.loadUrl("http://" + urlString);
                } else {
                    webView.loadUrl(videoUrl);
                }
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

                        return true;
                    }
                });
            }
        });
        videoUrlText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_ENTER) {
                    webView.loadUrl(videoUrlText.getText().toString());
                    webView.setWebViewClient(new WebViewClient() {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                            return true;
                        }
                    });
                }
                return false;
            }
        });
        turnLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceBinder.setON("http://api.heclouds.com/devices/20466971/datapoints?type=3", "POST", 1);
            }
        });
        turnRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceBinder.setON("http://api.heclouds.com/devices/20466971/datapoints?type=3", "POST", 2);
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceBinder.setON("http://api.heclouds.com/devices/20466971/datapoints?type=3", "POST", 0);
            }
        });
        return rootView;
    }

}
