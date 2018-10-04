/*
 * Developement Part, LUXROBO INC., SEOUL, KOREA
 * Copyright(c) 2018 by LUXROBO Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LUXROBO Inc.
 */

package com.luxrobo.modiplay.api.client;


import com.luxrobo.modiplay.api.enums.Characteristics;

public interface NotifyStateClient {

    /**
     * Callback when changed NotificationState
     *
     * @param characteristics Characteristics
     * @param enable
     */
    void onChangedNotificationState(Characteristics characteristics, boolean enable);
}
