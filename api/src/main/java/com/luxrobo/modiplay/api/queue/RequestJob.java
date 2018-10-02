/*
 * Developement Part, Luxrobo INC., SEOUL, KOREA
 * Copyright(c) 2018 by Luxrobo Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of Luxrobo Inc.
 */

package com.luxrobo.modiplay.api.queue;

import android.bluetooth.BluetoothGattCharacteristic;


public class RequestJob {

    public String method;
    public boolean notify;
    public int trial = 0;
    public BluetoothGattCharacteristic characteristic;
}
