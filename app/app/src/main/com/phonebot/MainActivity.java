package main.com.phonebot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import phonebot.control.JointKinematicState;
import phonebot.control.PIDController;
import phonebot.control.PhonebotJointKinematicSolver;
import phonebot.control.PhonebotKinematicConfiguration;

public class MainActivity extends AppCompatActivity {


    private static final String[] allPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestRequiredPermissions();
        testKinematics();
    }

    private void requestRequiredPermissions() {

        for (int i = 0; i < allPermissions.length; i++) {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    allPermissions[i])
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                // Should we show an explanation?
                if (this.shouldShowRequestPermissionRationale(
                        allPermissions[i])) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    // TODO: Write the explanation showing dialog
                } else {
                    // No explanation needed; request the permission
                    this.requestPermissions(new String[]{allPermissions[i]}, i);
                }
            } else {
                // Permission has already been granted
            }
        }
    }

    private void testKinematics() {
        PhonebotKinematicConfiguration config = new PhonebotKinematicConfiguration();
        JointKinematicState state = new JointKinematicState();

        config.setFemur_length_(0.0110f);
        config.setTibia_length_(0.0175f);
        config.setJoint_displacement_(0.0285f);

        state.setActive_front(1.404f);
        state.setActive_back(1.404f);

        state.setPassive_front(-2.46559f);
        state.setPassive_back(-2.46559f);

        state.setEndpoint_x(0.00000004f);
        state.setEndpoint_y(0.042138f);


        // other things don't matter right now
        // config.setBody_height_(0.0f);

        PhonebotJointKinematicSolver solver = new PhonebotJointKinematicSolver(config);
        solver.Reset(state);

        float[] a = new float[1];
        float[] b = new float[1];

        solver.SolveActive(0.0f, 0.032f, a, b);
        Log.i("ASolve", "active [" + String.valueOf(a[0]) + "," + String.valueOf(b[0]) + "]");
    }

    private void testPID() {
        final float k_p = 0.8f;
        final float k_i = 0.023f;
        final float k_d = 5.0f;
        final float dt = 0.1f;

        PIDController pid = new PIDController(k_p, k_i, k_d, 100.0f, 100.0f);

        float x = 0.0f;
        float v = 0.0f;
        float err = 0.0f;
        float t = 0.0f;
        while (t < 10.0f) {
            final float y = (t < 1.0f ? 0.0f : 1.0f);
            final float a = pid.Control(y - x, dt);
            err += (y - x) * (y - x);
            x += v * dt;
            v += a * dt;
            Log.i("pid", String.valueOf(x));
            t += dt;
        }
    }

    public void openConnection(View view) {
//        Used to link to Connection activity. TODO: Rebuild/refactor into Connection again
        Intent intent = new Intent(MainActivity.this, DeviceScanActivity.class);
        startActivity(intent);
    }

    public void openIDE(View view) {
        Intent intent = new Intent(MainActivity.this, IDE.class);
        startActivity(intent);
    }

    public void openOperation(View view) {
        Intent intent = new Intent(MainActivity.this, Operation.class);
        startActivity(intent);
    }

    public void openHacks(View view) {
        Intent intent = new Intent(MainActivity.this, Hacks.class);
        startActivity(intent);
    }

    public void openFDActivity(View view) {
        Intent intent = new Intent(MainActivity.this, FdActivity.class);
        startActivity(intent);
    }

    public void openLegs(View view) {
        Intent intent = new Intent(MainActivity.this, InteractiveLegActivity.class);
        startActivity(intent);
    }


    public void openSensors(View view) {
        Intent intent = new Intent(MainActivity.this, SensorsActivity.class);
        startActivity(intent);
    }

    public void openCamera(View view) {
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                // User chose the "Settings" item, show the app settings UI...
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

}
