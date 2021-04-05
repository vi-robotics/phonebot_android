package main.com.phonebot;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

public final class OrientationListener implements SensorEventListener {
    private final boolean mFuse;
    private SensorManager sensorManager;

    // Raw sensor values.
    private final float[] accValue = new float[3];
    private final float[] magValue = new float[3];
    private final float[] rotationVector = new float[5];

    // NOTE(yycho0108): Stamps are provided at nanosecond resolution.
    private long accStamp = 0;
    private long magStamp = 0;
    private long rvecStamp = 0;

    // Derived sensor values.
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private boolean isOrientationAngleStale = true;

    /**
     * @param context The application context.
     * @param fuse    Whether to use smoothed (fused) value from android rotation vector.
     */
    public OrientationListener(Context context, final boolean fuse) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mFuse = fuse;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    protected void registerListener() {
        if (mFuse) {
            Sensor rotationVector = sensorManager.getDefaultSensor(
                    Sensor.TYPE_ROTATION_VECTOR);
            // TODO(yycho0108): Configure update/callback delays.
            sensorManager.registerListener(this, rotationVector,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        } else {
            // Get updates from the accelerometer and magnetometer at a constant rate.
            // To make batch operations more efficient and reduce power consumption,
            // provide support for delaying updates to the application.
            //
            // In this example, the sensor reporting delay is small enough such that
            // the application receives an update before the system checks the sensor
            // readings again.
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer,
                        SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
            }
            Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            if (magneticField != null) {
                sensorManager.registerListener(this, magneticField,
                        SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    protected void unregisterListener() {
        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this);
    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO(yycho0108): Consider using fused sensor instead:
        //        Sensor.TYPE_ROTATION_VECTOR
        switch(event.sensor.getType()){
            case Sensor.TYPE_ROTATION_VECTOR:{
                System.arraycopy(event.values, 0, rotationVector,
                        0, rotationVector.length);
                rvecStamp = event.timestamp;
                isOrientationAngleStale = true;
                break;
            }
            case Sensor.TYPE_ACCELEROMETER:{
                System.arraycopy(event.values, 0, accValue,
                        0, accValue.length);
                accStamp = event.timestamp;
                isOrientationAngleStale = true;
                break;
            }
            case Sensor.TYPE_MAGNETIC_FIELD:{
                System.arraycopy(event.values, 0, magValue,
                        0, magValue.length);
                magStamp = event.timestamp;
                isOrientationAngleStale = true;
                break;
            }
            default:{
                break;
            }
        }
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {
        if (mFuse) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);
        } else {
            // Update rotation matrix, which is needed to update orientation angles.
            SensorManager.getRotationMatrix(rotationMatrix, null,
                    accValue, magValue);
        }
        // "mRotationMatrix" now has up-to-date information.
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
        // "mOrientationAngles" now has up-to-date information.
    }

    /**
     * Get fused orientation angles from sensor measurements.
     * NOTE(yycho0108): Consider if the orientation signs need frame conversion.
     *
     * @param angles Orientation angles about {-z,x,y} axes, in radians.
     * @return The elapsed time since last measurement.
     */
    public long getOrientationAngles(final float[] angles) {
        // Lazily update the orientation angle.
        if (isOrientationAngleStale) {
            updateOrientationAngles();
            isOrientationAngleStale = false;
        }

        System.arraycopy(orientationAngles, 0, angles, 0, orientationAngles.length);
        final long now = SystemClock.elapsedRealtimeNanos();
        return mFuse ? (now - rvecStamp) : (Math.max(now - accStamp, now - magStamp));
    }
}