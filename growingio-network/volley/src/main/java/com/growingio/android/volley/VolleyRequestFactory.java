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
      Response.Listener<EventResponse> callback,
      Response.ErrorListener listener,
      Map<String, String> headers);
}
