package com.okcaros.minusscreen;

import static com.okcaros.tool.PcConst.NORMAL_PC_MEDIA_INFO;
import static com.okcaros.tool.PcConst.NORMAL_PC_MEDIA_THUMB_INFO;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.session.PlaybackState;

import org.greenrobot.eventbus.EventBus;

public class MainReceiver extends BroadcastReceiver {
    private final static String Tag = "MainReceiver";

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
            case NORMAL_PC_MEDIA_INFO: {
                EventBus.getDefault().post(new EventBusEvent.MediaInfo(
                        new MinusScreenViewRoot.MenuAppEntity.MediaMenuEntityData(intent.getStringExtra("title"),
                                intent.getStringExtra("albumTitle"),
                                intent.getStringExtra("artist"),
                                intent.getIntExtra("duration", 0),
                                intent.getIntExtra("state", PlaybackState.STATE_STOPPED))
                ));
                break;
            }
            case NORMAL_PC_MEDIA_THUMB_INFO: {
                byte[] data = intent.getByteArrayExtra("data");
                if (data == null) {
                    break;
                }
                EventBus.getDefault().post(new EventBusEvent.MediaThumbInfo(data));
                break;
            }
        }
    }


    public void register(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NORMAL_PC_MEDIA_INFO);
        intentFilter.addAction(NORMAL_PC_MEDIA_THUMB_INFO);

        context.registerReceiver(this, intentFilter);
    }
}