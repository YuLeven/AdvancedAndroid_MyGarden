package com.example.android.mygarden.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.mygarden.R;
import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.PlantDetailActivity;
import com.example.android.mygarden.utils.PlantUtils;

import static com.example.android.mygarden.provider.PlantContract.BASE_CONTENT_URI;
import static com.example.android.mygarden.provider.PlantContract.PATH_PLANTS;

/**
 * Created by Yuri Levenhagen on 2018-01-09 as part
 * of the Udacity-Google Advanced Android App Development course.
 * <p>
 * The base example code belongs to The Android Open Source Project under the Apache 2.0 licence
 * All code further implemented as part of the course is under the same licence.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class GridWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GridRemoteViewsFactory(this.getApplicationContext());
    }
}

class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private Cursor mCursor;
    private static final String LOG_TAG = GridRemoteViewsFactory.class.getCanonicalName();

    GridRemoteViewsFactory(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "GridRemoteViewsFactory instantiated");
    }

    @Override
    public void onDataSetChanged() {
        if (mCursor != null) mCursor.close();

        mCursor = mContext.getContentResolver().query(
            BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build(),
                null,
                null,
                null,
                PlantContract.PlantEntry.COLUMN_CREATION_TIME
        );
    }

    @Override
    public void onDestroy() {
        mCursor.close();
    }

    @Override
    public int getCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (mCursor == null || mCursor.getCount() == 0) return null;

        mCursor.moveToPosition(position);
        long now = System.currentTimeMillis();
        long wateredAt = mCursor.getLong(mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME));
        long createdAt = mCursor.getLong(mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME));
        int plantType = mCursor.getInt(mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE));
        long plantId = mCursor.getLong(mCursor.getColumnIndex(PlantContract.PlantEntry._ID));

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.plant_widget);

        // Update the plant image
        int imgRes = PlantUtils.getPlantImageRes(
                mContext,
                now - createdAt,
                now - wateredAt,
                plantType
        );

        views.setImageViewResource(R.id.widget_plant_image, imgRes);
        views.setTextViewText(R.id.widget_plant_id, Long.toString(plantId));
        // Hide the watter button in GridView mode
        views.setViewVisibility(R.id.widget_water_button, View.GONE);

        // Fill in the template to handle each plant individually
        Bundle extras = new Bundle();
        extras.putLong(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        views.setOnClickFillInIntent(R.id.widget_plant_image, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        //All types are the sime
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
