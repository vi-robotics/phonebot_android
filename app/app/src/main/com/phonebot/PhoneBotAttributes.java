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

import java.util.HashMap;

/**
 * This class decribes the BLE attributes and services of PhoneBot
 */
public class PhoneBotAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String TRANSPARENT_UART_SERVICE = "49535343-fe7d-4ae5-8fa9-9fafd205e455";
    public static String TRANSPARENT_UART_TX = "49535343-1e4d-4bd9-ba61-23c647249616"; // The BLE module sends data to this characteristic. Write "Notify" to this in order to enable stream
    public static String TRANSPARENT_UART_RX = "49535343-8841-43f4-a8d4-ecbe34729bb3"; // Send data to this characteristic
    public static String TX_CHARACTERISTIC_CONFIGURATION = "00002902-0000-1000-8000-00805f9b34fb";

    public static String DEVICE_INFORMATION_SERVICE = "0000180a-0000-1000-8000-00805f9b34fb";
    public static String MANUFACTURER_NAME_STRING = "00002a29-0000-1000-8000-00805f9b34fb";

    static {
        // Services.
        attributes.put(TRANSPARENT_UART_SERVICE, "Transparent UART Service");
        attributes.put(DEVICE_INFORMATION_SERVICE, "Device Information Service");

        // Characteristics.
        attributes.put(TRANSPARENT_UART_TX, "Transparent UART TX");
        attributes.put(TRANSPARENT_UART_RX, "Transparent UART RX");
        attributes.put(MANUFACTURER_NAME_STRING, "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
