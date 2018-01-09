package com.example.android.mygarden;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.service.PlantWateringService;
import com.example.android.mygarden.ui.MainActivity;
import com.example.android.mygarden.ui.PlantDetailActivity;

/**
 * Implementation of App Widget functionality.
 */
public class PlantWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        PlantWateringService.startActionUpdatePlantWidgets(context);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    /**
     * Updates all plant widgets
     * @param context - The context of the caller
     * @param appWidgetManager - The widget manager
     * @param imgRes - The image to be displayed at the plant pot location
     * @param appWigetIds - The IDs of the widgets
     */
    public static void updatePlantWidgets(Context context, AppWidgetManager appWidgetManager, int imgRes, long plantId, int[] appWigetIds, boolean showWateringButton) {
        for (int widgetID : appWigetIds) {
            updateWidget(context, appWidgetManager, widgetID, imgRes, plantId, showWateringButton);
        }
    }

    /**
     * Registers a onClick event handler to launch the app once the plant is clicked
     * @param context - The context
     * @param appWidgetManager - A class responsible for handling all widgets on the screen
     * @param widgetID - The ID of the current widget being updated
     */
    private static void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetID, int imgRes, long plantId, boolean showWateringButton) {

        // Gets the remote reviews
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.plant_widget);

        // Determines the intent to be launched on tap
        // Will launch the main screen if no valid plantID was passed
        Intent intent;
        if (plantId == PlantContract.INVALID_PLANT_ID) {
            intent = new Intent(context, MainActivity.class);
        } else {
            intent = new Intent(context, PlantDetailActivity.class);
            intent.putExtra(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
        }

        // Pending Intent needed to launch to main activity
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        if (showWateringButton) {
            // Handles the watering intent
            Intent wateringIntent = new Intent(context, PlantWateringService.class);
            wateringIntent.setAction(PlantWateringService.ACTION_WATER_PLANTS);
            wateringIntent.putExtra(PlantWateringService.EXTRA_PLANT_ID, plantId);
            PendingIntent wateringPendingIntent = PendingIntent.getService(
                    context,
                    0,
                    wateringIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            // Sets the pending intent as the intent to be launched once the widget is clicked
            views.setOnClickPendingIntent(R.id.widget_water_button, wateringPendingIntent);

        } else {
            views.setViewVisibility(R.id.widget_water_button, View.INVISIBLE);
        }

        // Sets the pending intent as the intent to be launched once the widget is clicked
        views.setOnClickPendingIntent(R.id.widget_plant_image, pendingIntent);
        views.setImageViewResource(R.id.widget_plant_image, imgRes);
        views.setTextViewText(R.id.widget_plant_id, Long.toString(plantId));

        // Updates the current widget in the iteration
        appWidgetManager.updateAppWidget(widgetID, views);
    }
}

