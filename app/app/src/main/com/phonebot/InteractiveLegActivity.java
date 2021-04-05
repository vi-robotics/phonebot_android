package main.com.phonebot;

import android.app.ActivityManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class InteractiveLegActivity extends AppCompatActivity implements LegInterfaceFragment.OnListFragmentInteractionListener {

    private BluetoothLeService mBluetoothLeService;
    private Boolean isBluetoothRunning;
    private Boolean dialogShown = false;

    FragmentManager manager = getFragmentManager();
    FragmentTransaction transaction = manager.beginTransaction();


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isServiceRunning(BluetoothLeService.class)) {
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            isBluetoothRunning = true;
        } else {
            isBluetoothRunning = false;
        }

        setContentView(R.layout.activity_legs);
    }

    @Override
    public void ListFragmentInteraction(LegViewData.LegItem item) {
        byte[] bArr = new byte[8];
        for (int j = 0; j < LegViewData.ITEMS.size(); ++j) {
            bArr[j] = (byte) LegViewData.ITEMS.get(j).legValue;
        }

        if (isBluetoothRunning) {
            mBluetoothLeService.sendData(PhoneBotCommandEncoder.encodeCommand(PhoneBotCommandEncoder.PhoneBotCommand.SET_LEG_POSITIONS, bArr));
        } else if (!dialogShown) {
            // Ble not available, show user dialog to potentially scan and connect to Ble Service
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage(R.string.phonebot_not_connected_message)
                    .setTitle(R.string.phonebot_not_connected_title);

            builder.setPositiveButton(R.string.phonebot_not_connected_accept, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(InteractiveLegActivity.this, DeviceScanActivity.class);
                    startActivity(intent);

                }
            });
            builder.setNegativeButton(R.string.phonebot_not_connected_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialogShown = true;
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    dialogShown = true;
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
