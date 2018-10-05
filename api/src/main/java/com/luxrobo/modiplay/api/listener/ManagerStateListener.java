/*
 * Developement Part, LUXROBO INC., SEOUL, KOREA
 * Copyright(c) 2018 by LUXROBO Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LUXROBO Inc.
 */

package com.luxrobo.modiplay.api.listener;

import com.luxrobo.modiplay.api.client.ServiceStateClient;
import com.luxrobo.modiplay.api.utils.ModiLog;


public abstract class ManagerStateListener implements ServiceStateClient {

    private final static String TAG = ManagerStateListener.class.getSimpleName();

    @Override
    public void onBind() {
        ModiLog.d(TAG, "Service Bind");
        onCompletedToInitialize();
    }

    @Override
    public void onUnBind() {
        ModiLog.d(TAG, "Service Unbind");
        onCompletedToDeinitialize();
    }

    public abstract void onCompletedToInitialize();

    public abstract void onCompletedToDeinitialize();
}
