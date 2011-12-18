package net.madroom.k9uc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class K9Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, WidgetProvider.MyService.class));
    }
}
