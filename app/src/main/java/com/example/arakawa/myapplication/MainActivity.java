package com.example.arakawa.myapplication;

import java.io.IOException;

import com.example.arakawa.myapplication.backend.registration.Registration;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    //デベロッパーコンソールで取得したセンダーID
    private static final String  SENDER_ID = "859279548280";

    private GoogleCloudMessaging gcm;
    private String registrationId;
    private Context context;
    private AsyncTask<Void, Void, Void> registerTask;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    public static final int NOTIFICATION_ID = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button registerButton = (Button) findViewById(R.id.button);
        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("".equals(registrationId)) {

                    registerTask = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            if (gcm == null) {

                                gcm = GoogleCloudMessaging.getInstance(context);
                            }
                            try {
                                registrationId = gcm.register(SENDER_ID);
                                sendRegistrationIdToAppServer(registrationId);
                                storeRegistrationId(context, registrationId);

                                Log.d("REGID2",registrationId);
                            } catch (IOException e) {

                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void result) {
                            registerTask = null;
                        }
                    };
                    registerTask.execute(null, null, null);
                }
            }
        });
        context = getApplicationContext();

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(context);
            registrationId = getRegistrationId(context);
        } else {
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    /**
     * 端末のGooglePlayServiceAPKの有無をチェック
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * 端末に保存されているレジストレーションIDの取得
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.equals("")) {
            return "";
        }
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            return "";
        }

/*
        notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        builder = new NotificationCompat.Builder(this)
                .setContentTitle("GCM Notification")
                .setSmallIcon(R.drawable.common_signin_btn_icon_dark)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(registrationId))
                .setContentText(registrationId);

        builder.setContentIntent(contentIntent);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
*/
        Log.d("REGID",registrationId);
        return registrationId;
    }

    /**
     * アプリケーションのバージョン情報を取得する
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            throw new RuntimeException("パッケージが見つかりません:" + e);
        }
    }

    /**
     * アプリのプリファレンスを取得する
     */
    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * レジストレーションIDの端末保存
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void sendRegistrationIdToAppServer(String regId){
        //各々の仕様にあわせて実装してください
        Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), null)
                // Need setRootUrl and setGoogleClientRequestInitializer only for local testing,
                // otherwise they can be skipped
                .setRootUrl("https://premium-sum-127913.appspot.com/_ah/api/")
                .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                    @Override
                    public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest)
                            throws IOException {
                        abstractGoogleClientRequest.setDisableGZipContent(true);
                    }
                });
        Registration regService = builder.build();
        try{

            TelephonyManager manager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
            regService.register(regId,manager.getDeviceId()).execute();

        }catch (java.io.IOException e){
            e.printStackTrace();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
