/*
 * Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
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

package com.growingio.android.sdk.track.events;

import com.growingio.sdk.annotation.json.JsonSerializer;

@JsonSerializer
public class PageLevelCustomEvent extends CustomEvent {
    private static final long serialVersionUID = 1L;

    private final String path;
    private final long pageShowTimestamp;

    protected PageLevelCustomEvent(Builder eventBuilder) {
        super(eventBuilder);
        path = eventBuilder.path;
        pageShowTimestamp = eventBuilder.pageShowTimestamp;
    }

    public String getPath() {
        return checkValueSafe(path);
    }

    public long getPageShowTimestamp() {
        return pageShowTimestamp;
    }

    public static class Builder extends CustomEvent.Builder {
        private String path;
        private long pageShowTimestamp;

        public Builder() {
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setPageShowTimestamp(long pageShowTimestamp) {
            this.pageShowTimestamp = pageShowTimestamp;
            return this;
        }

        public String getPath() {
            return path;
        }

        @Override
        public PageLevelCustomEvent build() {
            return new PageLevelCustomEvent(this);
        }
    }
}
