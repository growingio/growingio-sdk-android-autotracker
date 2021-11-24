/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.database;

import com.growingio.android.sdk.track.events.AutotrackEventType;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.events.PageAttributesEvent;
import com.growingio.android.sdk.track.events.PageEvent;
import com.growingio.android.sdk.track.events.PageLevelCustomEvent;
import com.growingio.android.sdk.track.events.TrackEventType;
import com.growingio.android.sdk.track.events.ViewElementEvent;
import com.growingio.android.sdk.track.events.VisitEvent;
import com.growingio.android.sdk.track.events.base.BaseAttributesEvent;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.events.cdp.ResourceItem;
import com.growingio.android.sdk.track.events.cdp.ResourceItemCustomEvent;
import com.growingio.android.sdk.track.events.hybrid.HybridCustomEvent;
import com.growingio.android.sdk.track.events.hybrid.HybridPageAttributesEvent;
import com.growingio.android.sdk.track.events.hybrid.HybridPageEvent;
import com.growingio.android.sdk.track.events.hybrid.HybridViewElementEvent;
import com.growingio.android.sdk.track.middleware.GEvent;

import java.util.Map;

/**
 * <p>
 * transfer GEvent to protocol
 *
 * @author cpacm 2021/11/23
 */
public class EventProtocolTransfer {
    private static EventV3Protocol.EventType protocolType(String eventType) {
        if (TrackEventType.VISIT.equals(eventType)) {
            return EventV3Protocol.EventType.VISIT;//0
        } else if (TrackEventType.CUSTOM.equals(eventType)) {
            return EventV3Protocol.EventType.CUSTOM;//1
        } else if (TrackEventType.VISITOR_ATTRIBUTES.equals(eventType)) {
            return EventV3Protocol.EventType.VISITOR_ATTRIBUTES;//2
        } else if (TrackEventType.LOGIN_USER_ATTRIBUTES.equals(eventType)) {
            return EventV3Protocol.EventType.LOGIN_USER_ATTRIBUTES;//3
        } else if (TrackEventType.CONVERSION_VARIABLES.equals(eventType)) {
            return EventV3Protocol.EventType.CONVERSION_VARIABLES;//4
        } else if (TrackEventType.APP_CLOSED.equals(eventType)) {
            return EventV3Protocol.EventType.APP_CLOSED;//5
        } else if (AutotrackEventType.PAGE.equals(eventType)) {
            return EventV3Protocol.EventType.PAGE;//6
        } else if (AutotrackEventType.PAGE_ATTRIBUTES.equals(eventType)) {
            return EventV3Protocol.EventType.PAGE_ATTRIBUTES;//7
        } else if (AutotrackEventType.VIEW_CLICK.equals(eventType)) {
            return EventV3Protocol.EventType.VIEW_CLICK;//8
        } else if (AutotrackEventType.VIEW_CHANGE.equals(eventType)) {
            return EventV3Protocol.EventType.VIEW_CHANGE;//9
        } else if (TrackEventType.FORM_SUBMIT.equals(eventType)) {
            return EventV3Protocol.EventType.FORM_SUBMIT;//10
        }
        return EventV3Protocol.EventType.CUSTOM;//1
    }

