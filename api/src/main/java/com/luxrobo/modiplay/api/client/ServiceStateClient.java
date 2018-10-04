/*
 * Developement Part, LUXROBO INC., SEOUL, KOREA
 * Copyright(c) 2018 by LUXROBO Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LUXROBO Inc.
 */

package com.luxrobo.modiplay.api.client;


public interface ServiceStateClient {

    /**
     * Callback when Service bind
     */
    void onBind();

    /**
     * Callback when Service unbind
     */
    void onUnBind();
}
