package main.com.phonebot;

import java.util.UUID;

public class BleConstants {

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static UUID UUID_TRANSPARENT_UART_SERVICE =
            UUID.fromString(PhoneBotAttributes.TRANSPARENT_UART_SERVICE);
    public final static UUID UUID_TRANSPARENT_UART_TX =
            UUID.fromString(PhoneBotAttributes.TRANSPARENT_UART_TX);
    public final static UUID UUID_TRANSPARENT_UART_RX =
            UUID.fromString(PhoneBotAttributes.TRANSPARENT_UART_RX);
}
