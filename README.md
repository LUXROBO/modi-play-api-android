MODI Play API
===============

[![](https://jitpack.io/v/LUXROBO/modi-play-api-android.svg)](https://jitpack.io/#LUXROBO/modi-play-api-android)


Easy😆 and fast💨 MODI Play API.


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
        implementation 'com.github.LUXROBO:modi-play-api-android:0.0.0'
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
        public void onBuzzerState(int state) {

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
mModiManager.sendJoystickState(ModiManager.STATE_JOYSTICK_UP);
mModiManager.sendJoystickState(ModiManager.STATE_JOYSTICK_DOWN);
mModiManager.sendJoystickState(ModiManager.STATE_JOYSTICK_LEFT);
mModiManager.sendJoystickState(ModiManager.STATE_JOYSTICK_RIGHT);
mModiManager.sendJoystickState(ModiManager.STATE_JOYSTICK_UNPRESSED);

// send button state
mModiManager.sendButtonState(ModiManager.STATE_BUTTON_PRESSED);
mModiManager.sendButtonState(ModiManager.STATE_BUTTON_UNPRESSED);
```