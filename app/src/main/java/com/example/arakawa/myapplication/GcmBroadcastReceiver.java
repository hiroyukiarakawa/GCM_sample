/**
 * Created by 弘之 on 2016/04/16.
 */
package com.example.arakawa.myapplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class GcmBroadcastReceiver  extends WakefulBroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        //受け取ったインテントの処理をGcmIntentServiceで行う
        Log.d("GcmBroadcastReceiver","onReceive");
        ComponentName comp = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName());

        //サービスの起動。処理中スリープを制御
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}
