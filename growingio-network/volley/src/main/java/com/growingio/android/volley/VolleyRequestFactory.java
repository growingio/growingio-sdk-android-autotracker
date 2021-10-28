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

package com.growingio.android.volley;

import com.android.volley.Request;
import com.android.volley.Response;
import com.growingio.android.sdk.track.http.EventResponse;

import java.util.Map;

/** Used to construct a custom Volley request, such as for authentication header decoration. */
public interface VolleyRequestFactory {

  /**
   * Returns a Volley request for the given image url. The given future should be put as a listener
   * or called when the request completes.
   */
  Request<byte[]> create(
      String url,
      Map<String, String> headers,
      byte[] requestData,
      Response.Listener<EventResponse> callback,
      Response.ErrorListener listener);
}
