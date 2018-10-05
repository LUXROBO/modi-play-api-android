/*
 * Developement Part, LUXROBO INC., SEOUL, KOREA
 * Copyright(c) 2018 by LUXROBO Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LUXROBO Inc.
 */

package com.luxrobo.modiplay.api.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class ModiStringUtil {

    /**
     * 빈 문자열 체크
     *
     * @param str
     * @return
     */
    public static boolean isNullOrEmpty(String str) {
        return (str == null || str.trim().length() == 0) ? true : false;
    }


    public static Object getDataObject(HashMap paramMap, String key) {
        Object obj = null;
        if (paramMap != null) {
            obj = paramMap.get(key);
        }
        return obj;
    }

    /**
     * 해시맵에서 value 반환
     *
     * @param paramMap
     * @param key
     * @return
     */
    public static String getData(HashMap paramMap, String key) {
        String result = "";
        if (paramMap != null && paramMap.get(key) != null) {
            result = paramMap.get(key).toString();
            if (ModiStringUtil.isNullOrEmpty(result) == true || result.equalsIgnoreCase("null") == true) {
                result = "";
            }
        }

        return result;
    }

    /**
     * 해시맵에서 value 반환, 없을 경우 디폴트 문자열 반환
     *
     * @param paramMap
     * @param key
     * @param defaultStr
     * @return
     */
    public static String getData(HashMap paramMap, String key, String defaultStr) {
        String result = getData(paramMap, key);
        if (isNullOrEmpty(result)) {
            result = defaultStr;
        }

        return result;
    }

    /**
     * 해시맵에서 value 반환, 없을 경우 디폴트 문자열 반환, 0도 null로 인정
     *
     * @param paramMap
     * @param key
     * @param defaultStr
     * @return
     */
    public static String getDataWithoutZero(HashMap paramMap, String key, String defaultStr) {
        String result = "";
        try {
            result = String.format("%,d", getDataToInt(paramMap, key));
        } catch (Exception e) {
            ModiLog.e("getDataWithoutZero", e.toString());
        }

        if (isNullOrEmpty(result) || result.equalsIgnoreCase("0")) {
            result = defaultStr;
        }

        return result;
    }

    /**
     * 해시맵에서 int value 반환
     *
     * @param dataMap
     * @param key
     * @return
     */
    public static int getDataToInt(HashMap dataMap, String key) {
        return toInt(getData(dataMap, key));
    }

    /**
     * 해시맵에서 int value 반환
     *
     * @param dataMap
     * @param key
     * @return
     */
    public static int getDataToInt(HashMap dataMap, String key, int defaultVal) {
        Integer result = toInteger(getData(dataMap, key));

        if (result == null) {
            return defaultVal;
        } else {
            return result.intValue();
        }
    }

    /**
     * 해시맵에서 float value 반환
     *
     * @param dataMap
     * @param key
     * @return
     */
    public static float getDataToFloat(HashMap dataMap, String key) {
        return toFloat(getData(dataMap, key));
    }

    /***
     * string 을 int로 변환, 변환되지 않을 경우 0 반환
     *
     * @param data
     * @return
     */
    public static int toInt(String data) {
        int result = 0;

        if (isNullOrEmpty(data) == false) {
            try {
                if (data.indexOf(".") > 0) {
                    data = data.substring(0, data.indexOf("."));
                }
                result = Integer.parseInt(data);
            } catch (Exception ex1) {
                ModiLog.e("toInt value:" + data, ex1.toString());
            }
        }

        return result;
    }

    /***
     * string 을 Integer로 변환, 변환되지 않을 경우 null 반환
     *
     * @param data
     * @return
     */
    public static Integer toInteger(String data) {
        Integer result = null;

        if (isNullOrEmpty(data) == false) {
            try {
                result = Integer.valueOf(data);
            } catch (Exception ex1) {
                result = null;
                ModiLog.e("toInteger value:" + data, ex1.toString());
            }
        }

        return result;
    }

    /***
     * string 을 float 로 반환, 반환되지 않을 경우 0.f 반환
     *
     * @param data
     * @return
     */
    public static float toFloat(String data) {
        float result = 0.f;

        if (isNullOrEmpty(data) == false) {
            try {
                if (data.indexOf(".") > 0) {
                    result = Float.parseFloat(data);
                } else {
                    result = (float) Integer.parseInt(data);
                }
            } catch (Exception e) {
                ModiLog.e("toFloat value: " + data, e.toString());
            }
        }
        return result;
    }


    public static String removeDash(String value) {
        String result = value;

        if (isNullOrEmpty(value) == false) {
            result = value.replaceAll("-", "");
        }

        return result;
    }

    public static String removeNotNumber(String value) {
        String result = value;

        if (isNullOrEmpty(value) == false) {
            result = value.replaceAll("\\|\\@\\|", "-");
        }

        return result;
    }

    public static String toFormatDate(String data, String token) {
        String result = data;
        if (isNullOrEmpty(data) == false && data.length() == 8) {
            result = data.substring(0, 4) + token + data.substring(4, 6) + token + data.substring(6, 8);
        }

        return result;
    }

    public static String removeEnter(String str) {
        String result = str;

        if (isNullOrEmpty(str) == false) {
            result = str.replaceAll("\n", " ");
        }

        return result;
    }

    public static String cutStringByte(String data, int len) {
        byte[] strByte = data.getBytes();
        int asc;
        int retLength = 0;
        int tempSize = 0;

        if (isNullOrEmpty(data) || strByte.length < len) {
            return data;
        }

        int length = data.length();

        for (int i = 1; i <= length; i++) {
            asc = (int) data.charAt(i - 1);
            if (asc > 127) {
                if (len >= tempSize + 2) {
                    tempSize += 2;
                    retLength++;
                } else {
                    return data.substring(0, retLength) + "...";
                }
            } else {
                if (len > tempSize) {
                    tempSize++;
                    retLength++;
                }
            }
        }

        return data.substring(0, retLength);
    }

    public static String getData(String[] arr_src, int idx) {
        String result = null;

        if (arr_src != null && idx < arr_src.length) {
            result = arr_src[idx];
        }

        return result;
    }

    /**
     * 숫자로 변환(숫자로 변경이 불가능한 경우 0을 반환)
     *
     * @param number (숫자형 스트링)
     * @return Integer 변환된 값
     */
    public static int convertInteger(String number) {
        int result = 0;

        try {
            result = Integer.valueOf(number);
        } catch (Exception e) {
            ModiLog.e("convertInteger number:" + number, e.toString());
        }

        return result;
    }

    /**
     * Date to formatted String
     *
     * @param targetDate
     * @param format
     * @return
     */
    public static String dateFormattedString(Date targetDate, String format) {
        String formatedDate = "";

        try {
            format = (isNullOrEmpty(format)) ? "yyyyMMdd" : format;
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            formatedDate = dateFormat.format(targetDate);
        } catch (Exception e) {
            ModiLog.e("dateFormattedString", e.toString());
        }

        return formatedDate;
    }

    /**
     * formatted string to date
     *
     * @param formattedString
     * @param format
     * @return
     */
    public static Date formattedStringToDate(String formattedString, String format) {
        Date convertedDate = null;
        try {
            format = (isNullOrEmpty(format)) ? "yyyyMMdd" : format;
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            convertedDate = dateFormat.parse(formattedString);
        } catch (Exception e) {
            ModiLog.e("formattedStringToDate " + formattedString, e.toString());
        }
        return convertedDate;
    }


    /**
     * formatted string to time
     *
     * @param formattedString
     * @param format
     * @return
     */
    public static Date formattedStringToTime(String formattedString, String format) {
        Date convertedDate = null;
        try {
            format = (isNullOrEmpty(format)) ? "HH:mm:ss" : format;
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            convertedDate = dateFormat.parse(formattedString);
        } catch (Exception e) {
            ModiLog.e("formattedStringToDate " + formattedString, e.toString());
        }
        return convertedDate;
    }

    /**
     * formatted string to date long
     *
     * @param formattedString
     * @param format
     * @return
     */
    public static long formattedStringToDateTicks(String formattedString, String format) {
        long convertedTicks = 0;
        Date convertedDate = null;
        try {
            format = (isNullOrEmpty(format)) ? "yyyyMMdd" : format;
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            convertedDate = dateFormat.parse(formattedString);

            Calendar cal = Calendar.getInstance();
            cal.setTime(convertedDate);

            convertedTicks = cal.getTimeInMillis();
        } catch (Exception e) {
            ModiLog.e("formattedStringToDate " + formattedString, e.toString());
        }
        return convertedTicks;
    }

    /**
     * convert utf-8 string to byte
     *
     * @param targetStr
     * @return
     * @throws Exception
     */
    public static byte[] convertUTF8StringToByte(String targetStr) throws Exception {
        return targetStr.getBytes("UTF-8");
    }

    public static String convertByteToString(byte[] data, int offset, int strLength, String encoding) throws Exception {
        if (encoding == null | encoding.length() == 0) {
            encoding = "UTF-8";
        }

        byte[] strBuff = new byte[strLength];
        for (int i = 0; i < strBuff.length; i++) {
            strBuff[i] = data[offset];
            offset++;
        }

        String convertedString = new String(strBuff, encoding);

        return convertedString;
    }

    public static String convertByteToHexString(byte[] data, String separator) throws Exception {
        if (data != null) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(Byte.toString(byteChar) + separator);

            return stringBuilder.toString();
        }
        return "";
    }

    public static String convertByteToHexString(byte[] data) throws Exception {

        return convertByteToHexString(data, "");
    }
}
