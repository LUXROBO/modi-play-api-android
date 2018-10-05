/*
 * Developement Part, LUXROBO INC., SEOUL, KOREA
 * Copyright(c) 2018 by LUXROBO Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LUXROBO Inc.
 */

package com.luxrobo.modiplay.api.core;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.luxrobo.modiplay.api.callback.ConnectionCallback;
import com.luxrobo.modiplay.api.client.BluetoothClient;
import com.luxrobo.modiplay.api.client.LogClient;
import com.luxrobo.modiplay.api.client.ModiClient;
import com.luxrobo.modiplay.api.client.NotifyStateClient;
import com.luxrobo.modiplay.api.client.ServiceStateClient;
import com.luxrobo.modiplay.api.data.DeviceInformation;
import com.luxrobo.modiplay.api.enums.Characteristics;
import com.luxrobo.modiplay.api.enums.State;
import com.luxrobo.modiplay.api.listener.GattCloseListener;
import com.luxrobo.modiplay.api.listener.ManagerStateListener;
import com.luxrobo.modiplay.api.parser.ManufacturerDataParser;
import com.luxrobo.modiplay.api.queue.RequestQueue;
import com.luxrobo.modiplay.api.utils.ModiFileHandler;
import com.luxrobo.modiplay.api.utils.ModiLog;
import com.luxrobo.modiplay.api.utils.ModiPreference;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class ModiManager {

    private Context context;
    private boolean isScanning = false;                          // 검색 여부
    private boolean isConnected = false;                         // 연결 여부
    private boolean isDiscoveredCharacteristics = false;         // 속성확인 여부
    private boolean isDisconnectPermanently = false;             // 자동해제 여부

    private BluetoothAdapter bluetoothAdapter;                  //
    private BluetoothLeScanner bluetoothLeScanner;              //

    private ModiService mModiService;                           // 블루투스 서비스
    private ModiClient mModiClient;                             // 이벤트 클라언트
    private LogClient mLogClient;                               // 로그 클라이언트
    private NotifyStateClient mNotifyStateClient;               // 속성 알림 변경
    private ServiceStateClient mServiceStateClient;             //
    private BluetoothClient mBluetoothClient;

    private HashMap<String, BluetoothGattCharacteristic> characteristicsList = new HashMap<String, BluetoothGattCharacteristic>();
    private HashMap<String, Boolean> characteristicsNotifyState = new HashMap<>();

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int SCAN_PERIOD = 30000;               // 블루투스 스캔 주기
    private static final int REQUEST_INTERVAL = 3000;           //
    private static final int DISCOVER_DELAY_TIME = 50;         // 블루투스 속성 발견 핸들러의 지연 시간
    private String mConnectedDeviceName;

    private final ServiceConnection serviceConnection = new ModuleServiceConnection();

    private DeviceInformation deviceInformation;

    private HandlerThread MODIBackgroundThread;                         //SDK Background Handler

    private int ScanningTimes = 0;                                // 스캐닝 중 스캔시도 횟수, 5번이 최대

    private ModiManager() {
        MODIBackgroundThread = new HandlerThread("MODI Background Handler");
        deviceInformation = DeviceInformation.getInstance();
        setIsDisconnectPermanently(false);
    }

    private static class ManagerSingleton {
        private static final ModiManager instance = new ModiManager();
    }

    /**
     * get ModiManager Instance
     *
     * @return ModiManager Instance
     */
    public static ModiManager getInstance() {
        return ManagerSingleton.instance;
    }

    private void setIsDisconnectPermanently(boolean disconnectPermanently) {
        isDisconnectPermanently = disconnectPermanently;
    }

    private class ModuleServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                ModiService.LocalBinder binder = (ModiService.LocalBinder) service;
                mModiService = binder.getService();

                if (!mModiService.initialize()) {
                    ModiLog.e("Unable to initialize service");
                } else {
                    ModiLog.d("Service initialized");
                }
            } catch (Exception e) {
                ModiLog.e("Service Connection Error, service may not be initialized.");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ModiLog.d("Service is disconnected");
            mModiService = null;
        }
    }

    /**
     * turn on bluetooth
     *
     * @return result
     */
    public boolean turnOnBluetooth() {
        if (this.bluetoothAdapter == null) {
            setBluetoothAdapter();
        }

        try {
            if (this.bluetoothAdapter != null) {
                if (!this.bluetoothAdapter.isEnabled()) {
                    this.bluetoothAdapter.enable();
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            ModiLog.e("turnOnBluetooth Error " + e.toString());
        }

        return false;
    }

    private Handler rebootBluetoothHandler;

    /**
     * reboot bluetooth adapter
     *
     * @return result
     */
    public boolean rebootBluetoothAdapter() {
        if (this.bluetoothAdapter == null) {
            setBluetoothAdapter();
        }

        if (this.bluetoothAdapter != null) {
            if (this.bluetoothAdapter.isEnabled()) {
                try {
                    this.bluetoothAdapter.disable();
                    ModiLog.d("Bluetooth Turning off");

                    try {
                        if (rebootBluetoothHandler == null) {
                            rebootBluetoothHandler = new Handler(context.getMainLooper());
                        }
                    } catch (Exception e) {
                        ModiLog.e("rebootBluetoothAdapter Error " + e.toString());
                    } finally {
                        if (rebootBluetoothHandler == null) {
                            rebootBluetoothHandler = new Handler();
                        }
                    }

                    rebootBluetoothHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                bluetoothAdapter.enable();
                                ModiLog.d("Bluetooth Turning on");
                            } catch (Exception e) {
                                ModiLog.e("Bluetooth turn On Error " + e.toString());
                            }
                        }
                    }, 3000);
                } catch (Exception e) {
                    ModiLog.d("Bluetooth turn Off Error " + e.toString());
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 블루투스 켜기(강제로 블루투스 사용 시)
     */
    private void setBluetoothAdapter() {
        try {
            if (this.context != null) {
                final BluetoothManager bluetoothManager = (BluetoothManager) this.context.getSystemService(Context.BLUETOOTH_SERVICE);
                this.bluetoothAdapter = bluetoothManager.getAdapter();

                if (!this.bluetoothAdapter.isEnabled()) {
                    // bluetoothAdapter.enable();
                }
            }
        } catch (Exception e) {
            ModiLog.e("setBluetoothAdapter Error: " + e.toString());
        }
    }

    /**
     * 블루투스 상태 수신
     */
    private final BroadcastReceiver bluetoothAdapterReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            ModiLog.d(String.format("BluetoothAdapter Sent Action %s", action));

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                ModiLog.d(String.format("BluetoothAdapter State Changed to %d", state));

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        ModiLog.d("Bluetooth is OFF");
                        mModiClient.onOffEvent();
                        ScanningTimes = 0;
                        if (mBluetoothClient != null) {
                            mBluetoothClient.onBluetoothDisabled();
                        }
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                        ModiLog.d("Bluetooth is TURNING OFF");

                        if (mBluetoothClient != null) {
                            // mBluetoothClient.onBluetoothDisabled();
                        }
                        break;

                    case BluetoothAdapter.STATE_ON:
                        ModiLog.d("Bluetooth is ON");

                        disconnectedCount = 0;

                        try {
                            new Handler(MODIBackgroundThread.getLooper()).postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    if (!isConnected()) {
                                        if (!isDisconnectPermanently) {
                                            ModiLog.d("Bluetooth on, connecting start");
                                            scanForConnect();
                                        }

                                        if (mBluetoothClient != null) {
                                            mBluetoothClient.onBluetoothStateOnDisconnected();
                                        }
                                    } else {
                                        if (mBluetoothClient != null) {
                                            mBluetoothClient.onBluetoothStateOnConnected();
                                        }
                                    }
                                }
                            }, 1000);

                        } catch (Exception e) {
                            ModiLog.e("try to reconnect Error");
                        }

                        break;

                    case BluetoothAdapter.STATE_TURNING_ON:
                        ModiLog.d("Bluetooth is TURNING ON");

                        if (mBluetoothClient != null) {
                            // mBluetoothClient.onBluetoothEnabled();
                        }
                        break;

                    case BluetoothAdapter.ERROR:
                        if (mBluetoothClient != null) {
                            mBluetoothClient.onBluetoothError();
                        }
                        break;

                    default:
                        if (mBluetoothClient != null) {
                            mBluetoothClient.onBluetoothStateUnknown(state);
                        }
                        break;
                }
            }
        }
    };

    /**
     * set ModiClient
     *
     * @param client ModiClient
     */
    public void setClient(ModiClient client) {
        if (mModiClient != null) {
            mModiClient = null;
        }
        mModiClient = client;
    }

    /**
     * set Context(getApplicationContext)
     *
     * @param context
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * initialize class object
     *
     * @param context Context
     * @param client  ModiClient
     * @return Bluetooth enabled
     */
    public boolean init(Context context, ModiClient client) {
        init(context, null, client);

        return isSupportedBluetooth();
    }

    /**
     * initialize class object
     *
     * @param context       Context
     * @param stateListener ManagerStateListener
     * @return Bluetooth enabled
     */
    public boolean init(Context context, ManagerStateListener stateListener) {
        return init(context, stateListener, null);
    }

    /**
     * initialize class object
     *
     * @param context       Context
     * @param stateListener ManagerStateListener
     * @param client        ModiClient
     * @return Bluetooth enabled
     */
    public boolean init(Context context, @Nullable ManagerStateListener stateListener, @Nullable ModiClient client) {

        setContext(context);
        setBluetoothAdapter();

        isConnected = false;
        disconnectedCount = 0;

        if (stateListener != null) {
            setServiceStateClient(stateListener);
        }
        if (client != null) {
            setClient(client);
        }

        if (mModiService == null) {
            boolean bindResult = bindService();

            if (!bindResult) {
                return false;
            }
        }

        context.registerReceiver(serviceReceiver, makeGattUpdateIntentFilter());
        context.registerReceiver(bluetoothAdapterReceiver, makeBluetoothAdapterIntentFilter());

        return isSupportedBluetooth();
    }

    private boolean bindService() {
        Intent serviceIntent = new Intent(context, ModiService.class);
        boolean bindResult = context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        return bindResult;
    }

    private void unbindService() throws Exception {
        context.unbindService(serviceConnection);
    }

    /**
     * set LogClient
     *
     * @param client LogClient
     */
    public void setLogClient(LogClient client) {
        if (this.mLogClient != null) {
            this.mLogClient = null;
        }
        this.mLogClient = client;
    }

    /**
     * set NotifyStateClient
     *
     * @param client NotifyStateClient
     */
    public void setNotifyStateClient(NotifyStateClient client) {
        if (this.mNotifyStateClient != null) {
            this.mNotifyStateClient = null;
        }
        this.mNotifyStateClient = client;
    }

    /**
     * set ServiceStateClient
     *
     * @param client ServiceStateClient
     */
    public void setServiceStateClient(ServiceStateClient client) {
        if (this.mServiceStateClient != null) {
            this.mServiceStateClient = null;
        }
        this.mServiceStateClient = client;
    }

    /**
     * set BluetoothClient
     *
     * @param client BluetoothClient
     */
    public void setBluetoothClient(BluetoothClient client) {
        if (this.mBluetoothClient != null) {
            this.mBluetoothClient = null;
        }
        this.mBluetoothClient = client;
    }

    /**
     * terminated
     */
    public void finish() {

        disconnectPermanently();

        try {
            unbindService();
            context.unregisterReceiver(serviceReceiver);
            context.unregisterReceiver(bluetoothAdapterReceiver);
        } catch (Exception e) {
            ModiLog.e("finish Error " + e.toString());
        } finally {
            if (bleServiceHandler != null) {
                bleServiceHandler.removeCallbacksAndMessages(null);
            }
            if (checkRequestHandler != null) {
                checkRequestHandler.removeCallbacksAndMessages(null);
            }
        }
    }


    /**
     * Check for Bluetooth support after initialize
     *
     * @return Bluetooth enabled
     */
    private boolean isSupportedBluetooth() {
        if (this.bluetoothAdapter == null) {
            return false;
        } else {
            return true;
        }
    }


    private final int scanDelayTime = 100;

    /**
     * start device scan
     *
     * @return
     */
    public boolean scan() {
        if (ScanningTimes == 4) {
            ModiLog.d("Scan too frequently");
            return false;
        }
        if (MODIBackgroundThread.getState() == Thread.State.NEW) {
            MODIBackgroundThread.start();
        }

        setIsDisconnectPermanently(true);
        if (disconnectTimeoutHandler != null) {
            disconnectTimeoutHandler.removeCallbacksAndMessages(null);
        }

        boolean result = true;
        if (isScanning()) {
            ScanningTimes++;
            result = this.stopScan();
            setIsScanning(true);
        }

        try {
            if (scanForConnectHandler != null) {
                scanForConnectHandler.removeCallbacksAndMessages(null);
            }

            if (scanForConnectHandler == null) {
                scanForConnectHandler = new Handler(MODIBackgroundThread.getLooper());
            }
        } catch (Exception e) {
            ModiLog.e(e.toString());
        } finally {
            if (scanForConnectHandler == null) {
                scanForConnectHandler = new Handler();
            }
        }

        scanForConnectHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                startScan();
                scanForConnectHandler.removeCallbacksAndMessages(null);
            }
        }, rescanInterval);
        return result;
    }


    private Handler scanForConnectHandler;
    private int rescanInterval = 500;

    /**
     * Connect as soon as the specified device is detected
     *
     * @return connect result
     */
    public boolean scanForConnect() {
        scanForConnectHandler = new Handler(MODIBackgroundThread.getLooper());
        setIsDisconnectPermanently(false);
        if (disconnectTimeoutHandler != null) {
            disconnectTimeoutHandler.removeCallbacksAndMessages(null);
        }

        boolean result = false;
        if (isScanning()) {
            result = this.stopScan();
        }

        try {
            if (scanForConnectHandler == null) {
                scanForConnectHandler = new Handler(context.getMainLooper());
            }
        } catch (Exception e) {
            ModiLog.e("scanForConnect Error " + e.toString());
        } finally {
            if (scanForConnectHandler == null) {
                scanForConnectHandler = new Handler(MODIBackgroundThread.getLooper());
            }
        }

        scanForConnectHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                startScan();
                scanForConnectHandler.removeCallbacksAndMessages(null);
            }
        }, rescanInterval);

        return result;
    }

    /**
     * Automatically connect after device discovery
     *
     * @param deviceAddress
     * @return connect result
     */
    public boolean scanForConnect(String deviceAddress) {
        deviceInformation.deviceAddress = deviceAddress;
        return scanForConnect();
    }

    /**
     * Automatically connect after device discovery
     *
     * @param deviceAddress
     * @param connectionCallback ConnectionCallback
     * @return connect result
     */
    public boolean scanForConnect(String deviceAddress, ConnectionCallback connectionCallback) {
        deviceInformation.deviceAddress = deviceAddress;
        setConnectionCallback(connectionCallback);
        return scanForConnect();
    }


    /**
     * 스캔 실패 시 5회 재시도
     */
    private int scanFailCount = 0;

    private boolean onFailureScanning() {

        scanFailCount++;

        if (scanFailCount <= 5) {
            if (isScanning() == false && isConnected() == false) {
                mModiService.close(new GattCloseListener() {

                    @Override
                    public void onClosedBluetoothGatt() {
                        if (isScanning() == false && isConnected() == false) {
                            ModiLog.d("Restart Scanning Bluetooth Device");
                            if (isDisconnectPermanently) {
                                scan();
                            } else {
                                //scanForConnect();
                            }
                        }
                    }
                });
            }
            return true;
        } else {
            return false;
        }
    }


    private final int connectDelay = 2000;

    /**
     * 검색된 디바이스가 기존에 연결된 디바이스이고 연결을 명시적으로 끊지 않은 경우 디바이스 재연결, 새로 스캔한 경우에는 외부 전송
     *
     * @param device
     * @param rssi
     * @param scanRecord
     */
    private void sendFoundDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {

        String macAddress = "";

        try {
            macAddress = ManufacturerDataParser.getMacAddress(scanRecord);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isDisconnectPermanently == false && deviceInformation.deviceAddress != null) {
            if (deviceInformation.deviceAddress.equalsIgnoreCase(device.getAddress()) || deviceInformation.deviceAddress.equalsIgnoreCase(macAddress)) {
                stopScan();
                mModiClient.disconnectedByModulePowerOff();
                //autoConnnect(device.getAddress());
            }
        } else {
            if (mModiClient != null) {
                mModiClient.onFoundDevice(device, rssi, scanRecord);
            }
        }
    }

    /**
     * device autoconnect
     *
     * @param deviceAddress
     */
    private void autoConnnect(final String deviceAddress) {

        try {
            stopScan();

            new Handler(context.getMainLooper()).postDelayed(new Runnable() {

                @Override
                public void run() {
                    connect(deviceAddress);
                }
            }, connectDelay);
        } catch (Exception e) {

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    connect(deviceAddress);
                }
            }, connectDelay);
        }
    }

    /**
     * scan callback 설정
     */
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

            final String deviceName = device.getName();
            if (deviceName != null) {

                ModiLog.d("onLeScan device: " + deviceName + " address: " + device.getAddress());

                if (deviceName.toUpperCase().contains(ModiConstants.BROADCAST_NAME.toUpperCase())) {
                    sendFoundDevice(device, rssi, scanRecord);
                }
            }
        }
    };

    /**
     * 디바이스 스캔
     *
     * @return
     */
    private void setIsScanning(boolean isScanning) {
        this.scanFailCount = 0;

        ModiLog.d("Scan is " + (isScanning ? "Started" : "Stopped"));

        this.isScanning = isScanning;
        if (mModiClient != null) {
            mModiClient.onScanning(isScanning);
        }

        if (isScanning == false && isDisconnectPermanently == false && isConnected() == false) {
            // startKeepScan();
        }
    }

    private Handler bleServiceHandler;

    private void initBleServiceHandler() {

        try {
            if (bleServiceHandler == null) {
                bleServiceHandler = new Handler(MODIBackgroundThread.getLooper());
            }
        } catch (Exception e) {
            ModiLog.e("init BleServiceHandler Error " + e.toString());
        } finally {
            if (bleServiceHandler == null) {
                bleServiceHandler = new Handler();
            }
        }
    }

    /**
     * LeScanner 추가
     */
    @TargetApi(21)
    private void startScanner() {

        try {
            if (bluetoothAdapter == null) {
                setBluetoothAdapter();
            }

            if (bluetoothLeScanner == null) {
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            }

            bluetoothLeScanner.startScan(getScanCallback());
        } catch (Exception e) {
            ModiLog.e("startScanner Error " + e.toString());
        }
    }

    @TargetApi(21)
    private void stopScanner() {

        if (bluetoothLeScanner != null) {
            try {
                bluetoothLeScanner.stopScan(getScanCallback());
            } catch (Exception e) {
                ModiLog.e("stopScanner Error " + e.toString());
            }
        }
    }

    String deviceName;

    @TargetApi(21)
    private void sendScannedDevice(ScanResult result) {

        try {
            if (isScanning) {
                deviceName = result.getDevice().getName();

                if (deviceName != null) {
                    if (deviceName.toUpperCase().contains(ModiConstants.BROADCAST_NAME.toUpperCase())) {
                        ModiLog.d("isScanning " + isScanning + " ====== LeScanner device: " + deviceName + " address: " + result.getDevice().getAddress());

                        if (result.getScanRecord() != null) {
                            sendFoundDevice(result.getDevice(), 0, result.getScanRecord().getBytes());
                        } else {
                            sendFoundDevice(result.getDevice(), 0, null);
                        }
                    }
                }
            }
        } catch (Exception e) {
            ModiLog.e("sendScannedDevice Error " + e.toString());
        }
    }


    ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);

            for (ScanResult result : results) {
                sendScannedDevice(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            boolean result = onFailureScanning();
            setIsScanning(true);
            ModiLog.d("Bluetooth Scan is Failed, calling onFailureScanning is " + result);
        }

        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            new Handler(MODIBackgroundThread.getLooper()).post(new Runnable() {
                @Override
                public void run() {
                    sendScannedDevice(result);
                }
            });
        }
    };

    @TargetApi(21)
    private ScanCallback getScanCallback() {
        return scanCallback;
    }

    /**
     * 내부 스캔 시작 메서드
     */
    private void startDefaultScan() {

        try {
            if (bluetoothAdapter == null) {
                setBluetoothAdapter();
            }

            // 버전 21 이상 부터는 leScanner 사용
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                bluetoothAdapter.startLeScan(leScanCallback);
            } else {
                startScanner();
            }
        } catch (Exception e) {
            ModiLog.e("startDefaultScan Error " + e.toString());
        }
    }

    /**
     * 내부 스캔 종료 메서드
     */
    private void stopDefaultScan() {

        try {
            if (bluetoothAdapter == null) {
                setBluetoothAdapter();
            }

            if (isScanning) {

                // 버전 21 이상 부터는 leScanner 사용
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    bluetoothAdapter.stopLeScan(leScanCallback);
                } else {
                    stopScanner();
                }
                setIsScanning(false);
            }
        } catch (Exception e) {
            ModiLog.e("stopDefaultScan Error " + e.toString());
        }
    }

    private boolean startScan() {

        ModiLog.d("Start Scan");
        if (bluetoothAdapter == null) {
            setBluetoothAdapter();
        }

        if (isSupportedBluetooth()) {

            setIsScanning(true);
            if (bleServiceHandler == null) {
                initBleServiceHandler();
            }

            bleServiceHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    startDefaultScan();
                    bleServiceHandler.removeCallbacksAndMessages(null);
                    stopScanAutomaticly();
                }

            }, scanDelayTime);
            return true;
        } else {
            ModiLog.d("Fail to Start Scan: no BluetoothAdapter");
            return false;
        }
    }

    /**
     * 주기적으로 디바이스 스캔
     *
     * @return 주기적으로 디바이스 스캔 여부
     */
    private boolean startKeepScan() {

        ModiLog.d("Start Keep Scan");
        if (bluetoothAdapter == null) {
            setBluetoothAdapter();
        }

        if (isSupportedBluetooth()) {
            setIsScanning(true);
            if (bleServiceHandler == null) {
                initBleServiceHandler();
            }
            bleServiceHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    startDefaultScan();
                    bleServiceHandler.removeCallbacksAndMessages(null);
                    stopScanAutomaticly();
                }
            }, 2000);
            return true;
        } else {
            ModiLog.d("Fail to Start Keep Scan: no BluetoothAdapter");
            return false;
        }
    }

    /**
     * 디바이스 스캔 자동 종료(스캔 시작 후 30초)
     *
     * @return 블루투스 지원 여부 or 스캔 중 여부
     */
    private boolean stopScanAutomaticly() {

        if (bluetoothAdapter == null) setBluetoothAdapter();

        if (isSupportedBluetooth() && isScanning) {

            if (bleServiceHandler == null) initBleServiceHandler();
            bleServiceHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    ScanningTimes = 0;
                    stopDefaultScan();
                    bleServiceHandler.removeCallbacksAndMessages(null);
                }
            }, SCAN_PERIOD);

            return true;
        } else {

            return false;
        }
    }

    /**
     * stop device scan immediately
     *
     * @return bluetooth enabled or device scanning status
     */
    public boolean stopScan() {

        if (bluetoothAdapter == null) setBluetoothAdapter();

        if (isSupportedBluetooth() && isScanning) {

            if (bleServiceHandler == null) initBleServiceHandler();

            stopDefaultScan();
            bleServiceHandler.removeCallbacksAndMessages(null);

            return true;
        } else {

            return false;
        }
    }

    /**
     * device scanning status
     *
     * @return device scanning status
     */
    public boolean isScanning() {
        return isScanning;
    }

    /**
     * recently connected device exists
     *
     * @return Is a connected device present?
     */
    public boolean hasDeviceAddress() {

        ModiPreference preference = ModiPreference.getInstance();
        preference.init(context);

        return (preference.getDeviceAddress() == null);
    }


    private Handler connectHandler;

    /**
     * connect device
     *
     * @param deviceAddress
     */
    public void connect(final String deviceAddress) {

        setIsDisconnectPermanently(false);
        stopScan();
        if (!isConnected()) {

            try {

                if (mModiService != null) {

                    if (deviceAddress != null && (deviceAddress.trim()).length() > 0) {

                        deviceInformation.deviceAddress = deviceAddress;

                        if (connectHandler == null)
                            connectHandler = new Handler(MODIBackgroundThread.getLooper());

                        connectHandler.post(new Runnable() {

                            @Override
                            public void run() {

                                boolean result = mModiService.connect(deviceAddress);
                                ModiLog.d(String.format("Connecting to %s, request result= %b", deviceAddress, result));
                                connectHandler.removeCallbacksAndMessages(null);
                            }
                        });

                    } else {

                        ModiLog.e("No device Address");
                    }
                } else {

                    ModiLog.e("No Service");
                }
            } catch (Exception e) {

                ModiLog.e("connect Error " + e.toString());
            }
        }
    }

    /**
     * redirect recently connected devices
     *
     * @return redirection successful
     */
    public boolean connect() {

        ModiPreference preference = ModiPreference.getInstance();
        preference.init(context);
        String deviceAddress = preference.getDeviceAddress();

        if (deviceInformation.deviceAddress != null) {

            deviceAddress = deviceInformation.deviceAddress;
        }

        if (deviceAddress != null) {

            connect(deviceAddress);
            return true;
        } else {

            return false;
        }
    }

    /**
     * cancel connect (exit scan on scaning)
     */
    public void cancelConnect() {

        setIsDisconnectPermanently(true);

        if (isConnected()) {

            disconnectPermanently();
        } else if (isScanning()) {

            stopScan();
        }
    }

    private ConnectionCallback connectionCallback;

    /**
     * set ConnectionCallback
     *
     * @param connectionCallback ConnectionCallback
     */
    public void setConnectionCallback(ConnectionCallback connectionCallback) {
        this.connectionCallback = connectionCallback;
    }

    /**
     * disconnect device
     */
    public void disconnect() {
        try {
            if (mModiService != null) {
                ModiLog.d("Ask for disconnecting temporary to service...");
                mModiService.disconnect();
            }
        } catch (Exception e) {
            ModiLog.e("disconnect " + e.toString());
        }
    }

    /**
     * disconnect device
     *
     * @param connectionCallback ConnectionCallback
     */
    public void disconnect(ConnectionCallback connectionCallback) {

        setConnectionCallback(connectionCallback);
        disconnect();
    }

    private Handler disconnectTimeoutHandler;

    /**
     * disconnect permanently
     */
    public void disconnectPermanently() {

        setIsDisconnectPermanently(true);
        ModiLog.d("call disconnectPermanently");

        try {
            if (isScanning) {
                isScanning = false;
            }
            stopDefaultScan();

            if (disconnectTimeoutHandler != null) {
                disconnectTimeoutHandler.removeCallbacksAndMessages(null);
            }

            if (disconnectTimeoutHandler == null) {
                disconnectTimeoutHandler = new Handler(MODIBackgroundThread.getLooper());
            }

            if (mModiService != null) {
                ModiLog.d("Ask for disconnecting permanently to Service...");
                mModiService.isDisconnectPermanently = true;
                mModiService.disconnect();
            } else {
                ModiLog.d("ModiService is NULL");
            }
        } catch (Exception e) {
            ModiLog.e("disconnectPermanently Error " + e.toString());
        }
    }

    /**
     * disconnect permanently
     *
     * @param connectionCallback ConnectionCallback
     */
    public void disconnectPermanently(ConnectionCallback connectionCallback) {
        setConnectionCallback(connectionCallback);
        disconnectPermanently();
    }

    /**
     * Check Device Connection Status
     *
     * @return is connected ?
     */
    public boolean isConnected() {

        boolean sysemConnect = isDeviceConnectedWithSystem();

        if (sysemConnect != isConnected) {

            ModiLog.e("System Connectivity is different from App");
        }

        return isConnected;
    }

    /**
     * is device connected with system
     *
     * @return result
     */
    public boolean isDeviceConnectedWithSystem() {

        String deviceAddress = deviceInformation.deviceAddress;

        try {
            if (mModiService != null && deviceAddress != null && !"".equals(deviceAddress)) {
                boolean isDeviceConnected = mModiService.isDeviceConnected(deviceAddress);
                return isDeviceConnected;
            }
        } catch (Exception e) {
            ModiLog.e("isDeviceConnectedWithSystem Error " + e.toString());
        }

        return isConnected;
    }


    private int connectedState() {
        return mModiService.mConnectionState;
    }

    /**
     * check characteristics
     *
     * @return is discovered characteristics
     */
    public boolean isDiscoveredCharacteristics() {
        return isDiscoveredCharacteristics;
    }

    private void setDiscoveredCharacteristics(boolean isDiscoveredCharacteristics) {
        if (isDiscoveredCharacteristics == false) {
            characteristicsList.clear();
        }
        this.isDiscoveredCharacteristics = isDiscoveredCharacteristics;
    }

    private long latestBroadcastingTimestamp = 0;
    private Handler discoverServiceHandler;
    private boolean isDiscoveredServices = false;
    private int discoverServiceRetryCount = 0;

    private void discoverService() {

        ModiLog.d("call discoverService");
        try {
            if (discoverServiceHandler == null) {
                discoverServiceHandler = new Handler(context.getMainLooper());
            }
        } catch (Exception e) {
            ModiLog.e("discoverServiceHandler Error " + e.toString());
        } finally {
            if (discoverServiceHandler == null) {
                discoverServiceHandler = new Handler();
            }
        }

        discoverServiceHandler.postDelayed(new Runnable() {

            @Override
            public void run() {

                if (mModiService != null && isConnected()) {
                    isDiscoveredServices = false;
                    discoverServiceRetryCount++;
                    mModiService.discoverService();
                }

                // discoverServiceHandler.removeCallbacksAndMessages(null);
            }

        }, DISCOVER_DELAY_TIME);

        discoverServiceHandler.postDelayed(new Runnable() {

            @Override
            public void run() {

                if (isDiscoveredServices == false) {
                    if (mModiService != null && isConnected()) {
                        if (discoverServiceRetryCount <= 5) {
                            discoverService();
                        } else {
                            // 5회 시도 후 실패 시 연결 해제
                            if (mModiService.isBluetoothGattConnected()) {
                                disconnect();
                            } else {
                                if (connectionCallback != null) {
                                    connectionCallback.onDisconnectFailure();
                                }
                            }
                            discoverServiceHandler.removeCallbacksAndMessages(null);
                        }
                    }
                }
                // discoverServiceHandler.removeCallbacksAndMessages(null);
            }
        }, 5000);
    }

    private Handler discoveryHandler = new Handler();

    private void discoverCharacteristics() {

        try {
            if (discoveryHandler == null) {
                discoveryHandler = new Handler(context.getMainLooper());
            }
        } catch (Exception e) {
            ModiLog.e("init discoveryHandler Error " + e.toString());
        } finally {
            if (discoveryHandler == null) {
                discoveryHandler = new Handler();
            }
        }

        discoveryHandler.postDelayed(new Runnable() {

            @Override
            public void run() {

                discoverBluetoothCharacteristics(mModiService.getSupportedGattServices());
                discoveryHandler.removeCallbacksAndMessages(null);
            }

        }, DISCOVER_DELAY_TIME);
    }

    /**
     * 서비스 상태 및 데이터 수신
     */
    private void onChangedConnectState() {
        if (disconnectTimeoutHandler != null) {
            disconnectTimeoutHandler.removeCallbacksAndMessages(null);
        }
        if (discoverServiceHandler != null) {
            discoverServiceHandler.removeCallbacksAndMessages(null);
        }
        if (discoveryHandler != null) {
            discoveryHandler.removeCallbacksAndMessages(null);
        }
        if (recheckDisconnectHandler != null) {
            recheckDisconnectHandler.removeCallbacksAndMessages(null);
        }
        isDiscoveredServices = false;
    }


    private void onDisconnect() {

        disconnectedCount++;

        if (isDisconnectPermanently == false) {
            if (disconnectedCount >= connectingTryLimit) {
                disconnectedCount = 0;
                if (connectionCallback != null) {
                    connectionCallback.onDisconnectFailure();
                } else {
                    rescanInterval = 10000;
                    scan();
                }
            } else {
                rescanInterval = 3000;
                scan();
            }
        } else {
            deviceInformation.deviceAddress = null;
        }

        // 데이터 초기화
        deviceInformation.deinit();
        characteristicsNotifyState.clear();
        discoverServiceRetryCount = 0;

        if (mModiClient != null) {
            mModiClient.onDisconnected();
        }
    }

    /**
     * 연결 해제 재확인을 위한 핸들러
     */
    private int disconnectedCount = 0;                          // 연결 해제 시도 횟수
    private final int connectingTryLimit = 5;                   // 연결 해제 시도 제한
    private Handler recheckDisconnectHandler;                   // 연결 해제 핸들러
    private final int recheckDisconnectDelayTime = 1000;        // 연결 해제 시도 지연

    private void recheckDisconnect() {

        if (deviceInformation.deviceAddress != null) {
            try {
                boolean result = isDeviceConnectedWithSystem();
                ModiLog.d(String.format("recheckDisconnect Core Connection State: %B App Connection State: %B", result, isConnected));

                if (result == true && isConnected == false) {
                    if (connectionCallback != null) {
                        connectionCallback.onDisconnectFailure();
                    }
                }
            } catch (Exception e) {
                ModiLog.e("recheckDisconnect Error " + e.toString());
            }
        }
    }

    //--------------------------------------------------
    // 서비스 리시버
    //--------------------------------------------------
    private final BroadcastReceiver serviceReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, final Intent intent) {

            final String action = intent.getAction();
            long broadcastingTimestamp = intent.getLongExtra(ModiConstants.KEY_ARRIVED_TIMESTAMP, 0);

            if (latestBroadcastingTimestamp == broadcastingTimestamp) {
                ModiLog.d(action + " latestBroadcastingTimestamp " + latestBroadcastingTimestamp + " broadcastingTimestamp" + broadcastingTimestamp);
            } else {
                latestBroadcastingTimestamp = broadcastingTimestamp;

                //----------
                // 디바이스 연결
                //----------
                if (ModiService.ACTION_GATT_CONNECTED.equals(action)) {
                    ModiLog.d("ACTION_GATT_CONNECTED");
                    isConnected = true;
                    rescanInterval = 1000;
                    disconnectedCount = 0;

                    stopScan();
                    onChangedConnectState();
                    discoverService();
                    setIsDisconnectPermanently(false);

                    if (mModiClient != null) {
                        mModiClient.onConnected();
                    }
                }

                //----------
                // 디바이스 해제
                //----------
                else if (ModiService.ACTION_GATT_DISCONNECTED.equals(action)) {

                    ModiLog.d("ACTION_GATT_DISCONNECTED");
                    onChangedConnectState();
                    isConnected = false;
                    setDiscoveredCharacteristics(false);

                    // 연결 해제 검증
                    try {
                        if (recheckDisconnectHandler == null) {
                            recheckDisconnectHandler = new Handler(context.getMainLooper());
                        }
                    } catch (Exception e) {
                        ModiLog.d("recheckDisconnectHandler Error " + e.toString());
                    } finally {

                        if (recheckDisconnectHandler == null) {
                            recheckDisconnectHandler = new Handler();
                        }
                    }

                    recheckDisconnectHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            recheckDisconnect();
                        }
                    }, recheckDisconnectDelayTime);

                    onDisconnect();
                }

                //----------
                // 속성 발견
                //----------
                else if (ModiService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    ModiLog.d("ACTION_GATT_SERVICES_DISCOVERED");
                    discoverServiceHandler.removeCallbacksAndMessages(null);
                    isDiscoveredServices = true;
                    discoverCharacteristics();
                }

                //----------
                // 알림 변경
                //----------
                else if (ModiService.ACTION_NOTIFICATION_STATE_CHANGED.equals(action)) {
                    ModiLog.d("ACTION_NOTIFICATION_STATE_CHANGED");
                    String uuid = intent.getStringExtra(ModiConstants.KEY_UUID);

                    if (uuid != null) {
                        boolean result = putCharacteristicsNotifyState(uuid);
                        boolean enable = checkCharacteristicsNotifyState(uuid);
                        ModiLog.d("ACTION_NOTIFICATION_STATE_CHANGED " + ModiGattAttributes.lookup(uuid, "No Name") + " result " + result + " value " + enable);

                        if (mNotifyStateClient != null) {
                            mNotifyStateClient.onChangedNotificationState(Characteristics.get(uuid), enable);
                        }
                    }
                }

                //----------
                // 서비스 바인드
                //----------
                else if (ModiService.ACTION_GATT_SERVICE_BIND.equals(action)) {
                    ModiLog.d("ACTION_GATT_SERVICE_BIND");
                    if (mServiceStateClient != null) {
                        mServiceStateClient.onBind();
                    } else {
                        ModiLog.d("ServiceStateClient is NULL");
                    }
                }

                //----------
                // 서비스 바인드 해제
                //----------
                else if (ModiService.ACTION_GATT_SERVICE_UNBIND.equals(action)) {
                    ModiLog.d("ACTION_GATT_SERVICE_UNBIND");
                    if (mServiceStateClient != null) {
                        mServiceStateClient.onUnBind();
                    } else {
                        ModiLog.d("ServiceStateClient is NULL");
                    }
                }

                //----------
                // 블루투스 통신
                //----------
                else if (ModiService.ACTION_DATA_AVAILABLE.equals(action)) {
                    //ModiLog.d("ACTION_DATA_AVAILABLE");
                    Characteristics uuid = Characteristics.get(intent.getStringExtra(ModiService.EXTRA_DATA_GROUP));
                    BluetoothGattCharacteristic characteristic = characteristicsList.get(uuid);

                    if (uuid != null) {
                        // ModiLog.d( uuid.name() + ": " + intent.getStringExtra(ModiConstants.KEY_RAW_DATA));

                        try {
                            new Handler(MODIBackgroundThread.getLooper()).post(new Runnable() {

                                @Override
                                public void run() {
                                    if (mModiClient != null) {
                                        byte[] rawData = intent.getByteArrayExtra(ModiConstants.KEY_RAW_DATA_BYTE);

                                        mModiClient.onReceivedData(intent.getStringExtra(ModiConstants.KEY_RAW_DATA));
                                        mModiClient.onReceivedData(rawData);

                                        if ("Luxrobo".equals(new String(getMODI_ID()))) {
                                            //프로토콜 V1
                                            if (rawData[0] == 0x00) {
                                                int data = 0;
                                                for (int i = 0; i < 4; i++) {
                                                    data += ((int) rawData[i + 2] & 0x000000FF) << (i * 8);
                                                }
                                                mModiClient.onReceivedUserData(data);
                                            } else if (rawData[0] == 0x01) {
                                                byte compareData = rawData[2];
                                                if (rawData[1] == 0x03) {
                                                    State.Buzzer state = (compareData == 0x01) ? State.Buzzer.ON : State.Buzzer.OFF;
                                                    mModiClient.onBuzzerState(state);
                                                }
                                            }
                                        } else {
                                            //프로토콜 V2
                                            byte compareData = rawData[8];
                                            if (rawData[4] == 0x00 && rawData[5] == 0x01) {
                                                State.Buzzer state = (compareData == 0x01) ? State.Buzzer.ON : State.Buzzer.OFF;
                                                mModiClient.onBuzzerState(state);
                                            }
                                        }
                                    }
                                }
                            });
                        } catch (Exception e) {
                            ModiLog.e("receivedData Error " + e.toString());
                        }
                    }
                }

                //----------
                // 알림 변경
                //----------
                else if (ModiService.ACTION_WRITE_CHACTERISTIC.equals(action)) {
                    Characteristics uuid = Characteristics.get(intent.getStringExtra(ModiService.EXTRA_DATA_GROUP));
                    ModiLog.d(String.format("ACTION_WRITE_CHACTERISTIC %s", ModiGattAttributes.lookup(uuid.code(), "")));
                }
            }
        }
    };


    //--------------------------------------------------
    // Gatt Characteristics 저장 및 초기 셋팅
    //--------------------------------------------------
    private final int mCharacteristicsCountMax = 1;

    private void discoverBluetoothCharacteristics(List<BluetoothGattService> gattServicesList) {

        if (gattServicesList == null) {
            return;
        }

        String uuid16;

        for (BluetoothGattService gattService : gattServicesList) {

            List<BluetoothGattCharacteristic> gattCharacteristicsList = gattService.getCharacteristics();

            for (BluetoothGattCharacteristic characteristic : gattCharacteristicsList) {

                if (ModiGattAttributes.isUUIDExist(characteristic.getUuid())) {

                    uuid16 = ModiGattAttributes.convert16UUID(characteristic.getUuid());
                    characteristicsList.put(uuid16, characteristic);
                    ModiLog.d("discoverBluetoothCharacteristics " + ModiGattAttributes.lookup(uuid16, uuid16) + " count: " + characteristicsList.size());
                    final Characteristics characteristics = Characteristics.get(uuid16);

                    if (characteristics == null) {
                        continue;
                    }

                    startNotification(characteristics.code());
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            byte[] MODI = new byte[16];
                            for (int i = 0; i < 6; i++)
                                MODI[i] = 0x00;
                            MODI[6] = 0x08;
                            MODI[7] = 0x00;

                            MODI[8] = 0x02;
                            MODI[9] = 0x00;
                            MODI[10] = 0x00;
                            MODI[11] = 0x00;
                            MODI[12] = 0x00;
                            MODI[13] = 0x00;
                            MODI[14] = 0x00;
                            MODI[15] = 0x00;
                            sendData(MODI);
                        }
                    }, 1000);

                }
            }
        }

        String deviceName = null;

        try {
            deviceName = mModiService.getConnectedDeviceName();
            if (deviceName == null) deviceName = "";
        } catch (Exception e) {
            ModiLog.e("getConnectedDeviceName Error " + e.toString());
        }

        if (characteristicsList.size() < mCharacteristicsCountMax && deviceName != null && deviceName.contains("UP")) {
            ModiLog.d("No Service Found");
        } else {
            if (characteristicsList.size() < mCharacteristicsCountMax) {
                ModiLog.e("Discovered Not Enough Characteristics" + characteristicsList.size());
            } else {
                ModiLog.d("Discovered All Characteristics");
            }

            startNotifying();
            setDiscoveredCharacteristics(true);

            if (mModiClient != null) {
                mModiClient.onDiscoveredService();
            }
            if (checkRequestHandler == null) {
                checkRequestHandler = new Handler(MODIBackgroundThread.getLooper());
            }
            checkRequestHandler.postDelayed(CheckRequestRunnable, 500);
        }
    }


    /**
     * request queue handler
     */
    private final int checkRequestQueueInterval = 50;
    private final int checkRequestQueueIntervalOnGoing = 150;
    private int checkRequestTrialCount = 0;
    private Handler checkRequestHandler = null;
    private final Runnable CheckRequestRunnable = new Runnable() {

        @Override
        public void run() {

            mModiService.startQueuePulling();
            checkRequestHandler.removeCallbacksAndMessages(null);

            if (mModiService.getRequestQueueSize() == 0) {
                checkRequestTrialCount++;
            } else {
                checkRequestTrialCount = 0;
            }

            if (mModiService.getRequestQueueSize() > 0 || checkRequestTrialCount < 100) {
                if (isConnected()) {
                    checkRequestHandler.postDelayed(CheckRequestRunnable, checkRequestQueueInterval);
                }
            } else {
                if (isConnected()) {
                    checkRequestHandler.postDelayed(CheckRequestRunnable, checkRequestQueueIntervalOnGoing);
                }
            }
        }
    };

    /**
     * get Characteristic from UUID
     */
    private BluetoothGattCharacteristic getCharacteristic(String uuid16) {
        return characteristicsList.get(uuid16);
    }

    /**
     * READ CHARACTERISTICS
     *
     * @return
     */
    private void readCharacteristic(final BluetoothGattCharacteristic characteristic) {

        try {
            mModiService.addRequest(RequestQueue.REQUEST_READ, characteristic);
        } catch (Exception e) {
            ModiLog.e("readCharacteristic Error " + e);
        }
    }

    private boolean readCharacteristic(String uuid16) {

        final BluetoothGattCharacteristic characteristic = getCharacteristic(uuid16);

        if (characteristic != null) {
            readCharacteristic(characteristic);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Write CHARACTERISTICS
     */
    private void writeCharacteristic(final BluetoothGattCharacteristic characteristic) {

        try {
            mModiService.addRequest(RequestQueue.REQUEST_WRITE, characteristic);
        } catch (Exception e) {
            ModiLog.e("writeCharacteristic Error " + e);
        }
    }

    private boolean writeCharacteristic(String uuid16) {

        final BluetoothGattCharacteristic characteristic = characteristicsList.get(uuid16);

        if (characteristic != null) {
            writeCharacteristic(characteristic);
            return true;
        } else {
            return false;
        }
    }

    /**
     * set filter for BluetoothService
     *
     * @return
     */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(ModiService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(ModiService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ModiService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ModiService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(ModiService.ACTION_WRITE_CHACTERISTIC);
        intentFilter.addAction(ModiService.ACTION_NOTIFICATION_STATE_CHANGED);
        intentFilter.addAction(ModiService.ACTION_GATT_SERVICE_BIND);
        intentFilter.addAction(ModiService.ACTION_GATT_SERVICE_UNBIND);

        return intentFilter;
    }

    /**
     * set filter for BluetoothAdapter
     *
     * @return
     */
    private static IntentFilter makeBluetoothAdapterIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return intentFilter;
    }


    //--------------------------------------------------
    // 속성 알림 시작/종료
    //--------------------------------------------------
    private Boolean checkCharacteristicsNotifyState(String uuid16) {
        try {
            Boolean notifyState = characteristicsNotifyState.get(uuid16);
            return notifyState;
        } catch (Exception e) {
            ModiLog.e("checkCharacteristicsNotifyState " + ModiGattAttributes.lookup(uuid16, "No Name") + " Error" + e.toString());
        }
        return false;
    }

    private boolean putCharacteristicsNotifyState(String uuid16) {

        Boolean notifyState = getTempCharacteristicsNotifyState(uuid16);

        if (notifyState != null) {
            characteristicsNotifyState.put(uuid16, notifyState);
            characteristicsNotifyState.remove("tmp" + uuid16);
            ModiLog.d("putCharacteristicsNotifyState " + uuid16 + " notify " + notifyState.toString());
            return true;
        } else {
            ModiLog.d("putCharacteristicsNotifyState " + uuid16 + " no temp notify value!!!!!! ");
            return false;
        }
    }

    private void putTempCharacteristicsNotifyState(String uuid16, boolean notifyState) {
        characteristicsNotifyState.put("tmp" + uuid16, new Boolean(notifyState));
    }

    private Boolean getTempCharacteristicsNotifyState(String uuid16) {
        return characteristicsNotifyState.get("tmp" + uuid16);
    }

    /**
     * 속성 알림 시작
     *
     * @param characteristic
     */
    private void startNotification(final BluetoothGattCharacteristic characteristic) {

        try {
            String uuid16 = ModiGattAttributes.convert16UUID(characteristic.getUuid());
            Boolean isEnable = checkCharacteristicsNotifyState(uuid16);

            if (isEnable == null || isEnable == false) {
                putTempCharacteristicsNotifyState(uuid16, true);
                ModiLog.d("startNotification " + ModiGattAttributes.lookup(uuid16, "no name"));
                mModiService.addRequest(RequestQueue.REQUEST_NOTIFY, characteristic, true);
            } else {
                ModiLog.d("startNotification is not operated, " + ModiGattAttributes.lookup(uuid16, "no name") + " is already enable");
            }
        } catch (Exception e) {
            ModiLog.e("startNotification Error " + e);
        }
    }

    /**
     * 속성 알림 시작
     *
     * @param uuid16
     * @return
     */
    private boolean startNotification(String uuid16) {

        final BluetoothGattCharacteristic characteristic = getCharacteristic(uuid16);

        if (characteristic != null) {
            startNotification(characteristic);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 속성 알림 시작
     *
     * @param characteristics
     * @return
     */
    private boolean startNotification(Characteristics characteristics) {

        final BluetoothGattCharacteristic characteristic = getCharacteristic(characteristics.code());

        if (characteristic != null) {
            startNotification(characteristic);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 속성 알림 종료
     *
     * @param characteristic
     */
    private void stopNotification(final BluetoothGattCharacteristic characteristic) {

        try {
            String uuid16 = ModiGattAttributes.convert16UUID(characteristic.getUuid());
            Boolean isDisable = checkCharacteristicsNotifyState(uuid16);

            if (isDisable == null || isDisable == true) {
                putTempCharacteristicsNotifyState(uuid16, false);
                ModiLog.d("stopNotification " + ModiGattAttributes.lookup(uuid16, "no name"));
                mModiService.addRequest(RequestQueue.REQUEST_NOTIFY, characteristic, false);
            } else {
                ModiLog.d("stopNotification is not operated, " + ModiGattAttributes.lookup(uuid16, "no name") + " is already disable");
            }
        } catch (Exception e) {
            ModiLog.e("stopNotification Error " + e);
        }
    }

    /**
     * 속성 알림 종료
     *
     * @param uuid16
     * @return
     */
    private boolean stopNotification(String uuid16) {

        BluetoothGattCharacteristic characteristic = getCharacteristic(uuid16);

        if (characteristic != null) {
            stopNotification(characteristic);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 속성 알림 종료
     *
     * @param characteristics
     * @return
     */
    private boolean stopNotification(Characteristics characteristics) {

        BluetoothGattCharacteristic characteristic = getCharacteristic(characteristics.code());

        if (characteristic != null) {
            stopNotification(characteristic);
            return true;
        } else {
            return false;
        }
    }

    /**
     * start notifying
     */
    public void startNotifying() {
        startNotification(Characteristics.DEVICE_CHAR_TX_RX);
    }

    /**
     * stop notifying
     */
    public void stopNotifying() {
        stopNotification(Characteristics.DEVICE_CHAR_TX_RX);
    }

    /**
     * send type and message to MODI Network Module
     *
     * @param data
     */
    public void sendData(byte[] data) {

        if (!isConnected()) {
            return;
        }

        BluetoothGattCharacteristic characteristic = characteristicsList.get(ModiGattAttributes.DEVICE_CHAR_TX_RX);

        if (characteristic != null) {
            characteristic.setValue(data);
            writeCharacteristic(characteristic);
        }
    }

    /**
     * send type and message to MODI Network Module
     *
     * @param type data type
     * @param msg  data value
     */
    public void sendData(int type, byte[] msg) {
        if (isConnected()) {
            //ID 전환
            byte ID[] = getMODI_ID();

            if ("Luxrobo".equals(new String(ID))) {
                //프로토콜 V1
                // packet[0]: 0x00(데이터), 0x01(이벤트)
                // packet[1]: 데이터 타입
                // packet[~]: 페이로드

                byte packet[] = new byte[10];
                packet[0] = 0x01;
                packet[1] = (byte) (type & 0xFF);

                for (int i = 0; i < 8; i++) {
                    packet[i + 2] = 0x00;
                }

                packet[2] = msg[0];

                sendData(packet);
            } else {
                //프로토콜 V2
                //Command,  SourceID,   DestinationID,  Length,     Data
                //00 00,    00 00,      00 00,          00 00,      00 00 00 00 00 00 00 00

                byte packet[] = new byte[16];

                //Command
                packet[0] = (byte) 0x1F;
                packet[1] = (byte) 0x00;

                //Source ID
                packet[2] = ID[0];
                packet[3] = (byte) (ID[1] & 0x0F);

                //Destination ID
                packet[4] = (byte) (type & 0xFF);
                packet[5] = (byte) ((type & 0x000000FF) >> 8);

                //Length
                packet[6] = (byte) 0x08;
                packet[7] = (byte) 0x00;

                //Data
                for (int i = 0; i < 8; i++) {
                    packet[i + 8] = msg[7 - i];
                }

                sendData(packet);
            }
        }
    }

    /**
     * send button state to MODI Network Module
     *
     * @param state button state
     */
    public void sendButtonState(State.Button state) {

        if (!isConnected()) {
            return;
        }

        if ("Luxrobo".equals(new String(getMODI_ID()))) {
            //프로토콜 V1
            byte packet[] = new byte[1];
            packet[0] = (byte) ((state == State.Button.PRESSED) ? 0x01 : 0x00);
            sendData(0, packet);
        } else {
            //프로토콜 V2
            byte packet[] = new byte[8];
            for (int i = 0; i < 8; i++) {
                packet[i] = 0x00;
            }
            packet[0] = (byte) ((state == State.Button.PRESSED) ? 0x01 : 0x00);
            sendData(2, packet);
        }
    }

    /**
     * send joystick state to MODI Network Module
     *
     * @param direction joystick direction
     */
    public void sendJoystickState(State.Joystick direction) {

        if (!isConnected()) return;

        if ("Luxrobo".equals(new String(getMODI_ID()))) {
            //프로토콜 V1
            byte packet[] = new byte[1];

            if (direction == State.Joystick.UP) {
                packet[0] = 0x01;
            } else if (direction == State.Joystick.DOWN) {
                packet[0] = 0x04;
            } else if (direction == State.Joystick.LEFT) {
                packet[0] = 0x08;
            } else if (direction == State.Joystick.RIGHT) {
                packet[0] = 0x02;
            } else {
                packet[0] = 0x00;
            }

            sendData(2, packet);
        } else {
            //프로토콜 V2
            byte packet[] = new byte[8];
            for (int i = 0; i < 8; i++) {
                packet[i] = 0x00;
            }
            packet[0] = (byte) direction.state();
            sendData(3, packet);
        }
    }

    /**
     * send user data to MODI Network Module
     *
     * @param data user data
     */
    public void sendUserData(int data) {

        if (!isConnected()) return;

        if ("Luxrobo".equals(new String(getMODI_ID()))) {
            //프로토콜 V1
            byte[] packet = new byte[10];

            for (int i = 0; i < 10; i++) {
                packet[i] = 0x00;
            }

            for (int i = 0; i < 4; i++) {
                packet[i + 2] = (byte) ((data >> (i * 8)) & 0xFF);
            }

            sendData(packet);
        } else {
            //프로토콜 V2

        }
    }


    //--------------------------------------------------
    // 파일로그
    //--------------------------------------------------
    private StringBuilder logDataBuilder;
    private static boolean NEED_FILE_LOG = ModiLog.NEED_FILE_LOG;
    private static final int FILE_LOG_MAX_LENGTH = 1024 * 1024 * 10;

    private void initLogData() {
        if (NEED_FILE_LOG) {
            if (logDataBuilder != null) {
                logDataBuilder = null;
            }
            logDataBuilder = new StringBuilder();
        }
    }

    private void deinitLogData() {
        if (NEED_FILE_LOG) {
            if (logDataBuilder.length() > 0) {
                writeLogData();
            }
            logDataBuilder = null;
        }
    }

    /**
     * save log data file
     *
     * @param data data type
     */
    public void saveLogData(String data) {

        if (NEED_FILE_LOG) {
            if (logDataBuilder == null) {
                initLogData();
            }

            try {
                logDataBuilder.append(String.format(Locale.KOREAN, "[%s] %s \r\n", ModiFileHandler.getDate(), data));

                if (logDataBuilder.length() > FILE_LOG_MAX_LENGTH) {
                    writeLogData();
                }
            } catch (Exception e) {
                ModiLog.e("saveLogData Error " + e);
            }
        }
    }

    /**
     * write log data file
     */
    private void writeLogData() {

        if (NEED_FILE_LOG) {
            final ModiFileHandler mpsFileHandler = new ModiFileHandler(context);

            try {
                new Handler(MODIBackgroundThread.getLooper()).postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (logDataBuilder != null && logDataBuilder.length() > 0) {
                            String fileName = mpsFileHandler.writeLogFile(logDataBuilder.toString(), ModiFileHandler.LOG_RX_DATA);
                            if (fileName != null) {
                                ModiLog.d("Success Wrote Log File " + fileName);
                                logDataBuilder.delete(0, logDataBuilder.length());
                            } else {
                                ModiLog.d("Failed Wrote Log File");
                            }
                        }
                    }
                }, 10);
            } catch (Exception e) {
                ModiLog.e("writeLogData Error " + e.toString());
            }
        }
    }

    /**
     * clear Data Request Queue
     */
    public void clearRequetQueue() {
        mModiService.clearQueue();
    }

    /**
     * get MODI Network Module ID
     *
     * @return MODI Network Module ID
     */
    public byte[] getMODI_ID() {
        return mModiService.getMODI_ID();
    }
}
