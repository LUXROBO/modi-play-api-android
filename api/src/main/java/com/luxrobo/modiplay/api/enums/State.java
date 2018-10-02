/*
 * Developement Part, Luxrobo INC., SEOUL, KOREA
 * Copyright(c) 2018 by Luxrobo Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of Luxrobo Inc.
 */

package com.luxrobo.modiplay.api.enums;


public enum State {

    DISCONNECTED(0),
    CONNECTING(1),
    CONNECTED(2),
    SEARCHING(3),
    STOPSEARCHING(4);

    private final int code;

    State(int code) {

        this.code = code;
    }

    public int code() {

        return this.code;
    }
}
