package com.example.android.mygarden.service;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
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

    public PlantWateringService() {
        super("PlantWateringService");
    }

    /**
     * Starts this service to perform action water plants. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionWaterPlants(Context context) {
        Intent intent = new Intent(context, PlantWateringService.class);
        intent.setAction(ACTION_WATER_PLANTS);
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
            switch (intent.getAction()) {
                case ACTION_WATER_PLANTS: handleActionWaterPlants();
                case ACTION_UPDATE_PLANT_WIDGET: handleActionUpdatePlants();
            }
        }
    }

    /**
     * Water plants in a background thread (just imagine how nice must be in there!)
     */
    private void handleActionWaterPlants() {

        // Gets the current time
        long timeNow = System.currentTimeMillis();

        // Stores the current time on a content values to be passed to the plant
        // watering utility class
        ContentValues contentValues = new ContentValues();
        contentValues.put(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME, timeNow);

        // Update only plants that are still alive
        getContentResolver().update(
                buildPlantsURI(),
                contentValues,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME+">?",
                new String[]{String.valueOf(timeNow - PlantUtils.MAX_AGE_WITHOUT_WATER)});
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

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                long now = System.currentTimeMillis();
                long wateredAt = cursor.getLong(cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME));
                long createdAt = cursor.getLong(cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME));
                int plantType = cursor.getInt(cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE));

                imgRes = PlantUtils.getPlantImageRes(
                        this,
                        now - createdAt,
                        now - wateredAt,
                        plantType
                );
            }

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, PlantWidgetProvider.class));
            PlantWidgetProvider.updatePlantWidgets(this, appWidgetManager, imgRes, appWidgetIds);

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
