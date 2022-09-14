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

package com.growingio.android.sdk.track.events;

import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 用于构建事件的属性
 *
 * @author cpacm 2022/8/19
 */
public class AttributesBuilder {

    Map<String, String> attributes;
    private static final String LIST_SPLIT = "||";

    public AttributesBuilder() {
        this.attributes = new HashMap<>();
    }

    public AttributesBuilder addAttribute(String key, String value) {
        if (key != null && value != null) {
            attributes.put(key, value);
        }

        return this;
    }

    public <T> AttributesBuilder addAttribute(String key, List<T> value) {
        if (key != null && value != null && !value.isEmpty()) {
            StringBuilder valueBuilder = new StringBuilder();
            Iterator<T> iterator = value.iterator();
            if (iterator.hasNext()) {
                valueBuilder.append(toString(iterator.next()));
                while (iterator.hasNext()) {
                    valueBuilder.append(LIST_SPLIT);
                    valueBuilder.append(toString(iterator.next()));
                }
            }
            attributes.put(key, valueBuilder.toString());
        }

        return this;
    }

    /**
     * support string array only
     */
    public <T> AttributesBuilder addAttribute(String key, JSONArray array) {
        if (key != null && array != null && array.length() > 0) {
            List<String> valueList = new ArrayList<>();
            try {
                for (int i = 0; i < array.length(); i++) {
                    Object value = array.get(i);
                    if (value instanceof String) {
                        valueList.add(String.valueOf(value));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return addAttribute(key, valueList);
        }
        return this;
    }

    public <T> AttributesBuilder addAttribute(String key, SparseArray<T> array) {
        if (key != null && array != null && array.size() > 0) {
            List<String> valueList = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                T value = array.valueAt(i);
                valueList.add(String.valueOf(value));

            }
            return addAttribute(key, valueList);
        }
        return this;
    }

    public <T> AttributesBuilder addAttribute(String key, Set<T> array) {
        if (key != null && array != null && array.size() > 0) {
            StringBuilder valueBuilder = new StringBuilder();
            Iterator<T> iterator = array.iterator();
            if (iterator.hasNext()) {
                valueBuilder.append(toString(iterator.next()));
                while (iterator.hasNext()) {
                    valueBuilder.append(LIST_SPLIT);
                    valueBuilder.append(toString(iterator.next()));
                }
            }
            attributes.put(key, valueBuilder.toString());
        }
        return this;
    }

    public AttributesBuilder addAttribute(String key, String[] array) {
        if (key != null && array != null && array.length > 0) {
            StringBuilder valueBuilder = new StringBuilder();
            for (int i = 0; i < array.length; i++) {
                valueBuilder.append(array[i]);
                if (i < array.length - 1) {
                    valueBuilder.append(LIST_SPLIT);
                }
            }
            return addAttribute(key, valueBuilder.toString());
        }
        return this;
    }


    private String toString(Object value) {
        if (value == null) {
            return "";
        } else {
            return String.valueOf(value);
        }
    }

    public Map<String, String> build() {
        if (attributes == null || attributes.isEmpty()) {
            return null;
        }
        return attributes;
    }
}
