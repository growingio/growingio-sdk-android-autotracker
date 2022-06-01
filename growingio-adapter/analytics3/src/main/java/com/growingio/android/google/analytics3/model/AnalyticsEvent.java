package com.growingio.android.google.analytics3.model;

import com.growingio.android.sdk.track.events.TrackEventType;
import com.growingio.android.sdk.track.events.base.BaseEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * 当前仅支持 JSON模块上报，PB模块不依赖 toJSONObject方法
 */
public class AnalyticsEvent extends BaseEvent {
    private static final String USER_KEY = "userKey";
    private static final String SESSION_ID = "sessionId";
    private static final String TIMESTAMP = "timestamp";
    private static final String DATASOURCE_ID = "dataSourceId";
    private static final String USER_ID = "userId";
    private static final String GIO_ID = "gioId";

    private final BaseEvent mBaseEvent;
    private final TrackerInfo mTrackerInfo;
    private final long mTimestamp;

    public AnalyticsEvent(final BaseEvent baseEvent, final TrackerInfo trackerInfo) {
        this(baseEvent, trackerInfo, 0L);
    }

    public AnalyticsEvent(final BaseEvent baseEvent, final TrackerInfo trackerInfo, final long timestamp) {
        // 空实现
        super(new BaseBuilder<BaseEvent>() {
            @Override
            public String getEventType() {
                return null;
            }

            @Override
            public BaseEvent build() {
                return null;
            }
        });
        this.mBaseEvent = baseEvent;
        this.mTrackerInfo = trackerInfo;
        this.mTimestamp = timestamp;
    }


    @Override
    public String getEventType() {
        return mBaseEvent.getEventType();
    }

    @Override
    public int getSendPolicy() {
        return mBaseEvent.getSendPolicy();
    }

    @Override
    public JSONObject toJSONObject() {
        try {
            JSONObject jsonObject = mBaseEvent.toJSONObject();
            // 如果存在则移除 userKey 字段
            jsonObject.remove(USER_KEY);

            // 如果存在则移除 gioId 字段
            jsonObject.remove(GIO_ID);

            // 替换dataSourceId
            jsonObject.put(DATASOURCE_ID, mTrackerInfo.getDatasourceId());

            // 替换sessionId
            jsonObject.put(SESSION_ID, mTrackerInfo.getSessionId());

            // 替换userId
            jsonObject.put(USER_ID, mTrackerInfo.getUserId());

            // 如果 timestamp 不为0L，替换timestamp
            if (mTimestamp != 0L) {
                jsonObject.put(TIMESTAMP, mTimestamp);
            }

            // custom需要处理 通用参数
            if (TrackEventType.CUSTOM.equals(getEventType())) {
                JSONObject attributes = jsonObject.optJSONObject("attributes");
                if (attributes == null) {
                    attributes = new JSONObject();
                }

                for (Map.Entry<String, String> attr : mTrackerInfo.getParams().entrySet()) {
                    // 如果与send设置的字段冲突，优先使用send方法中设置的字段值
                    if (!attributes.has(attr.getKey())) {
                        attributes.put(attr.getKey(), attr.getValue());
                    }
                }
            }

            return jsonObject;
        } catch (JSONException ignored) {
        }

        return new JSONObject();
    }
}
