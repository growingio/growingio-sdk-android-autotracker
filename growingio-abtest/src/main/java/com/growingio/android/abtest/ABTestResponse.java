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
package com.growingio.android.abtest;

import com.growingio.android.sdk.track.middleware.abtest.ABExperiment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class ABTestResponse {
    private int code = -1;
    private String errorMsg;

    ABExperiment abExperiment;
    long expiredTime;
    long naturalDaytime;

    public int getCode() {
        return code;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public static ABTestResponse parseHttpJson(long expiredTime, String layerId, String json) {
        final ABTestResponse response = new ABTestResponse();
        try {
            JSONObject jsonObject = new JSONObject(json);
            int code = jsonObject.getInt("code");
            response.code = code;
            if (code == 0) {
                long experimentId = jsonObject.getLong("experimentId");
                long strategyId = jsonObject.optLong("strategyId", -1L);
                JSONObject variables = jsonObject.optJSONObject("variables");
                Map<String, String> variableMap = new HashMap<>();
                if (variables != null) {
                    Iterator<String> keys = variables.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        variableMap.put(key, variables.get(key).toString());
                    }
                }
                response.expiredTime = System.currentTimeMillis() + expiredTime;
                response.naturalDaytime = tomorrowMill();
                response.abExperiment = new ABExperiment(layerId, strategyId, experimentId, variableMap);
            } else {
                response.errorMsg = jsonObject.getString("errorMsg");
            }
        } catch (JSONException e) {
            response.code = -1;
            response.errorMsg = "parse error:Empty ABExperiment ID";
        }
        return response;
    }

    public ABExperiment getABExperiment() {
        return abExperiment;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    boolean isSucceed() {
        return code == 0;
    }

    static long tomorrowMill() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, cal.get(Calendar.DATE) + 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public String toSavedJson() {
        if (abExperiment == null) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("code", code);
            jsonObject.put("expiredTime", expiredTime);
            jsonObject.put("naturalDaytime", naturalDaytime);
            if (abExperiment != null) {
                jsonObject.put("layerId", abExperiment.getLayerId());
                jsonObject.put("strategyId", abExperiment.getStrategyId());
                jsonObject.put("experimentId", abExperiment.getExperimentId());
                Map<String, String> variableMap = abExperiment.getVariables();
                Iterator<String> keys = variableMap.keySet().iterator();
                JSONObject variablesJson = new JSONObject();
                while (keys.hasNext()) {
                    String key = keys.next();
                    variablesJson.put(key, variableMap.get(key));
                }
                jsonObject.put("variables", variablesJson);
            }

            return jsonObject.toString();
        } catch (JSONException ignored) {
            return null;
        }
    }

    public static ABTestResponse parseSavedJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            long naturalDaytime = jsonObject.getLong("naturalDaytime");
            long expiredTime = jsonObject.getLong("expiredTime");

            ABTestResponse abTestResponse = new ABTestResponse();
            abTestResponse.expiredTime = expiredTime;
            abTestResponse.naturalDaytime = naturalDaytime;

            String layerId = jsonObject.getString("layerId");
            long strategyId = jsonObject.getLong("strategyId");
            long experimentId = jsonObject.getLong("experimentId");
            JSONObject variables = jsonObject.getJSONObject("variables");
            Iterator<String> keys = variables.keys();
            Map<String, String> variableMap = new HashMap<>();
            while (keys.hasNext()) {
                String key = keys.next();
                variableMap.put(key, variables.get(key).toString());
            }
            abTestResponse.abExperiment = new ABExperiment(layerId, strategyId, experimentId, variableMap);
            return abTestResponse;
        } catch (JSONException e) {
            return null;
        }
    }
}
