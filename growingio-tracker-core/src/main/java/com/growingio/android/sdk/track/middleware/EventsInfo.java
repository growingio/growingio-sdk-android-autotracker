package com.growingio.android.sdk.track.middleware;

import java.util.Arrays;

public class EventsInfo {

    private String eventType;
    private int policy;
    private byte[] data;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public int getPolicy() {
        return policy;
    }

    public void setPolicy(int policy) {
        this.policy = policy;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public EventsInfo(String eventType, int policy, byte[] data) {
        this.eventType = eventType;
        this.policy = policy;
        this.data = data;
    }

    @Override
    public String toString() {
        return "EventsInfo{" +
                "eventType='" + eventType + '\'' +
                ", policy=" + policy +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
