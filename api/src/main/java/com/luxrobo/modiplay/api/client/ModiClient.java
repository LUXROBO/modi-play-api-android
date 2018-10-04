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
     * 디바이스를 찾으면 호출되는 메소드
     * @param device
     * @param rssi
     * @param scanRecord
     */
    void onFoundDevice(BluetoothDevice device, int rssi, byte[] scanRecord);

    /**
     * 서비스가 발견되면 호출되는 메소드
     */
    void onDiscoveredService();

    /**
     * 연결이 되면 호출되는 메소드
     */
    void onConnected();

    /**
     * 연결이 해제 되면 호출되는 메소드
     */
    void onDisconnected();

    /**
     * 스캔 상태를 알려주는 메소드
     * @param isScaning 스캔중이면 true, 아니면 false
     */
    void onScanning(boolean isScaning);

    /**
     * 네트워크 모듈에서 데이터를 받으면 호출되는 메소드
     * @param data Raw Data를 String으로 표시한 값
     */
    void onReceivedData(String data);

    /**
     * 네트워크 모듈에서 데이터를 받으면 호출되는 메소드
     * @param data Raw Data
     */
    void onReceivedData(byte[] data);

    /**
     * MODI Studio상에서 Send Data를 통해 받은 값
     * @param data
     */
    void onReceivedUserData(int data);

    /**
     * MODI Studio상에서 Buzzer의 값
     * @param state on: 1, off: 0
     */
    void onBuzzerState(int state);

    /**
     * evnet가 꺼지면 호출되는 메소드
     */
    void onOffEvent();

    /**
     * 네트워크 모듈이 전원이 꺼지면서 연결이 끊긴것을 감지하는 메소드
     */
    void disconnectedByModulePowerOff();
}