    public static byte[] protocol(GEvent gEvent) {
        EventV3Protocol.EventV3Dto.Builder eventBuilder = EventV3Protocol.EventV3Dto.newBuilder();
        if (gEvent instanceof BaseEvent) {
            BaseEvent baseEvent = (BaseEvent) gEvent;
            eventBuilder.setDeviceId(baseEvent.getDeviceId()); // 1
            eventBuilder.setUserId(baseEvent.getUserId()); // 2
            Map<String, String> extraParams = baseEvent.getExtraParams();
            if (extraParams != null && extraParams.containsKey("gioId")) {
                eventBuilder.setGioId(extraParams.get("gioId"));//3
            }
            eventBuilder.setSessionId(baseEvent.getSessionId());//4
            if (extraParams != null && extraParams.containsKey("dataSourceId")) {
                eventBuilder.setDataSourceId(extraParams.get("dataSourceId"));//5
            }
            eventBuilder.setEventType(protocolType(baseEvent.getEventType()));//6
            eventBuilder.setPlatform(baseEvent.getPlatform());//7
            eventBuilder.setTimestamp(baseEvent.getTimestamp());//8
            eventBuilder.setDomain(baseEvent.getDomain());//9

            eventBuilder.setGlobalSequenceId(baseEvent.getGlobalSequenceId());//14
            eventBuilder.setEventSequenceId((int) baseEvent.getEventSequenceId());//15
            eventBuilder.setScreenHeight(baseEvent.getScreenHeight());//16
            eventBuilder.setScreenWidth(baseEvent.getScreenWidth());//17
            eventBuilder.setLanguage(baseEvent.getLanguage());//18
            eventBuilder.setSdkVersion(baseEvent.getSdkVersion());//19
            eventBuilder.setAppVersion(baseEvent.getAppVersion());//20

            eventBuilder.setUrlScheme(baseEvent.getUrlScheme());//31
            eventBuilder.setAppState(baseEvent.getAppState());//32
            eventBuilder.setNetworkState(baseEvent.getNetworkState());//33
            eventBuilder.setAppChannel(baseEvent.getAppChannel());//34
            eventBuilder.setPlatformVersion(baseEvent.getPlatformVersion());//36
            eventBuilder.setDeviceBrand(baseEvent.getDeviceBrand());//37
            eventBuilder.setDeviceModel(baseEvent.getDeviceModel());//38
            eventBuilder.setDeviceType(baseEvent.getDeviceType());//39
            eventBuilder.setOperatingSystem(baseEvent.getPlatformVersion());//40
            eventBuilder.setAppName(baseEvent.getAppName());//42
            eventBuilder.setLatitude(baseEvent.getLatitude());//44
            eventBuilder.setLongitude(baseEvent.getLongitude());//45

            eventBuilder.setUserKey(baseEvent.getUserKey());//55
        }
        if (gEvent instanceof PageEvent) {
            PageEvent pageEvent = (PageEvent) gEvent;
            eventBuilder.setPath(pageEvent.getPath());//10
            eventBuilder.setTitle(pageEvent.getTitle());//12
            eventBuilder.setReferralPage(pageEvent.getReferralPage());//13
            eventBuilder.setOrientation(pageEvent.getOrientation());//52
        }
        if (gEvent instanceof BaseAttributesEvent) {
            BaseAttributesEvent attrEvent = (BaseAttributesEvent) gEvent;
            if (attrEvent.getAttributes() != null) {
                eventBuilder.putAllAttributes(attrEvent.getAttributes());//24
            }
        }
        if (gEvent instanceof CustomEvent) {
            CustomEvent customEvent = (CustomEvent) gEvent;
            eventBuilder.setEventName(customEvent.getEventName());//22
        }
        if (gEvent instanceof PageAttributesEvent) {
            PageAttributesEvent paEvent = (PageAttributesEvent) gEvent;
            eventBuilder.setPath(paEvent.getPath());//10
            eventBuilder.setPageShowTimestamp(paEvent.getPageShowTimestamp());//23
        }
        if (gEvent instanceof PageLevelCustomEvent) {
            PageLevelCustomEvent plEvent = (PageLevelCustomEvent) gEvent;
            eventBuilder.setPath(plEvent.getPath());//10
            eventBuilder.setPageShowTimestamp(plEvent.getPageShowTimestamp());//23
        }
        if (gEvent instanceof ViewElementEvent) {
            ViewElementEvent vEvent = (ViewElementEvent) gEvent;
            eventBuilder.setPath(vEvent.getPath());//10
            eventBuilder.setPageShowTimestamp(vEvent.getPageShowTimestamp());//23
            eventBuilder.setTextValue(vEvent.getTextValue());//27
            eventBuilder.setXpath(vEvent.getXpath());//28
            eventBuilder.setIndex(vEvent.getIndex());//29
        }
        if (gEvent instanceof VisitEvent) {
            VisitEvent visitEvent = (VisitEvent) gEvent;
            if (visitEvent.getExtraSdk() != null) {
                eventBuilder.putAllExtraSdk(visitEvent.getExtraSdk());//21
            }
            eventBuilder.setImei(visitEvent.getImei());//46
            eventBuilder.setAndroidId(visitEvent.getAndroidId());//47
            eventBuilder.setOaid(visitEvent.getOaid());//48
            eventBuilder.setGoogleAdvertisingId(visitEvent.getGoogleAdvertisingId());//49
        }

        // deal with hybrid event
        if (gEvent instanceof HybridCustomEvent) {
            HybridCustomEvent hcEvent = (HybridCustomEvent) gEvent;
            eventBuilder.setQuery(hcEvent.getQuery());//11
        }
        if (gEvent instanceof HybridPageAttributesEvent) {
            HybridPageAttributesEvent hpaEvent = (HybridPageAttributesEvent) gEvent;
            eventBuilder.setQuery(hpaEvent.getQuery());//11
        }
        if (gEvent instanceof HybridPageEvent) {
            HybridPageEvent hpEvent = (HybridPageEvent) gEvent;
            eventBuilder.setQuery(hpEvent.getQuery());//11
            eventBuilder.setProtocolType(hpEvent.getProtocolType());//26
        }
        if (gEvent instanceof HybridViewElementEvent) {
            HybridViewElementEvent hvEvent = (HybridViewElementEvent) gEvent;
            eventBuilder.setHyperlink(hvEvent.getHyperlink());//30
            eventBuilder.setQuery(hvEvent.getQuery());//11
        }

        // deal with cdp event
        if (gEvent instanceof ResourceItemCustomEvent) {
            ResourceItemCustomEvent ricEvent = (ResourceItemCustomEvent) gEvent;
            ResourceItem resourceItem = ricEvent.getResourceItem();
            if (resourceItem != null) {
                eventBuilder.setResourceItem(EventV3Protocol.ResourceItem.newBuilder()
                        .setId(resourceItem.getId()).setKey(resourceItem.getKey()));//25
            }
        }
        return eventBuilder.build().toByteArray();

    }
}


