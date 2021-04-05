package main.com.phonebot;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;


public class SensorsActivity extends AppCompatActivity implements SensorEventListener {
    // TODO(yycho0108): Refactor this class.

//    private float mLastX, mLastY, mLastZ;
//    private boolean mInitialized;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetic;
    private Sensor mGyroscope;
    private Sensor mHumidity;
    private Sensor mTemperature;
    private Sensor mLight;
    private Sensor mPressure;
    private Sensor mProximity;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);
//        mInitialized = false;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mHumidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        mSensorManager.registerListener(this, mAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetic ,      SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope,      SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mHumidity,       SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mTemperature ,   SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLight ,         SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mPressure,       SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mProximity,      SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer,  SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetic,       SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope,      SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mHumidity,       SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mTemperature ,   SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLight ,         SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mPressure,       SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mProximity,      SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // can be safely ignored for this demo
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            TextView tvX= findViewById(R.id.x_axis_mag);
            TextView tvY= findViewById(R.id.y_axis_mag);
            TextView tvZ= findViewById(R.id.z_axis_mag);

            float[] magnetic = event.values;
            tvX.setText(String.format(Locale.US, "%.2f", magnetic[0]));
            tvY.setText(String.format(Locale.US, "%.2f", magnetic[1]));
            tvZ.setText(String.format(Locale.US, "%.2f", magnetic[2]));
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            TextView tvX= findViewById(R.id.x_axis_acc);
            TextView tvY= findViewById(R.id.y_axis_acc);
            TextView tvZ= findViewById(R.id.z_axis_acc);
            float[] acceleration = event.values;

            tvX.setText(String.format(Locale.US, "%.3f", acceleration[0]));
            tvY.setText(String.format(Locale.US, "%.3f", acceleration[1]));
            tvZ.setText(String.format(Locale.US, "%.3f", acceleration[2]));
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            TextView tvX= findViewById(R.id.x_axis_gyr);
            TextView tvY= findViewById(R.id.y_axis_gyr);
            TextView tvZ= findViewById(R.id.z_axis_gyr);
            float[] rotation = event.values;

            tvX.setText(String.format(Locale.US, "%.5f", rotation[0]));
            tvY.setText(String.format(Locale.US, "%.5f", rotation[1]));
            tvZ.setText(String.format(Locale.US, "%.5f", rotation[2]));
        }
        if (event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
            TextView tvHum= findViewById(R.id.hum_dat);
            tvHum.setText(String.format(Locale.US, "%.5f", event.values[0]));
        }


        if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            TextView tvTemp= findViewById(R.id.amb_temp_dat);
            tvTemp.setText(String.format(Locale.US, "%.5f", event.values[0]));
        }
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            TextView tvHum= findViewById(R.id.light_dat);
            tvHum.setText(String.format(Locale.US, "%.5f", event.values[0]));
        }
        if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            TextView tvHum= findViewById(R.id.pressure_dat);
            tvHum.setText(String.format(Locale.US, "%.5f", event.values[0]));
        }
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            TextView tvHum= findViewById(R.id.proximity_dat);
            tvHum.setText(String.format(Locale.US, "%.5f", event.values[0]));
        }

    }
}