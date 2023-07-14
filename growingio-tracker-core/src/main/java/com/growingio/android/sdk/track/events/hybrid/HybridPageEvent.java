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

package com.growingio.android.sdk.track.events.hybrid;

import com.growingio.android.sdk.track.events.PageEvent;
import com.growingio.sdk.annotation.json.JsonSerializer;

@JsonSerializer
public class HybridPageEvent extends PageEvent {
    private static final long serialVersionUID = 1L;

    private final String protocolType;
    private final String query;

    protected HybridPageEvent(Builder eventBuilder) {
        super(eventBuilder);
        protocolType = eventBuilder.protocolType;
        query = eventBuilder.query;
    }

    public String getQuery() {
        return checkValueSafe(query);
    }

    public String getProtocolType() {
        return checkValueSafe(protocolType);
    }

    public static class Builder extends PageEvent.Builder {
        private String protocolType;
        private String query;

        public Builder() {
            super();
        }

        public Builder setProtocolType(String protocolType) {
            this.protocolType = protocolType;
            return this;
        }

        public Builder setQuery(String query) {
            this.query = query;
            return this;
        }

        @Override
        public HybridPageEvent build() {
            return new HybridPageEvent(this);
        }

    }
}
