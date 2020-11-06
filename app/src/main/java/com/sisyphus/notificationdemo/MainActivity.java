package com.sisyphus.notificationdemo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import com.loopj.android.http.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.service.notification.NotificationListenerService;
import android.text.TextUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.ComponentName;
import android.view.View;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver broadcastReceiver;
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent= new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        startActivity(intent);
        if (!isNotificationServiceEnabled()) {
            System.err.println("Notification Service is not enabled!");
        } else {

//       Finally we register a receiver to tell the MainActivity when a notification has been received

            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int receivedNotificationCode = intent.getIntExtra("Notification Code", -1);
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.sisyphus.notificationdemo");
            registerReceiver(broadcastReceiver, intentFilter);

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
    /**
     * Is Notification Service Enabled.
     * Verifies if the notification listener service is enabled.
     * Got it from: https://github.com/kpbird/NotificationListenerService-Example/blob/master/NLSExample/src/main/java/com/kpbird/nlsexample/NLService.java
     * @return True if enabled, false otherwise.
     */
    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        boolean havePermission = VerifyNotificationPermission();
        if (!havePermission) { return false; }
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true && havePermission;
                    }
                }
            }
        }
        return false;
    }

    public Boolean VerifyNotificationPermission() {
        String theList = android.provider.Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        String[] theListList = theList.split(":");
        String me = (new ComponentName(this, NotificationListenerTableService.class)).flattenToString();
        for ( String next : theListList ) {
            if ( me.equals(next) ) return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

}