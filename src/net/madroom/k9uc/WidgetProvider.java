package net.madroom.k9uc;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class WidgetProvider extends AbstractWidgetProvider
{
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        context.startService(new Intent(context, MyService.class));
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public static class MyService extends AbstractWidgetProvider.MyService
    {
        protected void initialize()
        {
            mComponentName = new ComponentName(mContext, WidgetProvider.class);
            mRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_layout_translucent);
            mActionClick = "net.madroom.k9uc.action.CLICK";
        }

        protected int getWidgetCount()
        {
            return AppWidgetManager.getInstance(mContext).getAppWidgetIds(new ComponentName(mContext, WidgetProvider.class)).length;
        }

        protected void setRemoteViews()
        {
            mRemoteViews.setOnClickPendingIntent(R.id.base_layout,
                    PendingIntent.getService(mContext, 0, new Intent().setAction(mActionClick), 0));

            mRemoteViews.setInt(R.id.base_layout, "setBackgroundColor", mColorBG);

            mRemoteViews.setTextViewText(R.id.count_text, mTotalUnread + "");
            mRemoteViews.setTextColor(R.id.count_text, mTotalUnread == 0 ? mColorZero : mColorNotZero);
        }
    }

}