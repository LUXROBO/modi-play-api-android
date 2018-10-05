/*
 * Developement Part, LUXROBO INC., SEOUL, KOREA
 * Copyright(c) 2018 by LUXROBO Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LUXROBO Inc.
 */

package com.luxrobo.modiplay.api.parser;

import com.luxrobo.modiplay.api.utils.ModiStringUtil;


public class ManufacturerDataParser {

    private static final String TAG = ManufacturerDataParser.class.getSimpleName();

    public static String getMacAddress(byte[] scanRecord) {
        final String manufacturerCode = "FF:5D:03:";
        String macAddress = "";
        final String strManufacturerData;
        final String strReverse;
        final String[] strArr;
        final StringBuffer stringBuffer;
        try {

            strManufacturerData = ModiStringUtil.convertByteToHexString(scanRecord, ":");

            if (strManufacturerData.contains(manufacturerCode)) {
                // Mac Address가 있는 경우
                int index = strManufacturerData.indexOf(manufacturerCode) + manufacturerCode.length();

                strReverse = strManufacturerData.substring(index, index + 17);
                strArr = strReverse.split(":");
                stringBuffer = new StringBuffer();

                for (int i = strArr.length - 1; i >= 0; i--) {

                    stringBuffer.append(strArr[i]);
                    if (i > 0) stringBuffer.append(":");
                }

                macAddress = stringBuffer.toString();
            }

        } catch (Exception e) {

        }

        return macAddress;
    }
}
