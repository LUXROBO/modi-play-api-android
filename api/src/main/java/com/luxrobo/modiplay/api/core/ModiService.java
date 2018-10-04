/*
 * Developement Part, LUXROBO INC., SEOUL, KOREA
 * Copyright(c) 2018 by LUXROBO Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LUXROBO Inc.
 */

package com.luxrobo.modiplay.api.core;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.luxrobo.modiplay.api.data.DeviceInformation;
import com.luxrobo.modiplay.api.listener.GattCloseListener;
import com.luxrobo.modiplay.api.queue.RequestJob;
import com.luxrobo.modiplay.api.queue.RequestQueue;
import com.luxrobo.modiplay.api.utils.ModiDateUtil;
import com.luxrobo.modiplay.api.utils.ModiLog;
import com.luxrobo.modiplay.api.utils.ModiPreference;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class ModiService extends Service {

    private final static String TAG = ModiService.class.getSimpleName();

    public final static String ACTION_GATT_CONNECTED = "com.luxrobo.modiplay.api.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.luxrobo.modiplay.api.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.luxrobo.modiplay.api.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.luxrobo.modiplay.api.ACTION_DATA_AVAILABLE";
    public final static String ACTION_WRITE_CHACTERISTIC = "com.luxrobo.modiplay.api.WRITE_CHACTERISTIC";
    public final static String ACTION_NOTIFICATION_STATE_CHANGED = "com.luxrobo.modiplay.api.NOTIFICATION_STATE_CHANGED";
    public final static String EXTRA_DATA = "com.luxrobo.modiplay.api.EXTRA_DATA";
    public final static String EXTRA_DATA_GROUP = "com.luxrobo.modiplay.api.EXTRA_DATA_GROUP";
    public final static String ACTION_GATT_SERVICE_BIND = "com.luxrobo.modiplay.api.ACTION_GATT_SERVICE_BIND";
    public final static String ACTION_GATT_SERVICE_UNBIND = "com.luxrobo.modiplay.api.ACTION_GATT_SERVICE_UNBIND";

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private BluetoothManager mBluetoothManager;                 // BLUETOOTH -
    private BluetoothAdapter mBluetoothAdapter;                 // BLUETOOTH -
    private BluetoothGatt mBluetoothGatt;                       // BLUETOOTH -

    private String mBluetoothDeviceAddress;                     // BLUETOOTH -
    private long initialServiceTimestamp = 0;                   // GATT SERVICE TIMESTEMP
    private final int closeGattInterval = 25;                 // GATT CLOSE INTERVAL

    private Handler connectBroadCastHandler = new Handler();    // 블루투스 연결 핸들러
    private Handler closeGattHandler;                           // GATT CLOSE 핸들러
    private int retrialCount = 0;                               // 명령 요청 시도 횟수
    private RequestQueue requestQueue;                          // 명령 요청 QUEUE
    private final IBinder mBinder = new LocalBinder();          // 서비스 바인더
    public int mConnectionState = STATE_DISCONNECTED;           // 블루투스 연결 상태
    public boolean isDisconnectPermanently = false;             // 재연결 여부

    private byte[] MODI_ID = new byte[2];                         //연결이후 MODI 모듈 ID 저장

    public byte[] getMODI_ID() {
        return MODI_ID;
    }              //ID 반환

    public ModiService() {
        requestQueue = RequestQueue.getInstance();
        initialServiceTimestamp = ModiDateUtil.getTimeTick();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, Service.START_NOT_STICKY, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    public boolean discoverService() {
        if (mBluetoothGatt != null) {
            return mBluetoothGatt.discoverServices();
        }
        return false;
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //specific connection error, retry to connect
            if (status == 133) {
                connect(gatt.getDevice().toString());
                ModiLog.d("error code reconnect");
                return;
            }

            String intentAction;

            if (BluetoothProfile.STATE_CONNECTED == newState) {

                DeviceInformation.getInstance().deviceName = gatt.getDevice().getName();
                DeviceInformation.getInstance().deviceAddress = gatt.getDevice().getAddress();

                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;

                final String action = intentAction;

                if (connectBroadCastHandler == null)
                    connectBroadCastHandler = new Handler(getMainLooper());

                connectBroadCastHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        broadcastUpdate(action);
                        connectBroadCastHandler.removeCallbacksAndMessages(null);
                    }
                }, closeGattInterval);

                ModiLog.i(TAG, "Connected to GATT server.");

            } else if (BluetoothProfile.STATE_DISCONNECTED == newState) {

                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                ModiLog.i(TAG, "Disconnected from GATT server.");

                final String action = intentAction;

                close(new GattCloseListener() {

                    @Override
                    public void onClosedBluetoothGatt() {
                        broadcastUpdate(action);
                        clearRequetQueue();
                    }
                });

                //----------
                // 연결이 이루어진 디바이스 주소 저장
                //----------
                if (!isDisconnectPermanently) {
                    ModiPreference preference = ModiPreference.getInstance();
                    preference.init(getApplicationContext());
                    preference.setDeviceAddress(mBluetoothDeviceAddress);
                } else {
                    ModiPreference preference = ModiPreference.getInstance();
                    preference.init(getApplicationContext());
                    preference.setDeviceAddress("");
                }

                isDisconnectPermanently = false;

            } else if (BluetoothProfile.STATE_CONNECTING == newState) {
                ModiLog.d(TAG, "Trying to Connecting...");
            } else if (BluetoothProfile.STATE_DISCONNECTING == newState) {
                ModiLog.d(TAG, "Trying to Disconnecting...");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                ModiLog.d(TAG, "onServicesDiscovered received: GATT_SUCCESS");
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                ModiLog.d(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }

            MODI_ID = characteristic.getValue();

            ModiLog.d(TAG, String.format("onCharacteristicRead %s status %d",
                    ModiGattAttributes.lookup(ModiGattAttributes.convert16UUID(characteristic.getUuid()), ""), status));
            requestQueue.doneLastRequest(characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            broadcastUpdate(ACTION_WRITE_CHACTERISTIC, characteristic);
            requestQueue.doneLastRequest(characteristic, status);
            ModiLog.d(TAG, String.format("onCharacteristicWrite %s status %d",
                    ModiGattAttributes.lookup(ModiGattAttributes.convert16UUID(characteristic.getUuid()), ""), status));
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);

            requestQueue.doneLastRequest(descriptor.getCharacteristic(), status);

            ModiLog.d(TAG, String.format("onDescriptorRead %s status %d",
                    ModiGattAttributes.lookup(ModiGattAttributes.convert16UUID(descriptor.getUuid()), ""), status));
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

            broadcastUpdate(ACTION_NOTIFICATION_STATE_CHANGED, ModiGattAttributes.convert16UUID(descriptor.getCharacteristic().getUuid()));
            requestQueue.doneLastRequest(descriptor.getCharacteristic(), status);

            ModiLog.d(TAG, String.format("onDescriptorWrite %s status %d",
                    ModiGattAttributes.lookup(ModiGattAttributes.convert16UUID(descriptor.getCharacteristic().getUuid()), ""), status));
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        intent.putExtra(ModiConstants.KEY_ARRIVED_TIMESTAMP, ModiDateUtil.getTimeTick());
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final String uuid) {
        final Intent intent = new Intent(action);
        intent.putExtra(ModiConstants.KEY_UUID, uuid);
        intent.putExtra(ModiConstants.KEY_ARRIVED_TIMESTAMP, ModiDateUtil.getTimeTick());
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        String uuid16 = ModiGattAttributes.convert16UUID(characteristic.getUuid());

        /***
         * unique broadcasting
         */
        intent.putExtra(ModiConstants.KEY_ARRIVED_TIMESTAMP, ModiDateUtil.getTimeTick());

        if (intent.getAction().equalsIgnoreCase(ACTION_WRITE_CHACTERISTIC)) {

            if (ModiGattAttributes.compareUUID(characteristic.getUuid(), ModiGattAttributes.DEVICE_CHAR_TX_RX)
                    || ModiGattAttributes.compareUUID(characteristic.getUuid(), ModiGattAttributes.DEVICE_CHAR_TX_RX)) {

                // addRequest(RequestQueue.REQUEST_READ, characteristic);
            }

        } else if (intent.getAction().equalsIgnoreCase(ACTION_NOTIFICATION_STATE_CHANGED)) {

        } else {

            if (characteristic.getValue().length > 0) {

                // DeviceInformation deviceInformation = DeviceInformation.getInstance();
                if (ModiGattAttributes.compareUUID(characteristic.getUuid(), ModiGattAttributes.DEVICE_CHAR_TX_RX)) {

                    String rawData = "";
                    final byte[] data = characteristic.getValue();

                    if (data != null && (data.length == 16 || data.length == 10)) {

                        final StringBuilder stringBuilder = new StringBuilder(data.length);
                        for (byte byteChar : data)
                            stringBuilder.append(String.format("%02X ", byteChar));

                        // [00] 타입 [00] 포멧 [00 00 00 00 00 00 00 00] payload
                        rawData = String.format("%s Raw Data: %s", ModiGattAttributes.lookup(uuid16, ""), stringBuilder.toString());

                        intent.putExtra(ModiConstants.KEY_RAW_DATA, stringBuilder.toString());
                        intent.putExtra(ModiConstants.KEY_RAW_DATA_BYTE, characteristic.getValue());
                        intent.putExtra(EXTRA_DATA_GROUP, uuid16);
                        sendBroadcast(intent);
                    }
                }
            } else {
                return;
            }
        }
    }

    public class LocalBinder extends Binder {
        ModiService getService() {
            return ModiService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        broadcastUpdate(ModiService.ACTION_GATT_SERVICE_BIND);
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        broadcastUpdate(ModiService.ACTION_GATT_SERVICE_UNBIND);
        close(null);
        return super.onUnbind(intent);
    }

    /**
     * Initialize BluetoothManager
     * @return result
     */
    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

            if (mBluetoothManager == null) {

                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            return false;
        }

        return true;
    }


    private Handler connectGattHandler;

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {

        ModiLog.d(TAG, "Try to connect " + address);

        if (mBluetoothAdapter == null || address == null) {
            ModiLog.d(TAG, "BluetoothAdapter not initialized or unspecified address.");
            initialize();
            return false;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        if (device == null) {
            ModiLog.d(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        if (connectGattHandler == null) connectGattHandler = new Handler(getMainLooper());
        final Context context = getApplicationContext();

        connectGattHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
                connectGattHandler.removeCallbacksAndMessages(null);
            }
        }, 100);

        ModiLog.d(TAG, "Trying to create a new connection. DEVICE ADDR=" + address);

        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }


    private Handler disconnectHandler;

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {

        ModiLog.d(TAG, "Received disconnecting");

        if (mBluetoothAdapter == null) {
            ModiLog.d(TAG, "BluetoothAdapter is not initialized");
            initialize();
            return;
        } else if (mBluetoothGatt == null) {
            ModiLog.d(TAG + " " + initialServiceTimestamp, "mBluetoothGatt is null");
            return;
        }

        if (disconnectHandler == null) disconnectHandler = new Handler();
        disconnectHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.disconnect();
                    ModiLog.d(TAG, "Try to Disconnecting...");
                } else {
                    ModiLog.d(TAG, "No Object for Disconnecting...");
                }

                disconnectHandler.removeCallbacksAndMessages(null);
            }
        }, closeGattInterval);
    }


    /**
     * Return mBluetoothGatt state
     */
    public boolean isBluetoothGattConnected() {
        if (mBluetoothGatt == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are released properly.
     * @param gattCloseListener GattCloseListener
     */
    public void close(final GattCloseListener gattCloseListener) {

        if (mBluetoothGatt == null) {
            return;
        }

        if (closeGattHandler == null) closeGattHandler = new Handler(getMainLooper());

        closeGattHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                boolean result = refreshGatt();
                if (result) {
                    ModiLog.d(TAG, "GATT Local Refresh Success");
                } else {
                    ModiLog.d(TAG, "GATT Local Refresh Failed");
                }

                closeGattHandler.removeCallbacksAndMessages(null);

                closeGattHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            if (mBluetoothGatt != null) {
                                mBluetoothGatt.close();
                                ModiLog.d(TAG + " " + initialServiceTimestamp, "Bluetooth GATT Closing");
                            } else {
                                ModiLog.d(TAG, "mBluetoothGatt is null, Close is not operated");
                            }

                            deinitGatt(gattCloseListener);
                        } catch (Exception e) {
                            ModiLog.e(TAG, "Close Gatt Error " + e.toString());
                        }

                        closeGattHandler.removeCallbacksAndMessages(null);

                    }
                }, closeGattInterval);
            }
        }, 100);
    }

    private Handler deinitHandler = new Handler();

    private void deinitGatt(final GattCloseListener gattCloseListener) {

        if (deinitHandler == null) deinitHandler = new Handler(getMainLooper());

        deinitHandler.postDelayed(new Runnable() {

            @Override
            public void run() {

                mBluetoothGatt = null;
                ModiLog.d(TAG + " " + initialServiceTimestamp, "Bluetooth GATT Deinitialized");

                if (gattCloseListener != null) {
                    gattCloseListener.onClosedBluetoothGatt();
                }

                deinitHandler.removeCallbacksAndMessages(null);
            }

        }, closeGattInterval);
    }

    /**
     * BluetoothGatt Refresh Device
     * @return result
     */
    public boolean refreshGatt() {

        try {
            BluetoothGatt bluetoothGatt = mBluetoothGatt;
            Method localMethod = bluetoothGatt.getClass().getMethod("refresh", new Class[0]);

            if (localMethod != null) {
                boolean result = ((Boolean) localMethod.invoke(bluetoothGatt, new Object[0])).booleanValue();
                return result;
            } else {
                ModiLog.d(TAG, "Gatt refresh is not found");
                return false;
            }

        } catch (Exception e) {
            ModiLog.e(TAG, "refreshGatt Error " + e.toString());
            return false;
        }
    }

    /**
     * Remove bond
     * @param device
     * @return result
     * @throws Exception
     */
    private boolean removeBond(BluetoothDevice device) throws Exception {

        boolean result = false;

        Method localMethod = device.getClass().getMethod("removeBond", (Class[]) null);
        result = ((Boolean) localMethod.invoke(device, (Object[]) null)).booleanValue();

        return result;
    }

    /**
     * Remove bond
     * @param deviceAddress
     * @return result
     */
    public boolean removeBond(String deviceAddress) {

        boolean result = false;

        try {
            if (mBluetoothAdapter == null) {
                initialize();
            }

            Set<BluetoothDevice> bondedDeviceList = mBluetoothAdapter.getBondedDevices();

            if (bondedDeviceList.size() > 0) {
                for (BluetoothDevice device : bondedDeviceList) {
                    if (device.getAddress().equalsIgnoreCase(deviceAddress)) {
                        ModiLog.d(TAG, "Found Bonding with " + deviceAddress + " for removing Bond");
                        result = removeBond(device);
                        break;
                    }
                }
            }

        } catch (Exception e) {
            ModiLog.e(TAG, "removeBond Error " + e.toString());
        }

        return result;
    }


    /**
     * Reflection
     * get system device connection state
     */
    private boolean isDeviceConnected(BluetoothDevice device) throws Exception {
        boolean result = false;

        Method localMethod = device.getClass().getMethod("isConnected", (Class[]) null);
        result = ((Boolean) localMethod.invoke(device, (Object[]) null)).booleanValue();

        return result;
    }

    /**
     * Return device connected state
     * @param deviceAddress
     * @return device connected state
     * @throws Exception
     */
    public boolean isDeviceConnected(String deviceAddress) throws Exception {

        boolean result = false;

        if (mBluetoothAdapter == null) {
            initialize();
        }

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        result = isDeviceConnected(device);
        // ModiLog.d(TAG, "BluetoothDevice "+ deviceAddress + " is "+  (result ? "NOT" : "") +" Disconnected with System");

        return result;
    }


    /**
     * Reflection System API
     * get BLE enable state
     *
     * @return  BLE enable state
     */
    private boolean isLeEnabled() {

        boolean result = false;

        try {
            if (mBluetoothAdapter == null) {
                initialize();
            }

            Method localMethod = mBluetoothAdapter.getClass().getMethod("isLeEnabled", (Class[]) null);
            result = ((Boolean) localMethod.invoke(mBluetoothAdapter, (Object[]) null)).booleanValue();

        } catch (Exception e) {
            ModiLog.d(TAG, "isDeviceConnected Method ERROR " + e.toString());
        }

        return result;
    }

    private boolean enableBLE() {

        boolean result = false;

        try {
            if (mBluetoothAdapter == null) {
                initialize();
            }

            Method localMethod = mBluetoothAdapter.getClass().getMethod("enableBLE", (Class[]) null);
            result = ((Boolean) localMethod.invoke(mBluetoothAdapter, (Object[]) null)).booleanValue();

        } catch (Exception e) {
            ModiLog.d(TAG, "isDeviceConnected Method ERROR " + e.toString());
        }

        return result;
    }

    private boolean disableBLE() {

        boolean result = false;

        try {
            if (mBluetoothAdapter == null) {
                initialize();
            }

            Method localMethod = mBluetoothAdapter.getClass().getMethod("disableBLE", (Class[]) null);
            result = ((Boolean) localMethod.invoke(mBluetoothAdapter, (Object[]) null)).booleanValue();
        } catch (Exception e) {
            ModiLog.d(TAG, "disableBLE Error " + e.toString());
        }

        return result;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        try {
            if (mBluetoothAdapter == null || mBluetoothGatt == null) {
                ModiLog.d(TAG, "BluetoothAdapter not initialized");
                return;
            }
            mBluetoothGatt.readCharacteristic(characteristic);
        } catch (Exception e) {
            ModiLog.e(TAG, "readCharacteristic " + e.toString());
        }

    }

    /**
     * Request a write on a given {@code BluetoothGattCharacteristic}. The write result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicWrite(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     * @param characteristic The characteristic to write  from.
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            ModiLog.d(TAG, "BluetoothAdapter not initialized");
            return;
        }

        String uuid16 = ModiGattAttributes.convert16UUID(characteristic.getUuid());
        String rawData;

        try {
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(Byte.toString(byteChar) + " ");

                rawData = "writeCharacteristic " + ModiGattAttributes.lookup(uuid16, "") + "\nRaw Data: " + stringBuilder.toString();
            } else {
                rawData = "writeCharacteristic " + ModiGattAttributes.lookup(uuid16, "") + "\nRaw Data: No Data";
            }

        } catch (Exception e) {
            ModiLog.e(TAG, "writeCharacteristic Raw Data Parsing Error from " + ModiGattAttributes.lookup(uuid16, ""));
            rawData = "writeCharacteristic " + ModiGattAttributes.lookup(uuid16, "") + "\nRaw Data: No Data";
        }

        ModiLog.d(TAG + " " + initialServiceTimestamp, rawData);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(final BluetoothGattCharacteristic characteristic, boolean enabled) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null || characteristic == null) {
            return;
        }

        try {
            mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
            mBluetoothGatt.readCharacteristic(characteristic);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(ModiGattAttributes.conver128UUID(ModiGattAttributes.DEVICE_TX_DESC)));

            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }
        } catch (Exception e) {

            ModiLog.e(TAG, "setCharacteristicNotification Error " + e.toString());
        }
    }

    /**
     * Bluetooth GATT Service
     * @param uuid Service UUID
     * @return Bluetooth GATT Service
     */
    public BluetoothGattService getService(UUID uuid) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {

            return null;
        }

        return mBluetoothGatt.getService(uuid);
    }


    /**
     * Write BLE Descriptor
     * @param descriptor
     */
    public void writeDescriptor(BluetoothGattDescriptor descriptor) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {

            return;
        }

        mBluetoothGatt.writeDescriptor(descriptor);
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {

        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    /**
     * Add Characteristic Request on queue
     * @param method RequestQueue READ/WRITE/NOTIFY
     * @param characteristic BluetoothGattCharacteristic
     */
    public void addRequest(String method, BluetoothGattCharacteristic characteristic) {
        requestQueue.putRequest(method, characteristic);
    }

    /**
     * Add Characteristic Request on queue
     * @param method RequestQueue READ/WRITE/NOTIFY
     * @param characteristic BluetoothGattCharacteristic
     * @param notify is nofity
     */
    public void addRequest(String method, BluetoothGattCharacteristic characteristic, boolean notify) {
        requestQueue.putRequest(method, characteristic, notify);
    }

    private void getRequest() {

        RequestJob requestJob = requestQueue.getRequest();

        try {
            if (requestJob != null) {
                if (RequestQueue.REQUEST_WRITE.equals(requestJob.method)) {
                    writeCharacteristic(requestJob.characteristic);
                } else {
                    setCharacteristicNotification(requestJob.characteristic, requestJob.notify);
                }
            }
        } catch (Exception e) {
            ModiLog.e(TAG, "getRequest Error " + e.toString());
        }
    }

    /**
     * Clear requestQueue
     */
    private void clearRequetQueue() {
        requestQueue.clear();
    }

    /**
     * Check Whether command remains in request Queue
     * @return result
     */
    private boolean hasRequestJob() {
        return requestQueue.hasJob();
    }

    /**
     * Get requestQueue size
     * @return requestQueue size
     */
    public int getRequestQueueSize() {
        return requestQueue.size();
    }

    /**
     * Start Queue Pulling
     */
    public void startQueuePulling() {
        try {
            if (hasRequestJob()) {
                if (requestQueue.getLastRequestJob() == null) {
                    retrialCount = 0;
                    getRequest();
                } else {
                    retrialCount++;
                    if (retrialCount > 5) {
                        String uuid16 = ModiGattAttributes.convert16UUID(requestQueue.getLastRequestJob().characteristic.getUuid());
                        requestQueue.doneLastRequest(requestQueue.getLastRequestJob().characteristic, 99);
                        ModiLog.e(TAG, "Request Failed " + ModiGattAttributes.lookup(uuid16, uuid16) + " jobs");
                        retrialCount = 0;
                    }
                }
                ModiLog.d(TAG, "Request Queue has " + requestQueue.size() + " jobs");
            }
        } catch (Exception e) {
            ModiLog.e(TAG, "startQueuePulling Error " + e.toString());
        }
    }

    /**
     * Clear requestQueue
     */
    public void clearQueue() {
        try {
            if (hasRequestJob()) {
                requestQueue.clear();
            }
        } catch (Exception e) {
            ModiLog.e(TAG, "clearQueue Error " + e.toString());
        }
    }

    /**
     * Check if there are pending commands
     * @return result
     */
    public boolean onQueueProcess() {
        return requestQueue.onProcess();
    }

    /**
     * Get Connected Device Name
     * @return device name
     */
    public String getConnectedDeviceName() {

        try {
            if (mBluetoothGatt != null) {
                if (mBluetoothAdapter == null) {
                    initialize();
                }
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(DeviceInformation.getInstance().deviceAddress);
                return device.getName();
            }
        } catch (Exception e) {
            ModiLog.e(TAG, "getConnectedDeviceName Error " + e.toString());
        }
        return null;
    }
}
