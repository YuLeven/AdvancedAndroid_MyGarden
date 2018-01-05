package com.example.android.mygarden;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.android.mygarden.service.PlantWateringService;
import com.example.android.mygarden.ui.MainActivity;

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
    public static void updatePlantWidgets(Context context, AppWidgetManager appWidgetManager, int imgRes, int[] appWigetIds) {
        for (int widgetID : appWigetIds) {
            updateWidget(context, appWidgetManager, widgetID, imgRes);
        }
    }

    /**
     * Registers a onClick event handler to launch the app once the plant is clicked
     * @param context - The context
     * @param appWidgetManager - A class responsible for handling all widgets on the screen
     * @param widgetID - The ID of the current widget being updated
     */
    private static void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetID, int imgRes) {
        // Gets the remote reviews
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.plant_widget);

        // Pending Intent needed to launch to main activity
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Handles the watering intent
        Intent wateringItent = new Intent(context, PlantWateringService.class);
        wateringItent.setAction(PlantWateringService.ACTION_WATER_PLANTS);
        PendingIntent wateringPendingIntent = PendingIntent.getService(
                context,
                0,
                wateringItent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Sets the pending intent as the intent to be launched once the widget is clicked
        views.setOnClickPendingIntent(R.id.widget_plant_image, pendingIntent);
        views.setOnClickPendingIntent(R.id.widget_water_button, wateringPendingIntent);

        views.setImageViewResource(R.id.widget_plant_image, imgRes);

        // Updates the current widget in the iteration
        appWidgetManager.updateAppWidget(widgetID, views);
    }
}

