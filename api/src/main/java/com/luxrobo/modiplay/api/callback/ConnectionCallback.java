/*
 * Developement Part, LUXROBO INC., SEOUL, KOREA
 * Copyright(c) 2018 by LUXROBO Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LUXROBO Inc.
 */

package com.luxrobo.modiplay.api.callback;


import com.luxrobo.modiplay.api.utils.ModiLog;

public abstract class ConnectionCallback {

    /**
     * BLE connect trying
     */
    public static final int BLE_CONNECT_TRYING = 11;
    /**
     * BLE connect done
     */
    public static final int BLE_CONNECT_DONE = 100;
    /**
     * BLE disconnect trying
     */
    public static final int BLE_DISCONNECT_TRYING = 98;
    /**
     * BLE disconnect done
     */
    public static final int BLE_DISCONNECT_DONE = 99;           // 해제완료

    /**
     * Callback when disconnect failure
     */
    public abstract void onDisconnectFailure();

    /**
     * Callback when Bluetooth Device ConnectionState Changed
     * @param connectionState connection state
     */
    public void onChangedConnectionState(int connectionState) {
        ModiLog.d("Bluetooth Device ConnectionState Changed to " + connectionState);
    }
}
