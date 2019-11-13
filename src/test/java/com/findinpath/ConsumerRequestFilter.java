package com.findinpath;


import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilter;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilterAction;
import com.github.tomakehurst.wiremock.http.Request;
import java.util.function.Consumer;

/**
 * Generic extension for the WireMock server used for consuming
 * each request before it lands on the mock server.
 *
 * Such an extension could be useful in scenarios where it is
 * needed to record the exact timestamp of each request made to
 * the WireMock server.
 *
 */
class ConsumerRequestFilter implements RequestFilter {

  private final Consumer<Request> requestConsumer;

  public ConsumerRequestFilter(Consumer<Request> requestConsumer){
    this.requestConsumer = requestConsumer;
  }

  @Override
  public boolean applyToStubs() {
    return true;
  }

  @Override
  public RequestFilterAction filter(Request request) {
    requestConsumer.accept(request);
    return RequestFilterAction.continueWith(request);
  }

  @Override
  public boolean applyToAdmin() {
    return false;
  }


  @Override
  public String getName() {
    return "consumer-request-filter";
  }



}