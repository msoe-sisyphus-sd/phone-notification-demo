package com.sisyphus.notificationdemo;

import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.loopj.android.http.*;
import org.json.*;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.client.HttpClient;

import androidx.annotation.RequiresApi;
import java.util.concurrent.TimeUnit;

/**
 * MIT License
 *
 *  Copyright (c) 2016 Fábio Alves Martins Pereira (Chagall)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationListenerTableService extends NotificationListenerService {

    /*
        These are the package names of the apps. for which we want to
        listen the notifications
     */
    private static final class ApplicationPackageNames {
        public static final String FACEBOOK_PACK_NAME = "com.facebook.katana";
        public static final String FACEBOOK_MESSENGER_PACK_NAME = "com.facebook.orca";
        public static final String WHATSAPP_PACK_NAME = "com.whatsapp";
        public static final String INSTAGRAM_PACK_NAME = "com.instagram.android";
    }

    /*
        These are the return codes we use in the method which intercepts
        the notifications, to decide whether we should do something or not
     */
    public static final class InterceptedNotificationCode {
        public static final int FACEBOOK_CODE = 1;
        public static final int WHATSAPP_CODE = 2;
        public static final int INSTAGRAM_CODE = 3;
        public static final int OTHER_NOTIFICATIONS_CODE = 4; // We ignore all notification with code == 4
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        System.out.println(sbn.getTag());
        int notificationCode = matchNotificationCode(sbn);
        SisbotRestUsage sb = new SisbotRestUsage();

        if(notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE) {
            Intent intent = new  Intent("com.sisyphus.notificationdemo");
            intent.putExtra("Notification Code", notificationCode);
            sendBroadcast(intent);
        }
        sb.getCurrentLightPattern();;
        System.out.println("WE GOT A RESPONSE, WOOO!");
        System.out.println("WE GOT A NOTIFICATION WOOOOOOOOO!");



    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        int notificationCode = matchNotificationCode(sbn);
        if(notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE) {

            StatusBarNotification[] activeNotifications = this.getActiveNotifications();

            if(activeNotifications != null && activeNotifications.length > 0) {
                for (int i = 0; i < activeNotifications.length; i++) {
                    if (notificationCode == matchNotificationCode(activeNotifications[i])) {
                        Intent intent = new  Intent("com.sisyphus.notificationdemo");
                        intent.putExtra("Notification Code", notificationCode);
                        sendBroadcast(intent);
                        break;
                    }
                }
            }
        }



    }

    private int matchNotificationCode(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();

        if(packageName.equals(ApplicationPackageNames.FACEBOOK_PACK_NAME)
                || packageName.equals(ApplicationPackageNames.FACEBOOK_MESSENGER_PACK_NAME)){
            return(InterceptedNotificationCode.FACEBOOK_CODE);
        }
        else if(packageName.equals(ApplicationPackageNames.INSTAGRAM_PACK_NAME)){
            return(InterceptedNotificationCode.INSTAGRAM_CODE);
        }
        else if(packageName.equals(ApplicationPackageNames.WHATSAPP_PACK_NAME)){
            return(InterceptedNotificationCode.WHATSAPP_CODE);
        }
        else{
            return(InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE);
        }
    }
}

class SisbotRestUsage {
    private String currentLedPattern = null;
    public void getCurrentLightPattern() {
        RequestParams p = new RequestParams("data", "{}");
        SisbotRestClient.post("state", p, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray

                try {
                    JSONArray j = (JSONArray) response.get("resp");
                    JSONObject firstListItem = (JSONObject) j.get(0);
                    if (currentLedPattern == null) {
                        currentLedPattern = (String) firstListItem.get("led_pattern");
                        System.out.println(currentLedPattern);
                    }
                    flashSolidPattern();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                // Pull out the first event on the public timeline
                JSONObject firstEvent = null;

            }
        });
    }
    public void flashSolidPattern() {
        RequestParams primaryColor = new RequestParams("data", "{\"data\": {\"led_primary_color\": \"#FFFFFF\"}}");
        SisbotRestClient.post("set_led_color", primaryColor, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray

                System.out.println("Set primary color.");
                setSolid();

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                // Pull out the first event on the public timeline
                JSONObject firstEvent = null;

            }
        });
     }

    public void setSolid() {
        RequestParams solidPattern = new RequestParams("data", "{\"data\": {\"id\": \"solid\"}}");
        SisbotRestClient.post("set_led_pattern", solidPattern, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                System.out.println("Set flash.");
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                setOriginal();

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                // Pull out the first event on the public timeline
                JSONObject firstEvent = null;

            }
        });
    }

    public void setOriginal() {
        RequestParams originalPattern = new RequestParams("data", "{\"data\": {\"id\": \"" +  this.currentLedPattern +"\"}}");
        SisbotRestClient.post("set_led_pattern", originalPattern, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray

                System.out.println("Set original pattern.");

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                // Pull out the first event on the public timeline
                JSONObject firstEvent = null;

            }
        });
    }

    public String getPatternName() {
        return this.currentLedPattern;
    }
}
class SisbotRestClient {
    private static final String BASE_URL = "http://seniordesigntable.msoe.edu:3002/sisbot/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);

    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
