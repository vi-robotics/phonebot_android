/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package main.com.phonebot;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {


    /**
     * Simple wrapper around connection state constants.
     */
    public enum ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    /**
     * Local binder to directly provide service instance.
     * TODO(yycho0108): Consider better alternatives.
     */
    public static class LocalBinder extends Binder {
        private BluetoothLeService mService = null;

        public LocalBinder(BluetoothLeService service) {
            mService = service;
        }

        BluetoothLeService getService() {
            return mService;
        }
    }

    /**
     * Local connection object to directly provide service instance.
     * TODO(yycho0108): Consider better alternatives.
     */
    public static class LocalConnection implements ServiceConnection {
        private String mDeviceAddress = null;
        private BluetoothLeService mService = null;


        public LocalConnection(final String address) {
            mDeviceAddress = address;
        }

        BluetoothLeService getService() {
            return mService;
        }

        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
            // Automatically connects to the device upon successful start-up initialization.
            mService.connect(mDeviceAddress);
            // FIXME(yycho0108): Attempt reconnect upon failure?
            // https://stackoverflow.com/questions/30069577/android-reconnect-to-bluetooth-device-if-connection-lost
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    }

    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private ConnectionState mConnectionState = ConnectionState.DISCONNECTED;
    private BluetoothGattService uartService;
    private BluetoothGattCharacteristic rxChar;
    private Long lastCommandSent = System.currentTimeMillis();
    private Integer minMessageDelay = 50; // Time in milliseconds


    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED: {
                    intentAction = BleConstants.ACTION_GATT_CONNECTED;
                    mConnectionState = ConnectionState.CONNECTED;
                    broadcastUpdate(intentAction);
                    Log.i(TAG, "Connected to GATT server.");
                    // Attempts to discover services after successful connection.
                    Log.i(TAG, "Attempting to start service discovery:" +
                            mBluetoothGatt.discoverServices());
                    break;
                }

                case BluetoothProfile.STATE_DISCONNECTED: {
                    intentAction = BleConstants.ACTION_GATT_DISCONNECTED;
                    mConnectionState = ConnectionState.DISCONNECTED;
                    Log.i(TAG, "Disconnected from GATT server.");
                    broadcastUpdate(intentAction);
                    break;
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Open the transparent uart stream when services are discovered
                openStream();
                broadcastUpdate(BleConstants.ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Reading Characteristic Successful.");
                broadcastUpdate(BleConstants.ACTION_DATA_AVAILABLE, characteristic);
            } else {
                Log.i(TAG, "Failed to read characteristic.");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(BleConstants.ACTION_DATA_AVAILABLE, characteristic);
        }
    };


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for transparent UART TX.

        if (characteristic.getUuid().equals(BleConstants.UUID_TRANSPARENT_UART_TX)) {
            // TODO(Max): Figure out how to read these broadcasts in a reasonable way
            intent.putExtra(BleConstants.EXTRA_DATA, characteristic.getValue());
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(BleConstants.EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }


    private final IBinder mBinder = new LocalBinder(this);

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }


    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = ConnectionState.CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = ConnectionState.CONNECTING;
        return true;
    }

    /**
     * Opens the Transparent UART service stream by sending Notify on the Tx descriptor. The Tx characteristic
     * is that which the peripheral device (PhoneBot) sends data. By setting "Notify" here, we're saying
     * that we want to be notified when PhoneBot has new data. Thus, we "set" this characteristic in
     * order to open the stream.
     */
    public void openStream() {
        uartService = mBluetoothGatt.getService(BleConstants.UUID_TRANSPARENT_UART_SERVICE);
        if (uartService == null) {
            Log.e(TAG, "UART service not found!");
            return;
        }
        rxChar = uartService.getCharacteristic(BleConstants.UUID_TRANSPARENT_UART_RX);
        BluetoothGattCharacteristic txChar = uartService.getCharacteristic(BleConstants.UUID_TRANSPARENT_UART_TX);

        BluetoothGattDescriptor descriptor = txChar.getDescriptor(
                UUID.fromString(PhoneBotAttributes.TX_CHARACTERISTIC_CONFIGURATION));

        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
        Log.i(TAG, "Open Stream Complete");
    }

    /**
     * Sends a byte array over the transparent UART BLE service. If called before minMessageDelay
     * is elapsed, then the message will not be sent.
     *
     * @param value The bytes to send. Note: These bytes should be encoded by
     *              {@code PhoneBotCommandEncoder.encodeCommand(PhoneBotCommandEncoder.PhoneBotCommand, byte[])}
     *              in order to be properly parsable by the PhoneBot firmware.
     */
    public void sendData(byte[] value) {


        if (uartService == null) {
            Log.e(TAG, "UART service not found!");
            return;
        }
        if (rxChar == null) {
            Log.e(TAG, "Rx characteristic not found!");
            return;
        }

        if (System.currentTimeMillis() - lastCommandSent < minMessageDelay) {
            return;
        }

        rxChar.setValue(value);
        rxChar.setWriteType(rxChar.WRITE_TYPE_NO_RESPONSE);

        boolean status = mBluetoothGatt.writeCharacteristic(rxChar);

        Log.d(TAG, "write rxChar status= " + status);
        lastCommandSent = System.currentTimeMillis();
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        Log.i(TAG, "Starting to Read Characteristic...");
        Log.i(TAG, String.valueOf(characteristic.getUuid().equals(BleConstants.UUID_TRANSPARENT_UART_TX)));
        for (int i = 0; i < characteristic.getDescriptors().size(); i++) {
            Log.i(TAG, characteristic.getDescriptors().get(i).getUuid().toString());
        }


        boolean success = mBluetoothGatt.readCharacteristic(characteristic);
        Log.i(TAG, String.valueOf(success));
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  Disable otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        if (!BleConstants.UUID_TRANSPARENT_UART_RX.equals(characteristic.getUuid())) {
            mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        }

        // Open the UART transparent stream by writing the Notify descriptor
        if (BleConstants.UUID_TRANSPARENT_UART_TX.equals(characteristic.getUuid())) {
            Log.i(TAG, "Setting Notification for TX");
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(PhoneBotAttributes.TX_CHARACTERISTIC_CONFIGURATION));

            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }

        if (BleConstants.UUID_TRANSPARENT_UART_RX.equals(characteristic.getUuid())) {
            Log.i(TAG, "Sending RX Data");

            characteristic.setWriteType(characteristic.WRITE_TYPE_NO_RESPONSE);

            mBluetoothGatt.writeCharacteristic(characteristic);
        }

    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    /**
     * Bind to context where service instance can be directly accessed.
     *
     * @param context Context to bind to.
     * @param address The device address to connect to.
     * @return Connection object; @see LocalConnection
     */
    public static LocalConnection bindToContext(Context context, final String address) {
        Intent intent = new Intent(context, BluetoothLeService.class);
        LocalConnection connection = new LocalConnection(address);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        return connection;
    }

    /**
     * Sets the minimum amount of time which must elapse before sending a data packet.
     * Messages sent before the time has elapsed will be discarded and not sent.
     *
     * @param delay Minimum message delay in milliseconds.
     */
    public void setMinMessageDelay(Integer delay) {
        minMessageDelay = delay;
    }

}
