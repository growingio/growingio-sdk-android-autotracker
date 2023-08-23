/*
 * Copyright (C) 2023 Beijing Yishu Technology Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.growingio.android.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import com.growingio.android.sdk.track.events.ActivateEvent;
import com.growingio.android.sdk.track.events.AppClosedEvent;
import com.growingio.android.sdk.track.events.AutotrackEventType;
import com.growingio.android.sdk.track.events.ConversionVariablesEvent;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.events.LoginUserAttributesEvent;
import com.growingio.android.sdk.track.events.PageEvent;
import com.growingio.android.sdk.track.events.PageLevelCustomEvent;
import com.growingio.android.sdk.track.events.TrackEventType;
import com.growingio.android.sdk.track.events.ViewElementEvent;
import com.growingio.android.sdk.track.events.VisitEvent;
import com.growingio.android.sdk.track.events.VisitorAttributesEvent;
import com.growingio.android.sdk.track.events.base.BaseAttributesEvent;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.events.hybrid.HybridCustomEvent;
import com.growingio.android.sdk.track.events.hybrid.HybridPageEvent;
import com.growingio.android.sdk.track.events.hybrid.HybridViewElementEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.providers.EventBuilderProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * <p>
 * transfer GEvent to protocol
 *
 * @author cpacm 2021/11/23
 */
class EventProtocolTransfer {

    private EventProtocolTransfer() {
    }

    public static EventV3Protocol.EventV3Dto covertToProtobuf(byte[] byteArray) {
        try {
            return EventV3Protocol.EventV3Dto.parseFrom(byteArray);
        } catch (InvalidProtocolBufferException e) {
            Logger.w("EventProtocolTransfer", "Events in the database are not in the protobuf format");
        }
        try {
            String data = new String(byteArray);
            if (data.startsWith("{") && data.endsWith("}")) { //ensure json format
                JSONObject json = new JSONObject(data);
                BaseEvent.BaseBuilder builder = generateEventBuilder(json);
                EventBuilderProvider.parseFrom(builder, json);
                if (builder != null) {
                    return protocol(builder.build());
                }
                return null;
            }
        } catch (JSONException e) {
            Logger.w("EventProtocolTransfer", "Events in the database are not in the json format");
        }
        return null;
    }

    private static EventV3Protocol.EventType protocolType(String eventType) {
        if (TrackEventType.VISIT.equals(eventType)) {
            return EventV3Protocol.EventType.VISIT; //0
        } else if (TrackEventType.CUSTOM.equals(eventType)) {
            return EventV3Protocol.EventType.CUSTOM; //1
        } else if (TrackEventType.VISITOR_ATTRIBUTES.equals(eventType)) {
            return EventV3Protocol.EventType.VISITOR_ATTRIBUTES; //2
        } else if (TrackEventType.LOGIN_USER_ATTRIBUTES.equals(eventType)) {
            return EventV3Protocol.EventType.LOGIN_USER_ATTRIBUTES; //3
        } else if (TrackEventType.CONVERSION_VARIABLES.equals(eventType)) {
            return EventV3Protocol.EventType.CONVERSION_VARIABLES; //4
        } else if (TrackEventType.APP_CLOSED.equals(eventType)) {
            return EventV3Protocol.EventType.APP_CLOSED; //5
        } else if (AutotrackEventType.PAGE.equals(eventType)) {
            return EventV3Protocol.EventType.PAGE; //6
        } else if (AutotrackEventType.PAGE_ATTRIBUTES.equals(eventType)) {
            return EventV3Protocol.EventType.PAGE_ATTRIBUTES; //7
        } else if (AutotrackEventType.VIEW_CLICK.equals(eventType)) {
            return EventV3Protocol.EventType.VIEW_CLICK; //8
        } else if (AutotrackEventType.VIEW_CHANGE.equals(eventType)) {
            return EventV3Protocol.EventType.VIEW_CHANGE; //9
        } else if (TrackEventType.FORM_SUBMIT.equals(eventType)) {
            return EventV3Protocol.EventType.FORM_SUBMIT; //10
        } else if (TrackEventType.ACTIVATE.equals(eventType)) {
            return EventV3Protocol.EventType.ACTIVATE; //11
        }
        return EventV3Protocol.EventType.CUSTOM; //1
    }

