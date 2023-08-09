package com.okcaros.minusscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.greenrobot.eventbus.EventBus;

public class MainReceiver extends BroadcastReceiver {
    private final static String Tag = "MainReceiver";
    public final static String ActionPcMediaInfo = "pc.intent.action.pc.VC_MEDIA_INFO";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (action == null) {
            return;
        }

        switch (action) {
            case ActionPcMediaInfo: {
                EventBus.getDefault().post(new EventBusEvent.MediaInfo(
                        new MinusScreenViewRoot.MenuAppEntity.MediaMenuEntityData(intent.getStringExtra("title"),
                                intent.getStringExtra("albumTitle"),
                                intent.getStringExtra("artist"),
                                intent.getIntExtra("duration", 0))
                ));
                break;
            }
        }
    }


    public void register(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActionPcMediaInfo);

        context.registerReceiver(this, intentFilter);
    }
}