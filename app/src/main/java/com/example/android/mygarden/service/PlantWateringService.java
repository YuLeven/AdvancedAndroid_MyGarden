package com.example.android.mygarden.service;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.example.android.mygarden.PlantWidgetProvider;
import com.example.android.mygarden.R;
import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.utils.PlantUtils;

import static com.example.android.mygarden.provider.PlantContract.BASE_CONTENT_URI;
import static com.example.android.mygarden.provider.PlantContract.INVALID_PLANT_ID;
import static com.example.android.mygarden.provider.PlantContract.PATH_PLANTS;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class PlantWateringService extends IntentService {

    public static final String ACTION_WATER_PLANTS = "com.example.android.mygarden.service.action.water_plants";
    public static final String ACTION_UPDATE_PLANT_WIDGET = "com.example.android.mygarden.service.action.update_plant_widgets";
    public static final String EXTRA_PLANT_ID = "com.example.andorid.mygarden.service.identifier.plant";

    public PlantWateringService() {
        super("PlantWateringService");
    }

    /**
     * Starts this service to perform action water plants. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionWaterPlants(Context context, long plantId) {
        Intent intent = new Intent(context, PlantWateringService.class);
        intent.setAction(ACTION_WATER_PLANTS);
        intent.putExtra(EXTRA_PLANT_ID, plantId);
        context.startService(intent);
    }

    /**
     * Starts this service to perform the update plant action
     * @param context - The context of the caller
     */
    public static void startActionUpdatePlantWidgets(Context context) {
        Intent intent = new Intent(context, PlantWateringService.class);
        intent.setAction(ACTION_UPDATE_PLANT_WIDGET);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            final String action = intent.getAction();
            if (action == null) return;

            switch (action) {
                case ACTION_WATER_PLANTS: handleActionWaterPlant(intent.getLongExtra(EXTRA_PLANT_ID, INVALID_PLANT_ID));
                case ACTION_UPDATE_PLANT_WIDGET: handleActionUpdatePlants();
            }
        }
    }

    /**
     * Water plants in a background thread (just imagine how nice must be in there!)
     */
    private void handleActionWaterPlant(long plantId) {

        Uri PLANT_URI = ContentUris.withAppendedId(BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_PLANTS).build(), plantId);

        // Gets the current time
        long timeNow = System.currentTimeMillis();

        // Stores the current time on a content values to be passed to the plant
        // watering utility class
        ContentValues contentValues = new ContentValues();
        contentValues.put(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME, timeNow);

        // Update the passed plant (by its ID)
        getContentResolver().update(
                PLANT_URI,
                contentValues,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME+">?",
                new String[]{String.valueOf(timeNow - PlantUtils.MAX_AGE_WITHOUT_WATER)});

        // Updates the widget
        startActionUpdatePlantWidgets(this);
    }

    private void handleActionUpdatePlants() {

        Cursor cursor = null;

        try {
            // Query the plant with needs water the most
            cursor = getContentResolver().query(
                    buildPlantsURI(),
                    null,
                    null,
                    null,
                    PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME
            );

            // Gets the plant details
            int imgRes = R.drawable.grass;
            long plantId = INVALID_PLANT_ID;
            boolean canWater = false;

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                long now = System.currentTimeMillis();
                long wateredAt = cursor.getLong(cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME));
                long createdAt = cursor.getLong(cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME));
                int plantType = cursor.getInt(cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE));
                plantId = cursor.getLong(cursor.getColumnIndex(PlantContract.PlantEntry._ID));
                canWater = (now - wateredAt) > PlantUtils.MIN_AGE_BETWEEN_WATER
                                && (now - wateredAt) < PlantUtils.MAX_AGE_WITHOUT_WATER;

                imgRes = PlantUtils.getPlantImageRes(
                        this,
                        now - createdAt,
                        now - wateredAt,
                        plantType
                );
            }

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, PlantWidgetProvider.class));
            PlantWidgetProvider.updatePlantWidgets(this, appWidgetManager, imgRes, plantId, appWidgetIds, canWater);

            // Notifies the updates the GridView
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_grid_view);

        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    /**
     * Returns base plants URI
     */
    private Uri buildPlantsURI() {
        return BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();
    }


}