    private static BaseEvent.BaseBuilder generateEventBuilder(JSONObject event) {
        try {
            String eventType = event.getString(BaseEvent.EVENT_TYPE);
            if (TrackEventType.VISIT.equals(eventType)) {
                return new VisitEvent.Builder();
            } else if (AutotrackEventType.VIEW_CLICK.equals(eventType)
                    || AutotrackEventType.VIEW_CHANGE.equals(eventType)
                    || TrackEventType.FORM_SUBMIT.equals(eventType)) {
                if (event.has("query")) {
                    return new HybridViewElementEvent.Builder(eventType);
                }
                return new ViewElementEvent.Builder(eventType);
            } else if (AutotrackEventType.PAGE.equals(eventType)) {
                if (event.has("query")) {
                    return new HybridPageEvent.Builder();
                }
                return new PageEvent.Builder();
            } else if (TrackEventType.CUSTOM.equals(eventType)) {
                if (event.has("query")) {
                    return new HybridCustomEvent.Builder();
                }
                if (event.has("path")) {
                    return new PageLevelCustomEvent.Builder();
                }
                return new CustomEvent.Builder(); //custom
            } else if (TrackEventType.ACTIVATE.equals(eventType)) {
                return new ActivateEvent.Builder();
            } else if (TrackEventType.VISITOR_ATTRIBUTES.equals(eventType)) {
                return new VisitorAttributesEvent.Builder();
            } else if (TrackEventType.LOGIN_USER_ATTRIBUTES.equals(eventType)) {
                return new LoginUserAttributesEvent.Builder();
            } else if (TrackEventType.CONVERSION_VARIABLES.equals(eventType)) {
                return new ConversionVariablesEvent.Builder();
            } else if (TrackEventType.APP_CLOSED.equals(eventType)) {
                return new AppClosedEvent.Builder();
            } else {
                return new BaseAttributesEvent.Builder(eventType) {
                    @Override
                    public BaseEvent build() {
                        return new BaseAttributesEvent(this) {
                            @Override
                            public Map<String, String> getAttributes() {
                                return super.getAttributes();
                            }
                        };
                    }
                };
            }
        } catch (JSONException ignored) {
        }
        return null;
    }


    public static byte[] protocolByte(GEvent gEvent) {
        EventV3Protocol.EventV3Dto eventBuilder = protocol(gEvent);
        return eventBuilder.toByteArray();

    }

