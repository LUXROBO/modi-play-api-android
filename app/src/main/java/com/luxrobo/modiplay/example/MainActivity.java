package com.luxrobo.modiplay.example;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.luxrobo.modiplay.api.client.ModiClient;
import com.luxrobo.modiplay.api.client.NotifyStateClient;
import com.luxrobo.modiplay.api.core.ModiManager;
import com.luxrobo.modiplay.api.enums.Characteristics;
import com.luxrobo.modiplay.api.listener.ManagerStateListener;
import com.luxrobo.modiplay.api.utils.ModiLog;
import com.luxrobo.modiplay.example.adapter.DeviceItem;

public class MainActivity extends AppCompatActivity {


    private Fragment mFragments[];
    private Fragment active;
    final FragmentManager fm = getSupportFragmentManager();

    public ModiManager mModiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragments = new Fragment[3];
        mFragments[0] = new PageAFragment();
        mFragments[1] = new PageBFragment();
        mFragments[2] = new PageCFragment();
        active = mFragments[0];

        initialize();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fm.beginTransaction().add(R.id.main_container, mFragments[2], "3").hide(mFragments[2]).commit();
        fm.beginTransaction().add(R.id.main_container, mFragments[1], "2").hide(mFragments[1]).commit();
        fm.beginTransaction().add(R.id.main_container, mFragments[0], "1").commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_bluetooth:
                    fm.beginTransaction().hide(active).show(mFragments[0]).commit();
                    active = mFragments[0];
                    return true;
                case R.id.navigation_modiplay:
                    fm.beginTransaction().hide(active).show(mFragments[1]).commit();
                    active = mFragments[1];
                    return true;
                case R.id.navigation_user:
                    fm.beginTransaction().hide(active).show(mFragments[2]).commit();
                    active = mFragments[2];
                    return true;
            }
            return false;
        }
    };

    private void initialize() {

        mModiManager = ModiManager.getInstance();
        boolean initResult = mModiManager.init(getApplicationContext(),
                new ManagerStateListener() {
                    @Override
                    public void onCompletedToInitialize() {
                        ModiLog.d("Manager Initialized");
                    }

                    @Override
                    public void onCompletedToDeinitialize() {
                        ModiLog.d("Manager Deinitialized");
                    }
                });

        mModiManager.setClient(mModiClient);

        if (initResult) {

            mModiManager.setNotifyStateClient(new NotifyStateClient() {
                @Override
                public void onChangedNotificationState(Characteristics characteristics, boolean enable) {
                    ModiLog.d("onChangedNotificationState " + characteristics.name() + " " + enable);
                }
            });

            mModiManager.scan();
        }
    }

    //--------------------------------------------------
    // CALLBACK
    //--------------------------------------------------
    private ModiClient mModiClient = new ModiClient() {

        @Override
        public void onFoundDevice(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    DeviceItem foundDevice = new DeviceItem();
                    foundDevice.setDeviceName(device.getName());
                    foundDevice.setDeviceAddress(device.getAddress());
                    ((PageAFragment) mFragments[0]).onFoundDevice(foundDevice);
                }
            });
        }

        @Override
        public void onDiscoveredService() {

        }

        @Override
        public void onConnected() {
            ModiLog.d("onConnected");
            ((PageAFragment) mFragments[0]).onConnected();
        }

        @Override
        public void onDisconnected() {
            ModiLog.d("onDisconnected");
            ((PageAFragment) mFragments[0]).onDisconnected();
        }

        @Override
        public void onScanning(boolean isScaning) {
            if (isScaning) {
                ((PageAFragment) mFragments[0]).startScanning();
            } else {
                ((PageAFragment) mFragments[0]).stopScanning();
            }
        }

        @Override
        public void onReceivedData(String data) {
            ModiLog.d("onReceivedData: " + data);
        }

        @Override
        public void onReceivedData(byte[] data) {

        }

        @Override
        public void onReceivedUserData(int data) {
            ((PageCFragment) mFragments[2]).onReceivedUserData(data);
        }

        @Override
        public void onBuzzerState(int state) {
            ((PageBFragment) mFragments[1]).onBuzzerState(state);
        }

        @Override
        public void onOffEvent() {
            ((PageAFragment) mFragments[0]).bluetoothOff();
        }

        @Override
        public void disconnectedByModulePowerOff() {
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    ((PageAFragment) mFragments[0]).disconnectedByModuleOff();
                }
            });
        }
    };
}
