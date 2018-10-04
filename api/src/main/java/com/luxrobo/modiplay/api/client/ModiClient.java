/*
 * Developement Part, LUXROBO INC., SEOUL, KOREA
 * Copyright(c) 2018 by LUXROBO Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LUXROBO Inc.
 */

package com.luxrobo.modiplay.api.client;

import android.bluetooth.BluetoothDevice;


public interface ModiClient {

    /**
     * Callback when found device
     * @param device BluetoothDevice
     * @param rssi RSSI
     * @param scanRecord ScanResult ScanRecord
     */
    void onFoundDevice(BluetoothDevice device, int rssi, byte[] scanRecord);

    /**
     * Callback when service discovered
     */
    void onDiscoveredService();

    /**
     * Callback when device connected
     */
    void onConnected();

    /**
     * Callback when device disconnected
     */
    void onDisconnected();

    /**
     * Callback when changed the scan state
     * @param isScaning
     */
    void onScanning(boolean isScaning);

    /**
     * Callback when received data from MODI Network Module
     * @param data display converted raw data to ascii string
     */
    void onReceivedData(String data);

    /**
     * Callback when received data from MODI Network Module
     * @param data raw data
     */
    void onReceivedData(byte[] data);

    /**
     * Callback when received user data from `Send Data` on MODI Studio
     * @param data
     */
    void onReceivedUserData(int data);

    /**
     * Callback when received buzzer state from MODI Studio
     * @param state
     */
    void onBuzzerState(int state);

    /**
     * Callback when event off
     */
    void onOffEvent();

    /**
     * Callback when disconnected MODI Network Module that power off
     */
    void disconnectedByModulePowerOff();
}
