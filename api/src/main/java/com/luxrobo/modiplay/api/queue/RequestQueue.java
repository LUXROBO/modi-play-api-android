/*
 * Developement Part, LUXROBO INC., SEOUL, KOREA
 * Copyright(c) 2018 by LUXROBO Inc.
 *
 * All rights reserved. No part of this work may be reproduced, stored in a
 * retrieval system, or transmitted by any means without prior written
 * Permission of LUXROBO Inc.
 */

package com.luxrobo.modiplay.api.queue;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.ArrayList;


public class RequestQueue {

    public static final String REQUEST_READ = "REQUEST_READ";
    public static final String REQUEST_WRITE = "REQUEST_WRITE";
    public static final String REQUEST_NOTIFY = "REQUEST_NOTIFY";

    private int doIndex = 0;
    private RequestJob lastRequestJob = null;
    private ArrayList<RequestJob> requestList;

    private RequestQueue() {
        requestList = new ArrayList<RequestJob>();
    }

    private static class Singleton {
        private static final RequestQueue instance = new RequestQueue();
    }

    public static RequestQueue getInstance() {
        return Singleton.instance;
    }

    public void putRequest(String method, BluetoothGattCharacteristic characteristic) {
        RequestJob requestJob = new RequestJob();
        requestJob.method = method;
        requestJob.characteristic = characteristic;
        requestList.add(requestJob);
    }

    public void putRequest(String method, BluetoothGattCharacteristic characteristic, boolean notify) {
        RequestJob requestJob = new RequestJob();
        requestJob.method = method;
        requestJob.notify = notify;
        requestJob.characteristic = characteristic;
        requestList.add(requestJob);
    }

    public void putRequest(final RequestJob requestJob) {
        requestList.add(requestList.size(), requestJob);
    }

    public RequestJob getLastRequestJob() {
        return lastRequestJob;
    }

    private void setLastRequestJob(RequestJob lastRequestJob) {
        this.lastRequestJob = lastRequestJob;
    }

    public void doneLastRequest(final BluetoothGattCharacteristic characteristic, int returnCode) {
        if (characteristic.getUuid().equals(lastRequestJob.characteristic.getUuid())) {
            if (returnCode != 0) {
                lastRequestJob.trial = lastRequestJob.trial + 1;

                if (this.lastRequestJob.trial <= 10) {
                    putRequest(this.lastRequestJob);
                }
            }

            this.lastRequestJob = null;
        }
    }

    public boolean onProcess() {
        if (this.lastRequestJob != null) {
            return true;
        } else {
            return false;
        }
    }

    public RequestJob getRequest() {
        if (doIndex > 0) {
            requestList.remove(0);
        }

        if (size() > 0) {
            doIndex = 1;
            setLastRequestJob(requestList.get(0));
            return getLastRequestJob();
        } else {
            doIndex = 0;
            return null;
        }
    }

    public int size() {
        return requestList.size();
    }

    public boolean hasJob() {
        return !requestList.isEmpty();
    }

    public void clear() {
        requestList.clear();
    }
}
