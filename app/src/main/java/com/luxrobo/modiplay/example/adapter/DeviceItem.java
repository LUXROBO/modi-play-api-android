package com.luxrobo.modiplay.example.adapter;

public class DeviceItem {

    public String deviceName;
    public String deviceAddress = "";

    public void setDeviceName(String name) {
        deviceName = name;
    }

    public void setDeviceAddress(String address) {
        deviceAddress = address;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public String getDeviceAddress() {
        return this.deviceAddress;
    }
}
