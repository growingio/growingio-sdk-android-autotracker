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
package com.growingio.android.sdk.track.events;

import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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

    public AttributesBuilder addAttribute(Map<String, Object> map) {
        if (map != null && map.keySet() != null) {
            for (String key : map.keySet()) {
                if (key == null) continue;
                Object value = map.get(key);
                if (value instanceof List<?>) {
                    List<?> tempValue = (List<?>) value;
                    addAttribute(key, tempValue);
                } else if (value instanceof SparseArray) {
                    SparseArray<?> tempValue = (SparseArray<?>) value;
                    addAttribute(key, tempValue);
                } else if (value instanceof String[]) {
                    addAttribute(key, (String[]) value);
                } else if (value instanceof Set) {
                    Set<?> tempValue = (Set<?>) value;
                    addAttribute(key, tempValue);
                } else if (value instanceof JSONArray) {
                    addAttribute(key, (JSONArray) value);
                } else if (value instanceof Date) {
                    addAttribute(key, (Date) value);
                } else {
                    addAttribute(key, String.valueOf(value));
                }
            }
        }
        return this;
    }

    public AttributesBuilder addAttribute(String key, Date date) {
        if (key != null && date != null) {
            attributes.put(key, formatDate(date));
        }
        return this;
    }

    private String formatDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        return formatter.format(date);
    }

    public AttributesBuilder addAttribute(String key, String value) {
        if (key != null && value != null) {
            attributes.put(key, value);
        }
        return this;
    }

    public AttributesBuilder addAttribute(String key, long value) {
        if (key != null) {
            attributes.put(key, String.valueOf(value));
        }
        return this;
    }

    public AttributesBuilder addAttribute(String key, int value) {
        if (key != null) {
            attributes.put(key, String.valueOf(value));
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

    public void clear() {
        attributes.clear();
    }

    public int size() {
        return attributes.size();
    }

    public AttributesBuilder removeAttribute(String key) {
        attributes.remove(key);
        return this;
    }

    public Map<String, String> build() {
        if (attributes == null || attributes.isEmpty()) {
            return null;
        }
        return attributes;
    }
}
