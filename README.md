MODI Play API
===============

[![](https://jitpack.io/v/LUXROBO/modi-play-api-android.svg)](https://jitpack.io/#LUXROBO/modi-play-api-android)


Easy😆 and fast💨 MODI Play API.


* Free software: MIT license
* Documentation: https://luxrobo.github.io/modi-play-api-android/index.html


Quickstart
-------

Install the latest MODI Play API if you haven't installed it yet

Add it in your root build.gradle at the end of repositories:

```gradle
allprojects {
        repositories {
                ...
                maven { url 'https://jitpack.io' }
        }
}
```

Add the dependency

```gradle
dependencies {
        implementation 'com.github.LUXROBO:modi-play-api-android:0.1.0'
}
```

Import `modiplay.api` package and create `ModiManager` Object::

```java
import com.luxrobo.modiplay.api.core.ModiManager;
...
...
private ModiManager mModiManager = ModiManager.getInstance();
```

Initialize ModiManager ::

```java
mModiManager.init(getApplicationContext(), mModiClient);

private ModiClient mModiClient = new ModiClient() {

        @Override
        public void onFoundDevice(final BluetoothDevice device, int rssi, byte[] scanRecord) {

        }

        @Override
        public void onDiscoveredService() {

        }

        @Override
        public void onConnected() {

        }

        @Override
        public void onDisconnected() {
                
        }

        @Override
        public void onScanning(boolean isScaning) {

        }

        @Override
        public void onReceivedData(String data) {

        }

        @Override
        public void onReceivedData(byte[] data) {

        }

        @Override
        public void onReceivedUserData(int data) {

        }

        @Override
        public void onBuzzerState(State.Buzzer state) {

        }

        @Override
        public void onOffEvent() {

        }

        @Override
        public void disconnectedByModulePowerOff() {
                
        }
};
```

Scan and Connect::
```java
mModiManager.scan();
...
...
mModiManager.connect(deviceAddress);
```


Send Button, Joystick State to MODI Network Module::
```java
// send joystick state
mModiManager.sendJoystickState(State.Joystick.UP);
mModiManager.sendJoystickState(State.Joystick.DOWN);
mModiManager.sendJoystickState(State.Joystick.LEFT);
mModiManager.sendJoystickState(State.Joystick.RIGHT);
mModiManager.sendJoystickState(State.Joystick.UNPRESSED);

// send button state
mModiManager.sendButtonState(State.Button.PRESSED);
mModiManager.sendButtonState(State.Button.UNPRESSED);
```