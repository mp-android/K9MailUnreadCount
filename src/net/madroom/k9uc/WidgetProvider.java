package net.madroom.k9uc;

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {
    private static final String ACTION_CLICK = "net.madroom.k9uc.action.CLICK";

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, MyService.class));
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }
    
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }
    /***************************************************************************
     * MyService
     ***************************************************************************/
    public static class MyService extends Service {
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        Context mContext;
        private SharedPreferences mPref;
        ComponentName mComponentName;
        AppWidgetManager mManager;
        RemoteViews mRemoteViews;

        /***************************************************************************
         * onCreate
         ***************************************************************************/
        @Override
        public void onCreate() {
            mContext = this;
            mPref = PreferenceManager.getDefaultSharedPreferences(mContext);
            mComponentName = new ComponentName(mContext, WidgetProvider.class);
            mManager = AppWidgetManager.getInstance(mContext);
            initializeRemoteViews();
        }

        /**
         * initializeRemoteViews
         */
        public void initializeRemoteViews() {
            switch(Build.VERSION.SDK_INT) {
            case Build.VERSION_CODES.ECLAIR_MR1:
                switch (mPref.getInt(MainActivity.KEY_BG_TYPE, MainActivity.DEF_BG_TYPE)) {
                case R.id.bg_type_transparency:
                    mRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_layout_transparency);
                    break;
                case R.id.bg_type_translucent:
                    mRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_layout_translucent);
                    break;
                case R.id.bg_type_opacity:
                    mRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_layout_opacity);
                    break;
                default:
                    mRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_layout_translucent);
                    break;
                }
                break;
            default:
                mRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_layout_translucent);
                break;
            }
        }

        /***************************************************************************
         * onStart
         ***************************************************************************/
        @Override
        public void onStart(Intent intent, int startId) {
            final int widgetCount =
                AppWidgetManager.getInstance(mContext).getAppWidgetIds(new ComponentName(mContext, WidgetProvider.class)).length;
            if(widgetCount==0) return;

            if(intent!=null && intent.getAction()!=null) {
                if(intent.getAction().equals(ACTION_CLICK)) {
                    Intent i = new Intent(mContext, TranslucentActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    stopSelf();
                    return;
                }
            }

            // For Crash error
            try {
                setRemoteViews();
                mManager.updateAppWidget(mComponentName, mRemoteViews);
            } catch (Exception e) {
                stopSelf();
            }
            stopSelf();
        }

        /**
         * setRemoteViews
         */
        public void setRemoteViews() {
            int total = getTotalUnreadCount();
            mRemoteViews.setOnClickPendingIntent(R.id.base_layout, getPendingIntent());
            mRemoteViews.setTextViewText(R.id.count_text, ""+total);
            if(total==0) {
                int color = Color.argb(
                        mPref.getInt(MainActivity.KEY_TEXT_ZERO_COLOR_A, MainActivity.DEF_TEXT_ZERO_COLOR_A),
                        mPref.getInt(MainActivity.KEY_TEXT_ZERO_COLOR_R, MainActivity.DEF_TEXT_ZERO_COLOR_R),
                        mPref.getInt(MainActivity.KEY_TEXT_ZERO_COLOR_G, MainActivity.DEF_TEXT_ZERO_COLOR_G),
                        mPref.getInt(MainActivity.KEY_TEXT_ZERO_COLOR_B, MainActivity.DEF_TEXT_ZERO_COLOR_B));
                mRemoteViews.setTextColor(R.id.count_text, color);
            } else {
                int color = Color.argb(
                        mPref.getInt(MainActivity.KEY_TEXT_NOT_ZERO_COLOR_A, MainActivity.DEF_TEXT_NOT_ZERO_COLOR_A),
                        mPref.getInt(MainActivity.KEY_TEXT_NOT_ZERO_COLOR_R, MainActivity.DEF_TEXT_NOT_ZERO_COLOR_R),
                        mPref.getInt(MainActivity.KEY_TEXT_NOT_ZERO_COLOR_G, MainActivity.DEF_TEXT_NOT_ZERO_COLOR_G),
                        mPref.getInt(MainActivity.KEY_TEXT_NOT_ZERO_COLOR_B, MainActivity.DEF_TEXT_NOT_ZERO_COLOR_B));
                mRemoteViews.setTextColor(R.id.count_text, color);
            }
            switch(Build.VERSION.SDK_INT) {
            case Build.VERSION_CODES.ECLAIR_MR1:
                break;
            default:
                int color = Color.argb(
                        mPref.getInt(MainActivity.KEY_BG_COLOR_A, MainActivity.DEF_BG_COLOR_A),
                        mPref.getInt(MainActivity.KEY_BG_COLOR_R, MainActivity.DEF_BG_COLOR_R),
                        mPref.getInt(MainActivity.KEY_BG_COLOR_G, MainActivity.DEF_BG_COLOR_G),
                        mPref.getInt(MainActivity.KEY_BG_COLOR_B, MainActivity.DEF_BG_COLOR_B));
                mRemoteViews.setInt(R.id.base_layout, "setBackgroundColor", color);
                break;
            }
        }

        /**
         * getTotalUnreadCount
         */
        public static final Uri K9_ACCOUNTS_URI =
            Uri.parse("content://com.fsck.k9.messageprovider/accounts/");

        public static final Uri K9_ACCOUNT_UNREAD_URI = 
            Uri.parse("content://com.fsck.k9.messageprovider/account_unread/");

        public static final String ACCOUNT_NUMBER = "accountNumber";
        public static final String UNREAD = "unread";

        public int getTotalUnreadCount() {
            int total = 0;

            final List<Integer> accountNumbers = new ArrayList<Integer>();

            final Cursor accountCursor = getCursor(mContext, K9_ACCOUNTS_URI);
            if(accountCursor.moveToFirst()) {
                do {
                    final int accountNumber =
                        accountCursor.getInt(accountCursor.getColumnIndex(ACCOUNT_NUMBER));
                    accountNumbers.add(accountNumber);
                } while(accountCursor.moveToNext());
            }
            accountCursor.close();

            for(int accountNumber : accountNumbers) {
                final Cursor unreadCursor =
                    getCursor(mContext, ContentUris.withAppendedId(K9_ACCOUNT_UNREAD_URI,accountNumber));
                unreadCursor.moveToFirst();
                total += unreadCursor.getInt(unreadCursor.getColumnIndex(UNREAD));
                unreadCursor.close();
            }
            return total;
        }

        /**
         * getCursor
         */
        public static Cursor getCursor(Context context, Uri uri) {
            return context.getContentResolver().query(uri, null, null, null, null);
        }

        /**
         * getPendingIntent
         */
        public PendingIntent getPendingIntent() {
            Intent clickIntent = new Intent();
            clickIntent.setAction(ACTION_CLICK);
            PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, clickIntent, 0);
            return pendingIntent;
        }
    }

}