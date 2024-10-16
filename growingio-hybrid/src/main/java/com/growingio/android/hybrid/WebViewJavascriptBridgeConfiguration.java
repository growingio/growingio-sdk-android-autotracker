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
package com.growingio.android.hybrid;

import com.growingio.android.sdk.track.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

class WebViewJavascriptBridgeConfiguration {
    private static final String TAG = "WebViewJavascriptBridgeConfiguration";

    private final String mProjectId;

    private final String mDataSourceId;
    private final String mAppId;
    private final String mAppPackage;
    private final String mNativeSdkVersion;
    private final int mNativeSdkVersionCode;
    private final String mServerUrl;

    WebViewJavascriptBridgeConfiguration(String projectId, String dataSourceId, String serverUrl, String appId, String appPackage, String nativeSdkVersion, int nativeSdkVersionCode) {
        mProjectId = projectId;
        mDataSourceId = dataSourceId;
        mServerUrl = serverUrl;
        mAppId = appId;
        mAppPackage = appPackage;
        mNativeSdkVersion = nativeSdkVersion;
        mNativeSdkVersionCode = nativeSdkVersionCode;
    }

    JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("projectId", mProjectId);
            jsonObject.put("dataSourceId", mDataSourceId);
            jsonObject.put("appId", mAppId);
            jsonObject.put("appPackage", mAppPackage);
            jsonObject.put("nativeSdkVersion", mNativeSdkVersion);
            jsonObject.put("nativeSdkVersionCode", mNativeSdkVersionCode);
            return jsonObject;
        } catch (JSONException e) {
            Logger.e(TAG, e.getMessage(), e);
        }
        return jsonObject;
    }


    public String injectScriptFile(String id, String scriptSrc) {
        String js = "javascript:(function(){try{" +
                "var jsNode = document.getElementById('%s');\n" +
                "if (jsNode==null) {\n" +
                    "!(function (e, n, t, s, c) {\n" +
                    "      e[s] =\n" +
                    "        e[s] ||\n" +
                    "        function () {\n" +
                    "          (e[s].q = e[s].q || []).push(arguments);\n" +
                    "        };\n" +
                    "      (e._gio_local_vds = c = c || 'vds'),\n" +
                    "        (e[c] = e[c] || {}),\n" +
                    "        (e[c].namespace = s);\n" +
                    "      var d = n.createElement('script');\n" +
                    "      var i = n.getElementsByTagName('script')[0];\n" +
                    "      (d.async = !0), (d.src = t), (d.id = '%s'); \n" +
                    "      i ? i.parentNode.insertBefore(d, i) : n.head.appendChild(d);\n" +
                    "    })(window, document, '%s', 'gdp');"
                    + initJsSDK() +
                "}}catch(e){}})()";
        return String.format(js, id, id, scriptSrc);
    }

    private String initJsSDK() {
        String initScript = "var _growing_init_func = function() {\n" +
                "      window._gr_ignore_local_rule = true;\n" +
                "      gdp('init', '%s', '%s', {\n" +
                "        serverUrl: '%s',\n" +
                "        debug: true," +
                "      });\n" +
                "    };\n" +
                "\n" +
                "    if (['interactive', 'complete'].indexOf(document.readyState) >= 0) {\n" +
                "      _growing_init_func();\n" +
                "    } else {\n" +
                "      document.addEventListener('readystatechange',\n" +
                "        () => {\n" +
                "          if (['interactive', 'complete'].indexOf(document.readyState) >= 0) {\n" +
                "            _growing_init_func();\n" +
                "          }\n" +
                "        },\n" +
                "        { once: true }\n" +
                "      );\n" +
                "  }";

        return String.format(initScript, mProjectId, mDataSourceId, mServerUrl);
    }
}
