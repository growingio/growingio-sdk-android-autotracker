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

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.utils.ReflectUtil;
import com.uc.webview.export.internal.android.WebViewAndroid;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

abstract class SuperWebView<T extends View> {
    private final T mRealWebView;

    protected SuperWebView(T realWebView) {
        mRealWebView = realWebView;
    }

    public T getRealWebView() {
        return mRealWebView;
    }

    public void getLocationOnScreen(int[] outLocation) {
        getRealWebView().getLocationOnScreen(outLocation);
    }

    public int getHeight() {
        return getRealWebView().getHeight();
    }

    public int getWidth() {
        return getRealWebView().getWidth();
    }

    public abstract void setJavaScriptEnabled(boolean flag);

    public abstract void addJavascriptInterface(Object obj, String interfaceName);

    public abstract void evaluateJavascript(String script, ValueCallback<String> resultCallback);

    public abstract void wrapperWebChromeClient(WebViewJavascriptBridgeConfiguration bridgeConfiguration);

    public boolean hasAddJavaScripted() {
        return mRealWebView.getTag(R.id.growing_tracker_has_add_java_script) != null;
    }

    public void setAddJavaScript() {
        mRealWebView.setTag(R.id.growing_tracker_has_add_java_script, new Object());
    }

    public static SuperWebView<WebView> make(WebView webView) {
        return new SystemWebView(webView);
    }

    public static SuperWebView<com.tencent.smtt.sdk.WebView> makeX5(com.tencent.smtt.sdk.WebView webView) {
        return new X5WebView(webView);
    }

    public static SuperWebView<com.uc.webview.export.WebView> makeUC(com.uc.webview.export.WebView webView) {
        return new UCWebView(webView);
    }

    protected WebChromeClient getWebChromeClientReflect(WebView webView) {
        try {
            Object provider = ReflectUtil.getFieldRecursive("mProvider", webView);
            if (provider == null) return null;
            Method providerChromeClientMethod = ReflectUtil.getMethod(provider.getClass(), "getWebChromeClient");
            if (providerChromeClientMethod != null) {
                return (WebChromeClient) providerChromeClientMethod.invoke(provider);
            }
        } catch (NoSuchFieldException ignored) {
        } catch (NoSuchMethodException ignored) {
        } catch (InvocationTargetException ignored) {
        } catch (IllegalAccessException ignored) {
        } catch (ClassCastException ignored) {
        }

        return null;
    }

    protected void setWebChromeClientAvoidThreadCheck(WebView webView, WebChromeClient client) {
        try {
            Object provider = ReflectUtil.getFieldRecursive("mProvider", webView);
            if (provider != null) {
                Method method = ReflectUtil.getMethod(provider.getClass(), "setWebChromeClient", WebChromeClient.class);
                if (method != null) {
                    method.invoke(provider, client);
                    return;
                }
            }
        } catch (NoSuchFieldException ignored) {
        } catch (NoSuchMethodException ignored) {
        } catch (InvocationTargetException ignored) {
        } catch (IllegalAccessException ignored) {
        } catch (ClassCastException ignored) {
        }

        try {
            webView.setWebChromeClient(client);
        } catch (Exception e) {
            Logger.e("SystemWebView", "setDelegateWebChromeClient failed: " + e);
        }
    }

    private static class SystemWebView extends SuperWebView<WebView> {

        protected SystemWebView(WebView realWebView) {
            super(realWebView);
        }

        @Override
        public void setJavaScriptEnabled(boolean flag) {
            getRealWebView().getSettings().setJavaScriptEnabled(flag);
        }

        @SuppressLint("JavascriptInterface")
        @Override
        public void addJavascriptInterface(Object obj, String interfaceName) {
            getRealWebView().addJavascriptInterface(obj, interfaceName);
        }


