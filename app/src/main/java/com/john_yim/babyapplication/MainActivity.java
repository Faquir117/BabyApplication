package com.john_yim.babyapplication;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.john_yim.babyapplication.view.HeartFragment;
import com.john_yim.babyapplication.view.TemperatureFragment;
import com.john_yim.babyapplication.view.UvFragment;
import com.john_yim.babyapplication.view.WebFragment;

import static com.john_yim.babyapplication.BabyService.CHANGE_STATUS_ACTION;
import static com.john_yim.babyapplication.BabyService.FRATION_KEY;
import static com.john_yim.babyapplication.BabyService.WARING_KEY;
import static com.john_yim.babyapplication.MainActivity.PlaceholderFragment.healthFractionText;
import static com.john_yim.babyapplication.MainActivity.PlaceholderFragment.rateBtn;
import static com.john_yim.babyapplication.MainActivity.PlaceholderFragment.rateStatusText;
import static com.john_yim.babyapplication.MainActivity.PlaceholderFragment.temperatureBtn;
import static com.john_yim.babyapplication.MainActivity.PlaceholderFragment.temperatureStatusText;
import static com.john_yim.babyapplication.MainActivity.PlaceholderFragment.uvBtn;
import static com.john_yim.babyapplication.MainActivity.PlaceholderFragment.uvStatusText;
import static com.john_yim.babyapplication.view.WebFragment.videoUrl;
import static com.john_yim.babyapplication.view.WebFragment.webView;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    public static BabyService.ServiceBinder serviceBinder;
    private static BroadcastReceiver receiver;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            serviceBinder = (BabyService.ServiceBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    public static final String[] tabTitle = {"健康指数", "紫外线", "温度", "心率", "监控"};

    private String heartRate, temperature, uv;
    private boolean[] babyStatus;
    private int on_off;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        heartRate = temperature = uv = "??";
        on_off = 0;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 3:
                        if (serviceBinder != null) {
                            heartRate = serviceBinder.getHeartRate().get(BabyService.VALUE_KEY).toString();
                        }
                        ((TextView) findViewById(R.id.heartRateValue)).setText(heartRate);
                        break;
                    case 2:
                        if (serviceBinder != null) {
                            temperature = serviceBinder.getTemperature().get(BabyService.VALUE_KEY).toString();
                        }
                        ((TextView) findViewById(R.id.temperatureValue)).setText(temperature);
                        break;
                    case 1:
                        if (serviceBinder != null) {
                            uv = serviceBinder.getUv().get(BabyService.VALUE_KEY).toString();
                        }
                        ((TextView) findViewById(R.id.uvValue)).setText(uv);
                        break;
                    case 4:
                        WebSettings webSettings = webView.getSettings();
                        webSettings.setJavaScriptEnabled(true);
                        webSettings.setSupportZoom(true);// 支持缩放
                        webSettings.setBuiltInZoomControls(true);
                        webSettings.setDisplayZoomControls(false);
                        webView.loadUrl(videoUrl);
                        webView.setWebViewClient(new WebViewClient() {
                            @Override
                            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                                return true;
                            }
                        });
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(CHANGE_STATUS_ACTION)) {
                    System.out.println("收到广播");
                    int healthFraction = intent.getIntExtra(FRATION_KEY, 100);
                    healthFractionText.setText(healthFraction + "");
                    babyStatus = intent.getBooleanArrayExtra(WARING_KEY);
                    if (babyStatus[0]) {
                        rateStatusText.setText("警告");
                        rateBtn.setBackgroundColor(getResources().getColor(R.color.auntRed));
                    }
                    else {
                        rateStatusText.setText("正常");
                        rateBtn.setBackgroundColor(getResources().getColor(R.color.backgroundWhite));
                    }
                    if (babyStatus[1]) {
                        temperatureStatusText.setText("警告");
                        temperatureBtn.setBackgroundColor(getResources().getColor(R.color.auntRed));
                    }
                    else {
                        temperatureStatusText.setText("正常");
                        temperatureBtn.setBackgroundColor(getResources().getColor(R.color.backgroundWhite));
                    }
                    if (babyStatus[2]) {
                        uvStatusText.setText("警告");
                        uvBtn.setBackgroundColor(getResources().getColor(R.color.auntRed));
                    }
                    else {
                        uvStatusText.setText("正常");
                        uvBtn.setBackgroundColor(getResources().getColor(R.color.backgroundWhite));
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(CHANGE_STATUS_ACTION);
        registerReceiver(receiver, intentFilter);


        final Intent serviceIntent = new Intent(this, BabyService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        PlaceholderFragment.activity = this;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_baby, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements View.OnClickListener {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_PARAM1 = ARG_SECTION_NUMBER;
        private static final String ARG_PARAM2 = ARG_PARAM1;

        static TextView healthFractionText;
        static TextView rateStatusText;
        static TextView temperatureStatusText;
        static TextView uvStatusText;
        static TextView switchStatusText;
        static RelativeLayout rateBtn;
        static RelativeLayout temperatureBtn;
        static RelativeLayout uvBtn;

        static MainActivity activity;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(String param1, String param2) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putString(ARG_PARAM1, param1);
            args.putString(ARG_PARAM2, param2);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_health, container, false);
            healthFractionText = rootView.findViewById(R.id.health_fraction);
            rateStatusText = rootView.findViewById(R.id.rate_status);
            temperatureStatusText = rootView.findViewById(R.id.temperature_status);
            uvStatusText = rootView.findViewById(R.id.uv_status);
            rateBtn = rootView.findViewById(R.id.goto_rateFragement);
            temperatureBtn = rootView.findViewById(R.id.goto_temperatureFragement);
            uvBtn = rootView.findViewById(R.id.goto_uvFragement);
            rateBtn.setOnClickListener(this);
            temperatureBtn.setOnClickListener(this);
            uvBtn.setOnClickListener(this);
            return rootView;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.goto_uvFragement:
                    activity.mViewPager.setCurrentItem(1);
                    break;
                case R.id.goto_temperatureFragement:
                    activity.mViewPager.setCurrentItem(2);
                    break;
                case R.id.goto_rateFragement:
                    activity.mViewPager.setCurrentItem(3);
                    break;
                default:
                    activity.mViewPager.setCurrentItem(0);
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 1: return UvFragment.newInstance(uv);
                case 2: return TemperatureFragment.newInstance(temperature);
                case 3: return HeartFragment.newInstance(heartRate);
                case 4: return WebFragment.newInstance();
            }
            Fragment fragment = PlaceholderFragment.newInstance("health", "baby_health");
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return tabTitle[0];
                case 1:
                    return tabTitle[1];
                case 2:
                    return tabTitle[2];
                case 3:
                    return tabTitle[3];
                case 4:
                    return tabTitle[4];
            }
            return null;
        }
    }
}
