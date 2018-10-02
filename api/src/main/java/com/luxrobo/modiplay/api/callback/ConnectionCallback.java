/*
 * Developement Part, Luxrobo INC., SEOUL, KOREA
 * Copyright(c) 2018 by Luxrobo Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of Luxrobo Inc.
 */

package com.luxrobo.modiplay.api.callback;


import com.luxrobo.modiplay.api.utils.ModiLog;

public abstract class ConnectionCallback {

    public static final int BLE_CONNECT_TRYING = 11;            // 연결시도
    public static final int BLE_CONNECT_DONE = 100;             // 연결완료
    public static final int BLE_DISCONNECT_TRYING = 98;         // 해제시도
    public static final int BLE_DISCONNECT_DONE = 99;           // 해제완료

    public abstract void onDisconnectFailure();

    public void onChangedConnectionState(int connectionState) {

        ModiLog.d("Bluetooth Device ConnectionState Changed to " + connectionState);
    }
}
