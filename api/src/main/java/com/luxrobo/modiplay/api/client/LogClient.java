/*
 * Developement Part, LUXROBO INC., SEOUL, KOREA
 * Copyright(c) 2018 by LUXROBO Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LUXROBO Inc.
 */

package com.luxrobo.modiplay.api.client;


public interface LogClient {

    /**
     * Callback when received raw data from MODI Network Module
     *
     * @param data raw data
     */
    void onReceivedRawData(String data);
}
