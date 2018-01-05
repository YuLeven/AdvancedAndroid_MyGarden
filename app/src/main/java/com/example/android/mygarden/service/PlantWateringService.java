package com.example.android.mygarden.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;

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

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && ACTION_WATER_PLANTS.equals(intent.getAction())) {
            handleActionWaterPlants();
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
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build(),
                contentValues,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME+">?",
                new String[]{String.valueOf(timeNow - PlantUtils.MAX_AGE_WITHOUT_WATER)});
    }
}
