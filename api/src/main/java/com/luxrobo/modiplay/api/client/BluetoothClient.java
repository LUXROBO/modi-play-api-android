/*
 * Developement Part, LUXROBO INC., SEOUL, KOREA
 * Copyright(c) 2018 by LUXROBO Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LUXROBO Inc.
 */

package com.luxrobo.modiplay.api.client;


public interface BluetoothClient {

    /**
     * Callback when Bluetooth enabled
     */
    void onBluetoothEnabled();

    /**
     * Callback when Bluetooth disabled
     */
    void onBluetoothDisabled();

    /**
     * Callback when Bluetooth connected
     */
    void onBluetoothStateOnConnected();

    /**
     * Callback when Bluetooth disconnected
     */
    void onBluetoothStateOnDisconnected();

    /**
     * Callback when Bluetooth error
     */
    void onBluetoothError();

    /**
     * Callback when Bluetooth state unknown
     */
    void onBluetoothStateUnknown(int state);
}