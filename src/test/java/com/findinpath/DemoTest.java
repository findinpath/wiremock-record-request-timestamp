package com.findinpath;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The test cases in this class demonstrate how the timestamps of the requests made towards WireMock
 * can be recorded and used in unit tests.
 */
public class DemoTest {

  private static final int WIREMOCK_PORT = 8090;
  private static final String GLOSSARY_ENDPOINT = "/glossary";
  private static final String GLOSSARY_ENDPOINT_URL =
      "http://localhost:" + WIREMOCK_PORT + GLOSSARY_ENDPOINT;

  private WireMockServer wireMockServer;

  private Map<RequestId, List<Instant>> requestTimestampsMap;


  @BeforeEach
  public void setup() {
    requestTimestampsMap = new HashMap<>();
    Consumer<Request> consumerRequest = request -> requestTimestampsMap.computeIfAbsent(
        new RequestId(request.getUrl(), request.getMethod()),
        requestId -> new LinkedList<>()
    ).add(Instant.now());
    var consumerRequestFilter = new ConsumerRequestFilter(consumerRequest);
    var configuration = new WireMockConfiguration()
        .extensions(consumerRequestFilter)
        .port(WIREMOCK_PORT);
    wireMockServer = new WireMockServer(configuration);
    wireMockServer.start();
    setupStubs();
  }

  @AfterEach
  public void teardown() {
    wireMockServer.stop();
  }


  @Test
  public void requestTimestampsShouldBeRecorded() throws Exception {
    var beginningOfTheTest = Instant.now();
    Thread.sleep(10);
    // at the beginning of the test there shouldn't be any entry in the requestTimestampsMap
    assertThat(requestTimestampsMap.entrySet(), hasSize(0));

    given()
        .when()
        .get(GLOSSARY_ENDPOINT_URL)
        .then().
        assertThat().statusCode(200);

    assertThat(requestTimestampsMap.entrySet(), hasSize(1));
    var glossaryRequestTimestamps = getRecordedTimestamps(GLOSSARY_ENDPOINT, RequestMethod.GET);
    assertThat(glossaryRequestTimestamps, hasSize(1));
    assertThat(glossaryRequestTimestamps.get(0), greaterThan(beginningOfTheTest));

    Thread.sleep(10);
    given()
        .when()
        .get(GLOSSARY_ENDPOINT_URL)
        .then().
        assertThat().statusCode(200);

    glossaryRequestTimestamps = getRecordedTimestamps(GLOSSARY_ENDPOINT, RequestMethod.GET);
    assertThat(glossaryRequestTimestamps, hasSize(2));
    assertThat(glossaryRequestTimestamps.get(0), greaterThan(beginningOfTheTest));
    assertThat(glossaryRequestTimestamps.get(1), greaterThan(glossaryRequestTimestamps.get(0)));
  }


  @Test
  public void requestTimestampsShouldBeRecordedAlsoForEndpointsWithoutStubs() throws Exception {
    var beginningOfTheTest = Instant.now();
    Thread.sleep(10);
    assertThat(requestTimestampsMap.entrySet(), hasSize(0));

    var unknownEndpoint = "/unknown";
    given()
        .when()
        .get("http://localhost:" + WIREMOCK_PORT + unknownEndpoint)
        .then().
        assertThat().statusCode(404);

    assertThat(requestTimestampsMap.entrySet(), hasSize(1));
    var glossaryRequestTimestamps = getRecordedTimestamps(unknownEndpoint, RequestMethod.GET);
    assertThat(glossaryRequestTimestamps, hasSize(1));
    assertThat(glossaryRequestTimestamps.get(0), greaterThan(beginningOfTheTest));
  }

  private void setupStubs() {
    wireMockServer.stubFor(get(urlEqualTo("/glossary"))
        .willReturn(aResponse().withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBodyFile("glossary.json")));
  }

  private List<Instant> getRecordedTimestamps(String url, RequestMethod requestMethod) {
    return requestTimestampsMap.getOrDefault(new RequestId(url, requestMethod),
        Collections.emptyList());
  }
}
