/*
 * Developement Part, Luxrobo INC., SEOUL, KOREA
 * Copyright(c) 2018 by Luxrobo Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of Luxrobo Inc.
 */

package com.luxrobo.modiplay.api.enums;


import com.luxrobo.modiplay.api.core.ModiGattAttributes;

public enum Characteristics {

    DEVICE_CHAR_TX_RX(ModiGattAttributes.DEVICE_CHAR_TX_RX);


    private final String code;

    Characteristics(String code) {

        this.code = code;
    }

    public String code() {
        return this.code;
    }

    public static Characteristics get(String appCode) {
        for (Characteristics value : Characteristics.values()) {
            if (appCode.equalsIgnoreCase(value.code())) return value;
        }
        return null;
    }
}
