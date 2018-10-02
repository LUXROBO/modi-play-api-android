/*
 * Developement Part, Luxrobo INC., SEOUL, KOREA
 * Copyright(c) 2018 by Luxrobo Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of Luxrobo Inc.
 */

package com.luxrobo.modiplay.api.client;


public interface BluetoothClient {

    void onBluetoothEnabled();

    void onBluetoothDisabled();

    void onBluetoothStateOnConnected();

    void onBluetoothStateOnDisconnected();

    void onBluetoothError();

    void onBluetoothStateUnknown(int state);
}