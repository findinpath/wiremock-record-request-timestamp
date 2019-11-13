package com.findinpath;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import java.util.Objects;

/**
 * Utility class used to group together the:
 * <ul>
 *   <li>request url</li>
 *   <li>request method</li>
 * </ul>
 *
 * under the same hood.
 *
 */
class RequestId {

  private String url;
  private RequestMethod method;

  public RequestId() {
  }

  public RequestId(String url, RequestMethod method) {
    this.url = url;
    this.method = method;
  }

  public String getUrl() {
    return url;
  }

  public RequestMethod getMethod() {
    return method;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RequestId requestId = (RequestId) o;
    return Objects.equals(url, requestId.url) &&
        Objects.equals(method, requestId.method);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, method);
  }
}
