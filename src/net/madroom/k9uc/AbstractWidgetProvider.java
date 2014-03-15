package net.madroom.k9uc;

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
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractWidgetProvider extends AppWidgetProvider
{
    @Override
    public void onEnabled(Context context)
    {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        context.startService(new Intent(context, MyService.class));
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context)
    {
        super.onDisabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);
    }

    public static abstract class MyService extends Service
    {
        protected abstract void initialize();
        protected abstract int getWidgetCount();
        protected abstract void setRemoteViews();

        protected Context mContext;
        protected SharedPreferences mPref;
        protected ComponentName mComponentName;
        protected AppWidgetManager mManager;
        protected RemoteViews mRemoteViews;
        protected String mActionClick;

        protected int mColorZero;
        protected int mColorNotZero;
        protected int mColorBG;

        @Override
        public IBinder onBind(Intent intent)
        {
            return null;
        }

        @Override
        public void onCreate()
        {
            mContext = this;
            mPref = PreferenceManager.getDefaultSharedPreferences(mContext);
            mManager = AppWidgetManager.getInstance(mContext);

            mColorZero = Color.argb(
                    mPref.getInt(MainActivity.KEY_TEXT_ZERO_COLOR_A, MainActivity.DEF_TEXT_ZERO_COLOR_A),
                    mPref.getInt(MainActivity.KEY_TEXT_ZERO_COLOR_R, MainActivity.DEF_TEXT_ZERO_COLOR_R),
                    mPref.getInt(MainActivity.KEY_TEXT_ZERO_COLOR_G, MainActivity.DEF_TEXT_ZERO_COLOR_G),
                    mPref.getInt(MainActivity.KEY_TEXT_ZERO_COLOR_B, MainActivity.DEF_TEXT_ZERO_COLOR_B));

            mColorNotZero = Color.argb(
                    mPref.getInt(MainActivity.KEY_TEXT_NOT_ZERO_COLOR_A, MainActivity.DEF_TEXT_NOT_ZERO_COLOR_A),
                    mPref.getInt(MainActivity.KEY_TEXT_NOT_ZERO_COLOR_R, MainActivity.DEF_TEXT_NOT_ZERO_COLOR_R),
                    mPref.getInt(MainActivity.KEY_TEXT_NOT_ZERO_COLOR_G, MainActivity.DEF_TEXT_NOT_ZERO_COLOR_G),
                    mPref.getInt(MainActivity.KEY_TEXT_NOT_ZERO_COLOR_B, MainActivity.DEF_TEXT_NOT_ZERO_COLOR_B));

            mColorBG = Color.argb(
                    mPref.getInt(MainActivity.KEY_BG_COLOR_A, MainActivity.DEF_BG_COLOR_A),
                    mPref.getInt(MainActivity.KEY_BG_COLOR_R, MainActivity.DEF_BG_COLOR_R),
                    mPref.getInt(MainActivity.KEY_BG_COLOR_G, MainActivity.DEF_BG_COLOR_G),
                    mPref.getInt(MainActivity.KEY_BG_COLOR_B, MainActivity.DEF_BG_COLOR_B));

            initialize();
        }

        @Override
        public void onStart(Intent intent, int startId)
        {
            if (getWidgetCount() == 0) {
                return;
            }

            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(mActionClick)) {
                    final Intent i = new Intent(mContext, TranslucentActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    stopSelf();
                    return;
                }
            }

            // For Crash error
            try {
                setK9Data();
                setRemoteViews();
                mManager.updateAppWidget(mComponentName, mRemoteViews);
            } catch (Exception e) {
                stopSelf();
            }

            stopSelf();
        }

        protected static final Uri K9_ACCOUNTS_URI =
                Uri.parse("content://com.fsck.k9.messageprovider/accounts/");

        protected static final Uri K9_ACCOUNT_UNREAD_URI =
                Uri.parse("content://com.fsck.k9.messageprovider/account_unread/");

        protected static final String ACCOUNT_NUMBER = "accountNumber";
        protected static final String ACCOUNT_NAME = "accountName";
        protected static final String ACCOUNT_UUID = "accountUuid";
        protected static final String ACCOUNT_COLOR = "accountColor";
        protected static final String UNREAD = "unread";

        protected final List<Integer> mAccountNumbers = new ArrayList<Integer>();
        protected final List<String> mAccountNames = new ArrayList<String>();
        protected final List<String> mAccountUuids = new ArrayList<String>();
        protected final List<Integer> mAccountColors = new ArrayList<Integer>();
        protected final List<Integer> mUnreads = new ArrayList<Integer>();
        protected int mTotalUnread = 0;

        protected void setK9Data()
        {
            final String[] projection =
                    new String[] {ACCOUNT_NUMBER, ACCOUNT_NAME, ACCOUNT_UUID, ACCOUNT_COLOR};
            final Cursor accountCursor =
                    mContext.getContentResolver().query(K9_ACCOUNTS_URI, projection, null, null, null);

            if (accountCursor.moveToFirst()) {
                do {
                    mAccountNumbers.add(
                            accountCursor.getInt(accountCursor.getColumnIndex(ACCOUNT_NUMBER)));
                    mAccountNames.add(
                            accountCursor.getString(accountCursor.getColumnIndex(ACCOUNT_NAME)));
                    mAccountUuids.add(
                            accountCursor.getString(accountCursor.getColumnIndex(ACCOUNT_UUID)));

                    mAccountColors.add(
                            accountCursor.getInt(accountCursor.getColumnIndex(ACCOUNT_COLOR)));

                } while (accountCursor.moveToNext());
            }

            accountCursor.close();

            for (int accountNumber : mAccountNumbers) {
                final Uri uri = ContentUris.withAppendedId(K9_ACCOUNT_UNREAD_URI, accountNumber);
                final Cursor unreadCursor =
                        mContext.getContentResolver().query(uri, null, null, null, null);

                unreadCursor.moveToFirst();

                final int unread = unreadCursor.getInt(unreadCursor.getColumnIndex(UNREAD));
                mUnreads.add(unread);
                mTotalUnread += unread;

                unreadCursor.close();
            }
        }
    }
}