/*
 * Developement Part, LUXROBO INC., SEOUL, KOREA
 * Copyright(c) 2018 by LUXROBO Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LUXROBO Inc.
 */

package com.luxrobo.modiplay.api.listener;

import com.luxrobo.modiplay.api.client.BluetoothClient;


public abstract class BluetoothStateListener implements BluetoothClient {

    @Override
    public void onBluetoothEnabled() {
        onBluetoothEnabled(true);
    }

    @Override
    public void onBluetoothDisabled() {
        onBluetoothEnabled(false);
    }

    @Override
    public void onBluetoothStateOnConnected() {
        onBluetoothStateOn(true);
    }

    @Override
    public void onBluetoothStateOnDisconnected() {
        onBluetoothStateOn(false);
    }

    @Override
    public void onBluetoothError() {
        onBluetoothStateError();
    }

    @Override
    public void onBluetoothStateUnknown(int state) {
        onBluetoothStateUnknown();
    }

    public abstract void onBluetoothEnabled(boolean enabled);

    public abstract void onBluetoothStateOn(boolean connected);

    public abstract void onBluetoothStateError();

    public abstract void onBluetoothStateUnknown();
}