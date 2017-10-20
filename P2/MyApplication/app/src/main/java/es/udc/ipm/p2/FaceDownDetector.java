package es.udc.ipm.p2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;

public class FaceDownDetector implements SensorEventListener {

    private final static int FACE_DOWN_TIME_LOW = 1000;     // En milisegundos
    private final static int FACE_DOWN_TIME_HIGH = 2000;
    private Long timestamp = null;
    private Float oldGravZ = null;
    private OnFaceDownListener listener;

    public interface OnFaceDownListener {
        void onFaceDown();
    }

    public void setOnFaceDownListener(OnFaceDownListener listener) {
        this.listener = listener;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Se ignora
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(listener != null) {
            // Se comprueba de donde viene el evento
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // Se obtiene el eje Z
                float gravZ = event.values[2];

                // Comprobamos si está bocarriba por primera vez
                if ((oldGravZ == null) && (gravZ > 0)) {
                    oldGravZ = gravZ;
                // Comprobamos si está bocabajo tras haber estado bocarriba
                } else if ((oldGravZ != null) && (gravZ < 0) && (timestamp == null)) {
                    oldGravZ = gravZ;
                    timestamp = System.currentTimeMillis();
                // Comprobamos si el dispositivo vuelve a estar bocarriba (fin de ciclo)
                } else if ((oldGravZ != null) && (timestamp != null) && (gravZ > 0)) {
                    oldGravZ = gravZ;
                    // Comprobamos si ha pasado un segundo o más
                    long now = System.currentTimeMillis();
                    if ((timestamp + FACE_DOWN_TIME_LOW <= now) &&
                        (timestamp + FACE_DOWN_TIME_HIGH >= now)) {
                        listener.onFaceDown();
                    }
                    timestamp = null; // Nuevo inicio de ciclo
                }
            }
        }
    }

    // Guardamos el timestamp y oldGravZ para que no se pierdan si cambia la orientación en el proceso
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (timestamp != null) {
            savedInstanceState.putLong("timestamp", timestamp);
        }
        if (oldGravZ != null) {
            savedInstanceState.putFloat("oldGravZ", oldGravZ);
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Usamos los valores por defecto para saber si anteriormente los atributos eran nulos
        // (ya que si este es el caso, no se habrán guardado)
        oldGravZ = savedInstanceState.getFloat("oldGravZ", 50);
        timestamp = savedInstanceState.getLong("timestamp", 0);
        if (oldGravZ == 50) {
            oldGravZ = null;
        }
        if (timestamp == 0) {
            timestamp = null;
        }
    }
}