    private static EventV3Protocol.EventV3Dto protocol(GEvent gEvent) {
        EventV3Protocol.EventV3Dto.Builder eventBuilder = EventV3Protocol.EventV3Dto.newBuilder();
        if (gEvent instanceof BaseEvent) {
            BaseEvent baseEvent = (BaseEvent) gEvent;
            eventBuilder.setDeviceId(baseEvent.getDeviceId()); //1
            eventBuilder.setUserId(baseEvent.getUserId()); //2
//            if (extraParams != null && extraParams.containsKey("gioId")) {
//                eventBuilder.setGioId(extraParams.get("gioId")); //3
//            }
            eventBuilder.setSessionId(baseEvent.getSessionId()); //4
            eventBuilder.setDataSourceId(baseEvent.getDataSourceId()); //5
            eventBuilder.setEventType(protocolType(baseEvent.getEventType())); //6
            eventBuilder.setPlatform(baseEvent.getPlatform()); //7
            eventBuilder.setTimestamp(baseEvent.getTimestamp()); //8
            eventBuilder.setDomain(baseEvent.getDomain()); //9

//            eventBuilder.setGlobalSequenceId(baseEvent.getGlobalSequenceId()); //14
            eventBuilder.setEventSequenceId((int) baseEvent.getEventSequenceId()); //15
            eventBuilder.setScreenHeight(baseEvent.getScreenHeight()); //16
            eventBuilder.setScreenWidth(baseEvent.getScreenWidth()); //17
            eventBuilder.setLanguage(baseEvent.getLanguage()); //18
            eventBuilder.setSdkVersion(baseEvent.getSdkVersion()); //19
            eventBuilder.setAppVersion(baseEvent.getAppVersion()); //20

            eventBuilder.setUrlScheme(baseEvent.getUrlScheme()); //31
            eventBuilder.setAppState(baseEvent.getAppState()); //32
            eventBuilder.setNetworkState(baseEvent.getNetworkState()); //33
            eventBuilder.setAppChannel(baseEvent.getAppChannel()); //34
            eventBuilder.setPlatformVersion(baseEvent.getPlatformVersion()); //36
            eventBuilder.setDeviceBrand(baseEvent.getDeviceBrand()); //37
            eventBuilder.setDeviceModel(baseEvent.getDeviceModel()); //38
            eventBuilder.setDeviceType(baseEvent.getDeviceType()); //39
            eventBuilder.setOperatingSystem(baseEvent.getPlatformVersion()); //40
            eventBuilder.setAppName(baseEvent.getAppName()); //42
            eventBuilder.setLatitude(baseEvent.getLatitude()); //44
            eventBuilder.setLongitude(baseEvent.getLongitude()); //45

            eventBuilder.setUserKey(baseEvent.getUserKey()); //55
            eventBuilder.setTimezoneOffset(baseEvent.getTimezoneOffset()); //57
        }

        if (gEvent instanceof BaseAttributesEvent) {
            BaseAttributesEvent attrEvent = (BaseAttributesEvent) gEvent;
            if (attrEvent.getAttributes() != null) {
                eventBuilder.putAllAttributes(attrEvent.getAttributes()); //24
            }
        }
        if (gEvent instanceof ViewElementEvent) {
            ViewElementEvent vEvent = (ViewElementEvent) gEvent;
            eventBuilder.setPath(vEvent.getPath()); //10
            eventBuilder.setTextValue(vEvent.getTextValue()); //27
            eventBuilder.setXpath(vEvent.getXpath()); //28
            eventBuilder.setIndex(vEvent.getIndex()); //29
            if (vEvent.getPageShowTimestamp() > 0) {
                eventBuilder.setPageShowTimestamp(vEvent.getPageShowTimestamp());//23
            }
            if (vEvent.getXIndex() != null) {
                eventBuilder.setXcontent(vEvent.getXIndex()); //56 --xIndex isEqualWith xContent
            }
        }
        if (gEvent instanceof PageEvent) {
            PageEvent pageEvent = (PageEvent) gEvent;
            eventBuilder.setPath(pageEvent.getPath()); //10
            eventBuilder.setTitle(pageEvent.getTitle()); //12
            eventBuilder.setReferralPage(pageEvent.getReferralPage()); //13
            eventBuilder.setOrientation(pageEvent.getOrientation()); //52
        }
        if (gEvent instanceof CustomEvent) {
            CustomEvent customEvent = (CustomEvent) gEvent;
            eventBuilder.setEventName(customEvent.getEventName()); //22
        }
        if (gEvent instanceof PageLevelCustomEvent) {
            PageLevelCustomEvent plEvent = (PageLevelCustomEvent) gEvent;
            eventBuilder.setPath(plEvent.getPath()); //10
            eventBuilder.setPageShowTimestamp(plEvent.getPageShowTimestamp()); //23
        }
        if (gEvent instanceof VisitEvent) {
            VisitEvent visitEvent = (VisitEvent) gEvent;
            if (visitEvent.getExtraSdk() != null) {
                eventBuilder.putAllExtraSdk(visitEvent.getExtraSdk()); //21
            }
            eventBuilder.setImei(visitEvent.getImei()); //46
            eventBuilder.setAndroidId(visitEvent.getAndroidId()); //47
            eventBuilder.setOaid(visitEvent.getOaid()); //48
            eventBuilder.setGoogleAdvertisingId(visitEvent.getGoogleAdvertisingId()); //49
        }

        // deal with hybrid event
        if (gEvent instanceof HybridCustomEvent) {
            HybridCustomEvent hcEvent = (HybridCustomEvent) gEvent;
            eventBuilder.setQuery(hcEvent.getQuery()); //11
        }
        if (gEvent instanceof HybridPageEvent) {
            HybridPageEvent hpEvent = (HybridPageEvent) gEvent;
            eventBuilder.setQuery(hpEvent.getQuery()); //11
            eventBuilder.setProtocolType(hpEvent.getProtocolType()); //26
        }
        if (gEvent instanceof HybridViewElementEvent) {
            HybridViewElementEvent hvEvent = (HybridViewElementEvent) gEvent;
            eventBuilder.setHyperlink(hvEvent.getHyperlink()); //30
            eventBuilder.setQuery(hvEvent.getQuery()); //11
        }

        // deal with advert events
        if (gEvent instanceof ActivateEvent) {
            ActivateEvent activateEvent = (ActivateEvent) gEvent;
            eventBuilder.setEventName(activateEvent.getEventName()); //22
            eventBuilder.setImei(activateEvent.getImei()); //46
            eventBuilder.setAndroidId(activateEvent.getAndroidId()); //47
            eventBuilder.setGoogleAdvertisingId(activateEvent.getGoogleId()); //49
            eventBuilder.setOaid(activateEvent.getOaid()); //48
        }

        return eventBuilder.build();
    }
}


