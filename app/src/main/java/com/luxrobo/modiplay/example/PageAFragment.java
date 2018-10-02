package com.luxrobo.modiplay.example;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.luxrobo.modiplay.api.core.ModiManager;
import com.luxrobo.modiplay.example.adapter.DeviceItem;
import com.luxrobo.modiplay.example.adapter.DeviceListAdapter;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class PageAFragment extends Fragment {

    private ListView bluetoothListView;
    public TextView selectedItemTextView;
    public Button scanButton;
    public Button disconnectButton;

    public ModiManager mModiManager;

    private ArrayList<DeviceItem> mDeviceList;
    private DeviceListAdapter mDeviceListAdapter;
    private DeviceItem mConnectedDevice;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_a, container, false);

        bluetoothListView = (ListView) view.findViewById(R.id.bluetooth_list);
        selectedItemTextView = (TextView) view.findViewById(R.id.connected_text);
        scanButton = (Button) view.findViewById(R.id.scan_button);
        disconnectButton = (Button) view.findViewById(R.id.disconnect_button);

        mModiManager = ModiManager.getInstance();

        mDeviceList = new ArrayList<DeviceItem>();
        mDeviceListAdapter = new DeviceListAdapter(getContext(), R.layout.item_device_search_list, mDeviceList);

        bluetoothListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                final DeviceItem selectedItem = (DeviceItem) adapterView.getItemAtPosition(position);

                openAlertPopup(selectedItem.getDeviceName().toUpperCase() + "와\n연결 하시겠습니까?", new AlertListener() {
                    @Override
                    public void onOkClick() {
                        selectedItemTextView.setText("연결중");
                        mConnectedDevice = selectedItem;
                        mModiManager.connect(selectedItem.getDeviceAddress());
                    }

                    @Override
                    public void onCancelClick() {

                    }
                });
            }
        });

        bluetoothListView.setAdapter(mDeviceListAdapter);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDeviceList.clear();
                mDeviceListAdapter.notifyDataSetChanged();

                mModiManager.scan();
            }
        });

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mModiManager.disconnect();
            }
        });

        return view;
    }

    public void startScanning() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mDeviceList.clear();
                selectedItemTextView.setText("검색중");
            }
        });
    }

    public void stopScanning() {
    }

    public void onConnected() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                selectedItemTextView.setText(mConnectedDevice.getDeviceName().toUpperCase());

                scanButton.setVisibility(View.GONE);
                disconnectButton.setVisibility(View.VISIBLE);
            }
        });
    }

    public void onDisconnected() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                selectedItemTextView.setText("disconnected");

                scanButton.setVisibility(View.VISIBLE);
                disconnectButton.setVisibility(View.GONE);
            }
        });
    }

    public void onFoundDevice(DeviceItem device) {
        boolean exist = false;

        for (DeviceItem dao : mDeviceList) {
            if (dao.deviceName.toUpperCase().equals(device.deviceName.toUpperCase())) {
                exist = true;
                break;
            }
        }

        if (!exist) {
            mDeviceList.add(device);
            mDeviceListAdapter.notifyDataSetChanged();
        }
    }

    public void bluetoothOff() {
        openConfirmPopup("블루투스 연결을 확인해 주세요.", new ConfirmListener() {
            @Override
            public void onOkClick() {

            }
        });
        mDeviceList.clear();
        stopScanning();
    }

    public void disconnectedByModuleOff() {
        mDeviceList.clear();
        stopScanning();
    }

    public void openAlertPopup(final String message, final AlertListener lister) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("알림");
        builder.setMessage(message);
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        lister.onOkClick();
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        lister.onCancelClick();
                    }
                });
        builder.show();
    }

    public void openConfirmPopup(final String message, final ConfirmListener lister) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("알림");
        builder.setMessage(message);
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        lister.onOkClick();
                    }
                });
        builder.show();
    }

    public abstract class AlertListener {
        public abstract void onOkClick();

        public abstract void onCancelClick();
    }

    public abstract class ConfirmListener {
        public abstract void onOkClick();
    }
}