        @Override
        public void evaluateJavascript(String script, ValueCallback<String> resultCallback) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getRealWebView().evaluateJavascript(script, resultCallback);
            }
        }


        @SuppressLint("WebViewApiAvailability")
        @Override
        public void wrapperWebChromeClient(WebViewJavascriptBridgeConfiguration bridgeConfiguration) {
            WebView webView = getRealWebView();
            if (webView == null) return;

            WebChromeClient existChromeClient;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                existChromeClient = webView.getWebChromeClient();
            } else {
                existChromeClient = getWebChromeClientReflect(webView);
            }
            if (existChromeClient instanceof WebChromeClientDelegate) {
                Logger.d("SystemWebView", "setWebChromeClient: existChromeClient is WebChromeClientDelegate");
                return;
            }

            WebChromeClientDelegate delegateClient = new WebChromeClientDelegate(
                    existChromeClient == null ? new WebChromeClient() : existChromeClient,
                    bridgeConfiguration);
            setWebChromeClientAvoidThreadCheck(webView, delegateClient);
        }
    }

    private static final class X5WebView extends SuperWebView<com.tencent.smtt.sdk.WebView> {

        protected X5WebView(com.tencent.smtt.sdk.WebView realWebView) {
            super(realWebView);
        }

        @Override
        public void setJavaScriptEnabled(boolean flag) {
            getRealWebView().getSettings().setJavaScriptEnabled(flag);
        }

        @Override
        public void addJavascriptInterface(Object obj, String interfaceName) {
            getRealWebView().addJavascriptInterface(obj, interfaceName);
        }

        @Override
        public void evaluateJavascript(String script, final ValueCallback<String> resultCallback) {
            getRealWebView().evaluateJavascript(script, new com.tencent.smtt.sdk.ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                    if (resultCallback != null) {
                        resultCallback.onReceiveValue(s);
                    }
                }
            });
        }

        @Override
        public void wrapperWebChromeClient(WebViewJavascriptBridgeConfiguration bridgeConfiguration) {
            com.tencent.smtt.sdk.WebView webView = getRealWebView();
            com.tencent.smtt.sdk.WebChromeClient existChromeClient = webView.getWebChromeClient();
            if (existChromeClient instanceof WebChromeClientX5Delegate) {
                Logger.d("X5WebView", "setWebChromeClient: existChromeClient is WebChromeClientX5Delegate");
                return;
            }
            WebChromeClientX5Delegate delegateClient = new WebChromeClientX5Delegate(
                    existChromeClient == null ? new com.tencent.smtt.sdk.WebChromeClient() : existChromeClient,
                    bridgeConfiguration);
            webView.setWebChromeClient(delegateClient);
        }
    }

    private static final class UCWebView extends SuperWebView<com.uc.webview.export.WebView> {

        protected UCWebView(com.uc.webview.export.WebView realWebView) {
            super(realWebView);
        }

        @Override
        public void setJavaScriptEnabled(boolean flag) {
            getRealWebView().getSettings().setJavaScriptEnabled(flag);
        }

        @Override
        public void addJavascriptInterface(Object obj, String interfaceName) {
            getRealWebView().addJavascriptInterface(obj, interfaceName);
        }

        @Override
        public void evaluateJavascript(String script, final ValueCallback<String> resultCallback) {
            getRealWebView().evaluateJavascript(script, resultCallback);
        }

        @SuppressLint("WebViewApiAvailability")
        @Override
        public void wrapperWebChromeClient(WebViewJavascriptBridgeConfiguration bridgeConfiguration) {
            com.uc.webview.export.WebView ucWebView = getRealWebView();
            if (ucWebView == null || ucWebView.getCoreView() == null || !(ucWebView.getCoreView() instanceof WebViewAndroid)) {
                return;
            }
            WebView webView = (WebViewAndroid) (ucWebView.getCoreView());

            WebChromeClient existChromeClient;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                existChromeClient = webView.getWebChromeClient();
            } else {
                existChromeClient = getWebChromeClientReflect(webView);
            }
            if (existChromeClient instanceof WebChromeClientDelegate) {
                Logger.d("SystemWebView", "setWebChromeClient: existChromeClient is WebChromeClientDelegate");
                return;
            }

            WebChromeClientDelegate delegateClient = new WebChromeClientDelegate(
                    existChromeClient == null ? new WebChromeClient() : existChromeClient,
                    bridgeConfiguration);
            setWebChromeClientAvoidThreadCheck(webView, delegateClient);
        }
    }
}
