package net.madroom.k9uc;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.widget.RemoteViews;

public class Widget1x2Provider extends AbstractWidgetProvider
{
    public static class MyService extends AbstractWidgetProvider.MyService
    {
        protected void initialize()
        {
            mComponentName = new ComponentName(mContext, Widget1x2Provider.class);
            mRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget1x2_layout_translucent);
            mActionClick = "net.madroom.k9uc.action.CLICK1x2";
        }

        protected int getWidgetCount()
        {
            return AppWidgetManager.getInstance(mContext).getAppWidgetIds(new ComponentName(mContext, Widget1x2Provider.class)).length;
        }

        protected void setRemoteViews()
        {
            mRemoteViews.setOnClickPendingIntent(R.id.base_layout,
                    PendingIntent.getService(mContext, 0, new Intent().setAction(mActionClick), 0));

            mRemoteViews.setInt(R.id.base_layout, "setBackgroundColor", mColorBG);

            final String text = mTotalUnread == 1 ? "New Email" : "New Emails";
            mRemoteViews.setTextViewText(R.id.unread_count, mTotalUnread + " " + text);
            mRemoteViews.setTextColor(R.id.unread_count, mTotalUnread == 0 ? mColorZero : mColorNotZero);

            final int[] textIds = {R.id.count_text1, R.id.count_text2, R.id.count_text3, R.id.count_text4};
            final int max = Math.min(mAccountNumbers.size(), 4);
            for (int i = 0; i < max; i++) {
                mRemoteViews.setTextViewText(textIds[i], Html.fromHtml(mUnreads.get(i) + "<br />" + mAccountNames.get(i)));
                mRemoteViews.setTextColor(textIds[i], Color.rgb(0, 0, 0));
                mRemoteViews.setInt(textIds[i], "setBackgroundColor", mAccountColors.get(i));
            }
        }
    }
}