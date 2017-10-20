package es.udc.ipm.p2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeDetector implements SensorEventListener {

    /*
     * Fuerza gravitatoria que se necesita para registrar una sacudida.
     * Debe de ser más grande que 1G (Una unidad gravitatoria terrestre)
     */
    private static final float SHAKE_THRESHOLD_GRAVITY = 1.8F;
    private static final int SHAKE_SLOP_TIME_MS = 500;

    // Interfaz les observador
    public interface OnShakeListener {
        void onShake();
    }

    private OnShakeListener listener;
    private long shakeTimestamp;

    public void setOnShakeListener(OnShakeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Se ignora
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (listener != null) {
            // Se hace la comprobación de que el evento sea del acelerómetro
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                double gX = x / SensorManager.GRAVITY_EARTH;
                double gY = y / SensorManager.GRAVITY_EARTH;
                double gZ = z / SensorManager.GRAVITY_EARTH;

                // La fuerza gravitatoria (gForce) será cercana a 1 cuando no haya movimiento
                float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

                if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                    final long now = System.currentTimeMillis();
                    // Se ignoran sacudidas muy cercanas (500ms)
                    if (shakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                        return;
                    }

                    shakeTimestamp = now;
                    listener.onShake();
                }
            }

        }

    }
}
