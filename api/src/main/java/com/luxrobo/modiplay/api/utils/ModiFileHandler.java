/*
 * Developement Part, Luxrobo INC., SEOUL, KOREA
 * Copyright(c) 2018 by Luxrobo Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of Luxrobo Inc.
 */

package com.luxrobo.modiplay.api.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class ModiFileHandler {

    private static final String TAG = ModiFileHandler.class.getSimpleName();
    private Context context;
    private static final String LOGFILE_EXT = ".txt";
    private static final int BUFF_LENGTH = 1024;

    public static final String LOG_RX_DATA = "RX";
    public static final String KEY_LOG_FILE_NAME = "FILENAME";


    public ModiFileHandler(Context _context) {
        this.context = _context;
    }

    public static final Calendar convertStringToCalendar(String date, String time) {
        Calendar cal = Calendar.getInstance();
        String format = "yyyyMMdd";

        try {
            if (date.length() < 8) {
                date = getDate(cal);
            }

            if (time.length() == 6) {
                format = format + "HHmmss";
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            Date convertDate = dateFormat.parse(date + time);
            cal.setTime(convertDate);
        } catch (Exception e) {
            ModiLog.e(TAG, e.toString());

            return null;
        }

        return cal;
    }


    public static final int getYear(Calendar cal) {
        return cal.get(Calendar.YEAR);
    }

    public static final int getMonth(Calendar cal) {
        return cal.get(Calendar.MONTH) + 1;
    }

    public static final int getMonthOfYear(Calendar cal) {
        return cal.get(Calendar.MONTH);
    }

    public static final int getDay(Calendar cal) {
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public static final int getHour(Calendar cal) {
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public static final int getMinute(Calendar cal) {
        return cal.get(Calendar.MINUTE);
    }

    public static final int getSecond(Calendar cal) {
        return cal.get(Calendar.SECOND);
    }


    public static final String getDate() {
        String _today = "";

        Calendar _cal = Calendar.getInstance();

        int _year = _cal.get(Calendar.YEAR);
        int _month = _cal.get(Calendar.MONTH) + 1;
        int _day = _cal.get(Calendar.DAY_OF_MONTH);
        int _hour = _cal.get(Calendar.HOUR_OF_DAY);
        int _minute = _cal.get(Calendar.MINUTE);
        int _second = _cal.get(Calendar.SECOND);

        _today = _today + _year;
        _today = (_month < 10) ? _today + "0" + _month : _today + _month;
        _today = (_day < 10) ? _today + "0" + _day : _today + _day;
        _today = (_hour < 10) ? _today + "0" + _hour : _today + _hour;
        _today = (_minute < 10) ? _today + "0" + _minute : _today + _minute;
        _today = (_second < 10) ? _today + "0" + _second : _today + _second;

        return _today;
    }


    public static final String getDate(Calendar _cal) {
        String _today = "";

        int _year = _cal.get(Calendar.YEAR);
        int _month = _cal.get(Calendar.MONTH) + 1;
        int _day = _cal.get(Calendar.DAY_OF_MONTH);
        int _hour = _cal.get(Calendar.HOUR_OF_DAY);
        int _minute = _cal.get(Calendar.MINUTE);
        int _second = _cal.get(Calendar.SECOND);

        _today = _today + _year;
        _today = (_month < 10) ? _today + "0" + _month : _today + _month;
        _today = (_day < 10) ? _today + "0" + _day : _today + _day;
        _today = (_hour < 10) ? _today + "0" + _hour : _today + _hour;
        _today = (_minute < 10) ? _today + "0" + _minute : _today + _minute;
        _today = (_second < 10) ? _today + "0" + _second : _today + _second;

        return _today;
    }

    private String getFilePath(String _root, String _measurementType) {
        _root = (_root.endsWith(File.separator)) ? _root : _root + File.separator;
        String _dirPath = _root + LOG_RX_DATA;

        boolean _ok = false;
        try {
            File _dir = new File(_dirPath);
            if (!_dir.isDirectory()) {
                _ok = _dir.mkdirs();
            } else {
                _ok = true;
            }
        } catch (Exception e) {
            ModiLog.e(TAG, e.toString());
        }

        if (_ok) {
            return _dirPath;
        } else {
            return null;
        }
    }

    private String getDir(String _measurementType) {
        String _root = this.context.getFilesDir().getAbsolutePath();

        ModiLog.d(TAG, "root:" + _root);
        ModiLog.d(TAG, "root:" + EnvironmentCompat.getStorageState(Environment.getDataDirectory()));

        return getFilePath(_root, _measurementType);

    }

    private String getExternalDir(String _measurementType) {
        String _root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        ModiLog.d(TAG, "ext root:" + _root);

        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return getFilePath(_root, _measurementType);
        }
        return null;
    }

    public ArrayList<String> fileSearch(String _measurementType) {
        ArrayList<String> _fileList = new ArrayList<String>();

        String _dirPath = getDir(_measurementType);
        ModiLog.d(TAG, "_dirPath: " + _dirPath);
        try {
            File _file = new File(_dirPath);

            for (String _filename : _file.list()) {
                _fileList.add(_dirPath + File.separator + _filename);

                ModiLog.d(TAG, "fileSearch: " + _filename);
            }

        } catch (Exception e) {
            ModiLog.e(TAG, e.toString());
        }

        return _fileList;
    }


    private boolean fileExist(String _dir, String _filename) {
        boolean _exist = false;

        try {
            File _file = new File(this.context.getFilesDir(), _filename);
            _exist = _file.exists();
            _file = null;
        } catch (Exception e) {
            ModiLog.e(TAG, e.toString());
        }

        return _exist;
    }

    private String getFileName(String _dir, String _measurementType) {
        String _filehead = _measurementType + "_" + ModiFileHandler.getDate();
        String _filename = _filehead + LOGFILE_EXT;

        int i = 1;
        while (fileExist(_dir, _filename)) {
            _filename = _filehead + "_" + i + LOGFILE_EXT;
            i++;
        }

        return _dir + File.separator + _filename;
    }

    private String writeLogFile(String str, String _dir, String _measurementType) {
        String _logFileName = getFileName(_dir, _measurementType);
        ModiLog.d(TAG, "_logFileName: " + _logFileName);

        try {
            if (ContextCompat.checkSelfPermission(this.context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ModiLog.d(TAG, "denied permission WRITE_EXTERNAL_STORAGE");
            } else {
                ModiLog.d(TAG, "granted permission WRITE_EXTERNAL_STORAGE");
            }

            FileWriter _fileWriter = new FileWriter(_logFileName, true);
            _fileWriter.write(str);
            _fileWriter.flush();
            _fileWriter.close();

        } catch (Exception e) {
            ModiLog.e(TAG, e.toString());
            return null;
        }

        return _logFileName;
    }

    public String writeLogFile(final String str, final String _measurementType) {
        String _dir = getExternalDir(_measurementType);

        if (_dir != null) {
            return writeLogFile(str, _dir, _measurementType);
        } else {
            return null;
        }
    }

    public String writeLogFileWithTimeStamp(final String str, final String _measurementType) {
        String _dir = getExternalDir(_measurementType);

        if (_dir != null) {
            return writeLogFile(String.format(Locale.KOREAN, "[%s] %s", ModiFileHandler.getDate(), str), _dir, _measurementType);
        } else {
            return null;
        }
    }

    public String writeLogFileToExternal(final String str, final String _measurementType) {

        if (Build.MODEL.equalsIgnoreCase("LG-F600S")) {

            //writeLogFile(str, _measurementType);
        }

        String _dir = getExternalDir(_measurementType);

        if (_dir != null) {
            return writeLogFile(str, _dir, _measurementType);
        } else {
            return null;
        }
    }

    public boolean deleteLogFile(String _filename) {
        boolean _success = false;

        try {
            File _file;
            _file = new File(_filename);

            _success = _file.delete();
            _file = null;

            ModiLog.d(TAG, "_logFileName: " + _filename);
        } catch (Exception e) {
            ModiLog.e(TAG, e.toString());
        }

        return _success;
    }
}