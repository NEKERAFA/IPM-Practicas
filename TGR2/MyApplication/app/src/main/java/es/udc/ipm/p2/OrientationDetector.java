package es.udc.ipm.p2;

import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.WindowManager;

public class OrientationDetector extends OrientationEventListener { //implements SensorEventListener {

    private static final int ORIENTATION_COUNT_RESET_TIME_MS = 1000;
    private static final int ORIENTATION_COUNT_TRIGGER = 4;

    public interface OnOrientationListener {
        void onOrientationChanged();
    }

    private OnOrientationListener listener;
    private long orientationTimestamp;
    private int orientationChangedCount;
    private int oldRotation;
    private Context context;
    private Display display;

    // private final float[] orientationMatrix = new float[16];

    public void setOnOrientationListener(OnOrientationListener listener) {
        this.listener = listener;
    }

    public OrientationDetector(Context context, int rate) {
        super(context, rate);
        this.context = context;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.display = windowManager.getDefaultDisplay();
        this.oldRotation = this.display.getRotation();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("oldRotation", oldRotation);
        savedInstanceState.putLong("orientationTimestamp", orientationTimestamp);
        savedInstanceState.putInt("orientationChangedCount", orientationChangedCount);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        oldRotation = savedInstanceState.getInt("oldRotation");
        orientationTimestamp = savedInstanceState.getLong("orientationTimestamp");
        orientationChangedCount = savedInstanceState.getInt("orientationChangedCount");
    }

    @Override
    public void onOrientationChanged(int orientation) {

        if (listener != null) {
            int rotation = this.display.getRotation();

            if (rotation != oldRotation) {
                long now = System.currentTimeMillis();

                if(orientationTimestamp + ORIENTATION_COUNT_RESET_TIME_MS < now) {
                    orientationChangedCount = 0;
                }

                if(orientationChangedCount == 0) {
                    orientationTimestamp = now;
                }
                oldRotation = rotation;
                orientationChangedCount++;

                if(orientationChangedCount == ORIENTATION_COUNT_TRIGGER) {
                    listener.onOrientationChanged();
                    orientationChangedCount = 0;
                }
            }
        }

    }
}