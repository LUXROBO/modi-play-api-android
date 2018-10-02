/*
 * Developement Part, Luxrobo INC., SEOUL, KOREA
 * Copyright(c) 2018 by Luxrobo Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of Luxrobo Inc.
 */

package com.luxrobo.modiplay.api.data;

import com.luxrobo.modiplay.api.enums.State;


public class DeviceInformation {

    public String deviceName;
    public String deviceAddress = "";
    public String serialNumber;
    public String hardwareRevision;
    public String firmwareRevision;
    public String softwareRevision;
    public int batteryLevel;
    public State state = State.DISCONNECTED;

    private DeviceInformation() {

        deviceName = null;
        deviceAddress = "";
        serialNumber = null;
        hardwareRevision = null;
        firmwareRevision = null;
        softwareRevision = null;
        state = State.DISCONNECTED;
    }

    private static class Singleton {
        private static final DeviceInformation instance = new DeviceInformation();
    }

    public static DeviceInformation getInstance() {
        return Singleton.instance;
    }

    public void deinit() {

        deviceName = null;
        deviceAddress = "";
        serialNumber = null;
        hardwareRevision = null;
        firmwareRevision = null;
        softwareRevision = null;
        batteryLevel = 0;
        state = State.DISCONNECTED;
    }
}
