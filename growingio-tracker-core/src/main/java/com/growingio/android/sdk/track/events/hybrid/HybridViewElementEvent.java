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
package com.growingio.android.sdk.track.events.hybrid;

import com.growingio.android.sdk.track.events.ViewElementEvent;
import com.growingio.sdk.annotation.json.JsonSerializer;


@JsonSerializer
public final class HybridViewElementEvent extends ViewElementEvent {
    private static final long serialVersionUID = 1L;

    private final String query;
    private final String hyperlink;

    private HybridViewElementEvent(Builder eventBuilder) {
        super(eventBuilder);
        query = eventBuilder.query;
        hyperlink = eventBuilder.hyperlink;
    }

    public String getQuery() {
        return checkValueSafe(query);
    }

    public String getHyperlink() {
        return checkValueSafe(hyperlink);
    }


    public final static class Builder extends ViewElementEvent.Builder {
        private String query;
        private String hyperlink;

        public Builder(String eventType) {
            super(eventType);
        }

        @Override
        public HybridViewElementEvent build() {
            return new HybridViewElementEvent(this);
        }

        public Builder setQuery(String query) {
            this.query = query;
            return this;
        }

        public Builder setHyperlink(String hyperlink) {
            this.hyperlink = hyperlink;
            return this;
        }
    }
}
