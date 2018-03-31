package com.john_yim.babyapplication;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Hashtable;
import java.util.Random;

import static com.john_yim.babyapplication.SettingsActivity.maxRate;
import static com.john_yim.babyapplication.SettingsActivity.maxTemperature;
import static com.john_yim.babyapplication.SettingsActivity.minRate;
import static com.john_yim.babyapplication.SettingsActivity.minTemperature;
import static com.john_yim.babyapplication.SettingsActivity.uvPoint;

public class BabyService extends Service {
    public BabyService() {
        this.serviceBinder = new ServiceBinder();
    }

    @Override
    public void onCreate() {
        this.heartRate = this.heartRateTemp = 000;
        this.heartRateText = "000";
        this.temperature = this.temperatureTemp = 00.0;
        this.temperatureText = "00.0";
        this.uv = this.uvTemp =  0.0;
        this.uvText = "0.0";
        this.isHeartRateBell = this.isTemperatureBell = this.isUvBell = true;
        this.temperatureTable = new Hashtable();
        this.heartRateTable = new Hashtable();
        this.uvTable = new Hashtable();
        this.intent = new Intent(CHANGE_STATUS_ACTION);
        this.socketThread = new Thread(new clientSocket());
        this.urlThread = new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonObject;
                while (!netFlag) {
                    jsonObject = getHttpValue("http://api.heclouds.com/devices/20123605/datapoints?datastream_id=pulse&limit=1", "GET");
//                    jsonObject = getHttpValue("http://112.74.49.223/zwz/son_monitor/?api=download", "GET");
                    try {
                        getBabyValue(jsonObject);
                        playWarningBell();
                    } catch (NullPointerException e) {
                        continue;
                    }  finally {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        System.out.println("绑定服务");
        return this.serviceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        System.out.println("创建服务");
        this.urlThread.start();
//        new Thread(new clientSocket()).start();
        return START_STICKY;
    }



    private JSONObject getHttpValue(String urlStr, String requestMethod) {
        JSONObject jsonObject = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setRequestMethod(requestMethod);
            connection.setRequestProperty("Charset", "utf-8");
            connection.addRequestProperty("api-key", "V2H4hdpQFh4=Q2KBLMNxINqKT0w=");
            InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer =new StringBuffer();
            String message;
            while ((message = bufferedReader.readLine()) != null) {
                stringBuffer.append(message);
            }
            jsonObject = new JSONObject(stringBuffer.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }  finally {
            return jsonObject;
        }
    }

    private synchronized void getBabyValue(String socketData) {
        try {
            this.temperatureText = socketData.substring(socketData.indexOf('T') + 1, socketData.indexOf('U'));
            this.temperature = Double.parseDouble(this.temperatureText);
            this.uvText = socketData.substring(socketData.indexOf('U') + 1);
            this.uv = Double.parseDouble(this.uvText);
            this.heartRateText = socketData.substring(socketData.indexOf('B') + 1, socketData.indexOf('T'));
            this.heartRate = Integer.parseInt(this.heartRateText);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println(e.getMessage());
        }
    }

    private synchronized void getBabyValue(JSONObject jsonObject) {
        if (jsonObject == null)
            return ;
        else {
            try {
                JSONObject dataJson = jsonObject.getJSONObject("data");
                JSONArray datastreamsJson = dataJson.getJSONArray("datastreams");
                String value = datastreamsJson.getJSONObject(0).getJSONArray("datapoints").getJSONObject(0)
                        .getString(VALUE_KEY);
//                String value = jsonObject.getJSONArray("data").getJSONObject(0).getString("rate");
                StringBuffer rateBuffer = new StringBuffer(3);
                StringBuffer temperatureBuffer = new StringBuffer(4);
                StringBuffer uvBuffer = new StringBuffer((4));
                char cFlag = '\0';
                for (char c :
                        value.toCharArray()) {
                    if (c == 'B') {
                        cFlag = c;
                        rateBuffer = new StringBuffer(3);
                    } else if (c == 'T') {
                        cFlag = c;
                        temperatureBuffer = new StringBuffer(4);
                    } else if (c == 'U') {
                        cFlag = c;
                        uvBuffer = new StringBuffer((4));
                    } else if (cFlag == 'B') {
                        rateBuffer.append(c);
                    } else if (cFlag == 'T') {
                        temperatureBuffer.append(c);
                    } else if (cFlag == 'U') {
                        uvBuffer.append(c);
                    }
                }
                this.heartRateText = rateBuffer.toString();
                this.temperatureText = temperatureBuffer.toString();
                this.uvText = uvBuffer.toString();
//                this.heartRateText = value.substring(value.indexOf('B') + 1, value.indexOf('T'));
//                this.temperatureText = value.substring(value.indexOf('T') + 1, value.indexOf('U'));
//                this.uvText = value.substring(value.indexOf('U') + 1);
                this.heartRate = Integer.parseInt(this.heartRateText);
                this.temperature = Double.parseDouble(this.temperatureText);
                this.uv = Double.parseDouble(this.uvText);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
                this.heartRate = this.heartRateTemp;
                this.temperature = this.temperatureTemp;
                this.uv = this.uvTemp;
            } catch (NumberFormatException e) {
                e.printStackTrace();
                this.heartRate = this.heartRateTemp;
                this.temperature = this.temperatureTemp;
                this.uv = this.uvTemp;
            } catch (StringIndexOutOfBoundsException e) {
                this.heartRate = this.heartRateTemp;
                this.temperature = this.temperatureTemp;
                this.uv = this.uvTemp;
                e.printStackTrace();
            } finally {
                this.heartRateTemp = this.heartRate;
                this.temperatureTemp = 36.0;
                this.uvTemp = 0.9;
                if (!modeFlag) {
                    Random random = new Random();
                    this.heartRate = random.nextInt() % 10 + 70;
                    this.heartRateText = String.valueOf(this.heartRate);
                }if (!temperatureModeFlag) {
                    Random random = new Random();
                    this.temperature = random.nextInt() % 5 + 25;
                    this.temperatureText = String.valueOf(this.temperature);
                }
                if (!uvModeFlag) {
                    this.uv = 1.2;
                    this.uvText = String.valueOf(this.uv);
                }
                return;
            }
        }
    }

    private void playWarningBell() {
        this.sendBoardFlag = false;
        boolean[] babyStatus = new boolean[3];
        if (this.heartRate > maxRate || this.heartRate < minRate) {
            this.rateFraction = this.heartRate > maxRate ?
                    (this.rateFraction = 40 - (this.heartRate - maxRate) * 2) :
                    (this.rateFraction = 40 - (minRate - this.heartRate) * 2);
            this.sendBoardFlag = babyStatus[0] = this.heartRateWarning = true;
            if (this.isHeartRateBell) {
                if (this.rateBell == null)
                    this.rateBell = MediaPlayer.create(this, RATE_BELL);
                if (!this.rateBell.isPlaying()) {
                    this.rateBell.setLooping(true);
                    this.rateBell.start();
                }
            } else {
                this.serviceBinder.pauseBell(RATE_BELL);
            }
        } else {
            if (this.rateBell != null && this.rateBell.isPlaying())
                this.rateBell.pause();
            babyStatus[0] = this.heartRateWarning = false;
        }
        if (this.temperature > maxTemperature || temperature < minTemperature) {
            this.temperatureFraction = this.temperature > maxTemperature ?
                    (int) (30 - (this.temperature - maxTemperature) / 0.4 * 5) :
                    (int) (30 - (minTemperature - this.temperature) / 0.4 * 5);
            this.sendBoardFlag = babyStatus[1] = this.temperatureWarning = true;
            if (this.isTemperatureBell) {
                if (this.temperatureBell == null)
                    this.temperatureBell = MediaPlayer.create(this, TEMPERATURE_BELL);
                if (!this.temperatureBell.isPlaying()) {
                    this.temperatureBell.setLooping(true);
                    this.temperatureBell.start();
                }
            } else {
                this.serviceBinder.pauseBell(TEMPERATURE_BELL);
            }
        } else {
            if (this.temperatureBell != null && this.temperatureBell.isPlaying())
                this.temperatureBell.pause();
            babyStatus[1] = this.temperatureWarning = false;
        }
        if (this.uv > uvPoint) {
            this.uvFraction = (int) (30 - (this.uv - uvPoint) * 6);
            this.sendBoardFlag = babyStatus[2] = this.uvWarning = true;
            if (isUvBell) {
                if (this.uvBell == null)
                    this.uvBell = MediaPlayer.create(this, UV_BELL);
                if (!this.uvBell.isPlaying()) {
                    this.uvBell.setLooping(true);
                    this.uvBell.start();
                }
            } else {
                this.serviceBinder.pauseBell(UV_BELL);
            }
        } else {
            if (this.uvBell != null && this.uvBell.isPlaying())
                this.uvBell.pause();
            babyStatus[2] = this.uvWarning = false;
        }
        if (this.sendBoardFlag) {
            this.healthFraction = (this.rateFraction + this.uvFraction + this.temperatureFraction) < 0 ?
                    0 : (this.rateFraction + this.uvFraction + this.temperatureFraction);
            this.intent.putExtra(WARING_KEY, babyStatus);
            this.intent.putExtra(FRATION_KEY, this.healthFraction);
            this.sendBroadcast(this.intent);
        }
    }

    public class ServiceBinder extends Binder {

        public void setIsTemperatureBell(boolean isBell) {
            isTemperatureBell = isBell;
        }

        public void setIsRateBell(boolean isBell) {
            isHeartRateBell = isBell;
        }

        public void setIsUvBell(boolean isBell) {
            isUvBell = isBell;
        }

        public void pauseBell(int bellFlag) {
            switch (bellFlag) {
                case TEMPERATURE_BELL:
                    if (temperatureBell != null && temperatureBell.isPlaying()) {
                        temperatureBell.pause();
                    }
                    break;
                case RATE_BELL:
                    if (rateBell != null && rateBell.isPlaying()) {
                        rateBell.pause();
                    }
                    break;
                case UV_BELL:
                    if (uvBell != null && uvBell.isPlaying()) {
                        uvBell.pause();
                    }
                    break;
            }
        }

        public synchronized Hashtable getHeartRate() {
            if (heartRate > maxRate || heartRate < minRate) {
                heartRateWarning = true;
            } else {
                heartRateWarning = false;
            }
            heartRateText = String.format("%3d", heartRate);
            heartRateTable.put(WARING_KEY, heartRateWarning);
            heartRateTable.put(VALUE_KEY, heartRateText);
            return heartRateTable;
        }

        public synchronized Hashtable getTemperature() {
            if (temperature > maxTemperature || temperature < minTemperature) {
                temperatureWarning = true;
            } else {
                temperatureWarning = false;
            }
            temperatureText = String.format("%.1f", temperature);
            temperatureTable.put(WARING_KEY, temperatureWarning);
            temperatureTable.put(VALUE_KEY, temperatureText);
            return temperatureTable;
        }

        public synchronized Hashtable getUv() {
            if (uv > uvPoint) {
                uvWarning = true;
            } else {
                uvWarning = false;
            }
            uvText = String.format("%.1f", uv);
            uvTable.put(WARING_KEY, uvWarning);
            uvTable.put(VALUE_KEY, uvText);
            return uvTable;
        }

        public void setON(final String urlStr, final String requestMethod, final int sendValue) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(urlStr);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoOutput(true);
                        connection.setDoInput(true);
                        connection.setRequestMethod(requestMethod);
                        connection.setRequestProperty("Charset", "UTF-8");
                        connection.addRequestProperty("Content-Type", "application/json");
                        connection.addRequestProperty("api-key", "V2H4hdpQFh4=Q2KBLMNxINqKT0w=");
//                        connection.addRequestProperty("U-ApiKey", "f66fe6f3c2af8fe89510e3a085827421");
                        connection.setUseCaches(false);
                        connection.setInstanceFollowRedirects(true);
                        connection.connect();
                        DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("button", String.valueOf(sendValue));
                        String postValue = jsonObject.toString();
                        dataOutputStream.writeBytes(postValue);
                        dataOutputStream.flush();
                        dataOutputStream.close();
                        InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        StringBuffer stringBuffer =new StringBuffer();
                        String message;
                        while ((message = bufferedReader.readLine()) != null) {
                            stringBuffer.append(message);
                        }
//                        JSONObject returnJson = new JSONObject(stringBuffer.toString());
//                        System.out.println(returnJson.getInt("value"));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    class clientSocket implements Runnable {

        Socket socket;
        DataInputStream in;
        DataOutputStream out;

        @Override
        public void run() {
            try {
                this.socket = new Socket("39.108.114.6", 3633);
                this.out = new DataOutputStream(this.socket.getOutputStream());
                this.in = new DataInputStream(this.socket.getInputStream());
                while (true) {
//                    this.out.writeBytes("app");
                    byte[] socketData = new byte[20];
                    in.read(socketData);
                    StringBuffer socketBuffer = new StringBuffer(20);
                    for (byte b :
                            socketData) {
                        char c = (char) b;
                        if (c > 122 || c < 46)
                            break;
                        socketBuffer.append(c);
                    }
                    String dataStr = socketBuffer.toString();
                    System.out.println("获取的数据为：" + dataStr);
                    getBabyValue(dataStr);
                    playWarningBell();
                    Thread.sleep(500);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private ServiceBinder serviceBinder;
    private Intent intent;
    private Thread socketThread;
    private Thread urlThread;

    private volatile String heartRateText;
    private volatile String temperatureText;
    private volatile String uvText;
    private int heartRateTemp;
    private double temperatureTemp;
    private double uvTemp;
    private double temperature;
    private int heartRate;
    private double uv;
    private boolean temperatureWarning;
    private boolean heartRateWarning;
    private boolean uvWarning;
    private boolean isTemperatureBell;
    private boolean isHeartRateBell;
    private boolean isUvBell;
    private Hashtable heartRateTable;
    private Hashtable temperatureTable;
    private Hashtable uvTable;

    private boolean sendBoardFlag;

    private MediaPlayer rateBell;
    private MediaPlayer temperatureBell;
    private MediaPlayer uvBell;

    private int healthFraction;
    private int rateFraction;
    private int temperatureFraction;
    private int uvFraction;

    public static boolean netFlag = false;
    public static boolean modeFlag = true;
    public static boolean temperatureModeFlag = true;
    public static boolean uvModeFlag = true;

    public static final String CHANGE_STATUS_ACTION = "change_status";
    public static final String FRATION_KEY = "health_fration";
    public static final String WARING_KEY = "warning";
    public static final String VALUE_KEY = "value";

    public static final int TEMPERATURE_BELL = R.raw.temperature_warn;
    public static final int RATE_BELL = R.raw.rate_warn;
    public static final int UV_BELL = R.raw.uv_warn;
}
