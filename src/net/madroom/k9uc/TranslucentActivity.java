package net.madroom.k9uc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

public class TranslucentActivity extends Activity {

    private static final String K9_PACKAGE_NAME = "com.fsck.k9";
    private static final int REQUEST_CODE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.translucent_activity_layout);

        Intent i = new Intent();
        i.setPackage(K9_PACKAGE_NAME);
        i.setAction(Intent.ACTION_MAIN);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(i, REQUEST_CODE);

    }

    @Override
    public void onRestart(){
        super.onRestart();
        finish();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        startService(new Intent(this, WidgetProvider.MyService.class));
        startService(new Intent(this, Widget2x1Provider.MyService.class));
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        finish();
        return super.dispatchKeyEvent(e);
    }
}
