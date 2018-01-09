package com.example.android.mygarden;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.service.GridWidgetService;
import com.example.android.mygarden.service.PlantWateringService;
import com.example.android.mygarden.ui.MainActivity;
import com.example.android.mygarden.ui.PlantDetailActivity;

/**
 * Implementation of App Widget functionality.
 */
public class PlantWidgetProvider extends AppWidgetProvider {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int imgRes, long plantId, boolean showWater, int widgetID) {

        Bundle options = appWidgetManager.getAppWidgetOptions(widgetID);
        int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        RemoteViews views;

        if (width < 300) {
            views = getSinglePlantRemoteView(context, widgetID, imgRes, plantId, showWater);
        } else {
            views = getGardenGridRemoteView(context);
        }

        // Updates the current widget in the iteration
        appWidgetManager.updateAppWidget(widgetID, views);
    }

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

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        PlantWateringService.startActionUpdatePlantWidgets(context);
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
            updateAppWidget(
                    context,
                    appWidgetManager,
                    imgRes,
                    plantId,
                    showWateringButton,
                    widgetID
            );
        }
    }

    /**
     * Registers a onClick event handler to launch the app once the plant is clicked
     * @param context - The context
     * @param widgetID - The ID of the current widget being updated
     */
    private static RemoteViews getSinglePlantRemoteView(Context context, int widgetID, int imgRes, long plantId, boolean showWateringButton) {

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

        return views;
    }

    /**
     * Creates a GridView to be displayed on the widget
     * @param context - The context of the caller
     * @return - A RemoteView containing the GridView
     */
    private static RemoteViews getGardenGridRemoteView(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_grid_view);

        // Binds the views to the service
        Intent intent = new Intent(context, GridWidgetService.class);
        views.setRemoteAdapter(R.id.widget_grid_view, intent);

        // Set the PlantDetailActivity intent to launch when clicked
        Intent appIntent = new Intent(context, PlantDetailActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_grid_view, appPendingIntent);
        views.setEmptyView(R.id.widget_grid_view, R.id.empty_view);

        return views;
    }
}

