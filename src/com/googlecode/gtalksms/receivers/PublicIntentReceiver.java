package com.googlecode.gtalksms.receivers;

import com.googlecode.gtalksms.Log;
import com.googlecode.gtalksms.MainService;
import com.googlecode.gtalksms.SettingsManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class PublicIntentReceiver extends BroadcastReceiver {
    private static PublicIntentReceiver sPublicIntentReceiver;
    private static IntentFilter sIntentFilter;

    private static SettingsManager sSettings;
    private Context mContext;

    static {
        sIntentFilter = new IntentFilter();
        // ACTION_CONNECT and TOGGLE is received by filter within the Apps Manifest
        // sIntentFilter.addAction(MainService.ACTION_CONNECT);
        // sIntentFilter.addAction(MainService.ACTION_TOGGLE);
        sIntentFilter.addAction(MainService.ACTION_COMMAND);
        sIntentFilter.addAction(MainService.ACTION_SEND);
    }

    /**
     * A nice empty constructor stub, so that Android can
     * instantiate this class
     */
    public PublicIntentReceiver() {
        // Does nothing
    }

    private PublicIntentReceiver(Context context) {
        sSettings = SettingsManager.getSettingsManager(context);
        this.mContext = context;
    }

    public static PublicIntentReceiver getReceiver(Context ctx) {
        if (sPublicIntentReceiver == null)
            sPublicIntentReceiver = new PublicIntentReceiver(ctx);

        return sPublicIntentReceiver;
    }

    public void onServiceStart() {
        mContext.registerReceiver(this, sIntentFilter);
    }

    public void onServiceStop() {
        mContext.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("PublicIntentReceiver got intent: " + intent.getAction());

        // Init the static class to have access to the SettingsManager
        if (sPublicIntentReceiver == null)
            sPublicIntentReceiver = new PublicIntentReceiver(context);

        String token = intent.getStringExtra("token");
        if (sSettings.publicIntentsEnabled) {
            if (sSettings.publicIntentTokenRequired) {
                if (token == null || !sSettings.publicIntentToken.equals(token)) {
                    // token required but no token set or it doesn't match
                    Log.w("Public intent without correct security token received");
                    return;
                }
            }
            intent.setClass(context, MainService.class);
            context.startService(intent);
        } else {
            Log.w("Received public intent but public intents are disabled");
        }
    }
}