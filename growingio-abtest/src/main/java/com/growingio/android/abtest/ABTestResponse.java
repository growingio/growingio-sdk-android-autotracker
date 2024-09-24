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

    private static final String LAYER_ID = "layerId";
    private static final String LAYER_NAME = "layerName";
    private static final String STRATEGY_ID = "strategyId";
    private static final String STRATEGY_NAME = "strategyName";
    private static final String EXPERIMENT_ID = "experimentId";
    private static final String EXPERIMENT_NAME = "experimentName";
    private static final String VARIABLES = "variables";

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
                long experimentId = jsonObject.optLong(EXPERIMENT_ID);
                long strategyId = jsonObject.optLong(STRATEGY_ID);
                JSONObject variables = jsonObject.optJSONObject(VARIABLES);
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
                String expLayerName = jsonObject.optString(LAYER_NAME);
                String expName = jsonObject.optString(EXPERIMENT_NAME);
                String expStrategyName = jsonObject.optString(STRATEGY_NAME);
                response.abExperiment.setExperimentNames(expLayerName, expName, expStrategyName);
            } else {
                response.errorMsg = jsonObject.getString("errorMsg");
            }
        } catch (JSONException e) {
            response.code = -1;
            response.errorMsg = "parse error:Illegal ABExperiment";
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
                jsonObject.put(LAYER_ID, abExperiment.getLayerId());
                jsonObject.put(STRATEGY_ID, abExperiment.getStrategyId());
                jsonObject.put(EXPERIMENT_ID, abExperiment.getExperimentId());
                Map<String, String> variableMap = abExperiment.getVariables();
                Iterator<String> keys = variableMap.keySet().iterator();
                JSONObject variablesJson = new JSONObject();
                while (keys.hasNext()) {
                    String key = keys.next();
                    variablesJson.put(key, variableMap.get(key));
                }
                jsonObject.put(VARIABLES, variablesJson);
                jsonObject.put(LAYER_NAME, abExperiment.getExpLayerName());
                jsonObject.put(EXPERIMENT_NAME, abExperiment.getExpName());
                jsonObject.put(STRATEGY_NAME, abExperiment.getExpStrategyName());
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

            String layerId = jsonObject.getString(LAYER_ID);
            long strategyId = jsonObject.getLong(STRATEGY_ID);
            long experimentId = jsonObject.getLong(EXPERIMENT_ID);
            JSONObject variables = jsonObject.getJSONObject(VARIABLES);
            Iterator<String> keys = variables.keys();
            Map<String, String> variableMap = new HashMap<>();
            while (keys.hasNext()) {
                String key = keys.next();
                variableMap.put(key, variables.get(key).toString());
            }
            abTestResponse.abExperiment = new ABExperiment(layerId, strategyId, experimentId, variableMap);
            String expLayerName = jsonObject.optString(LAYER_NAME);
            String expName = jsonObject.optString(EXPERIMENT_NAME);
            String expStrategyName = jsonObject.optString(STRATEGY_NAME);
            abTestResponse.abExperiment.setExperimentNames(expLayerName, expName, expStrategyName);
            return abTestResponse;
        } catch (JSONException e) {
            return null;
        }
    }
}
