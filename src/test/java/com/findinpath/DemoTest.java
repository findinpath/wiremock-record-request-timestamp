package com.findinpath;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.Date;
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


  @BeforeEach
  public void setup() {
    var configuration = new WireMockConfiguration()
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
    var beginningOfTheTest = new Date();
    Thread.sleep(10);

    given()
        .when()
        .get(GLOSSARY_ENDPOINT_URL)
        .then().
        assertThat().statusCode(200);

    var loggedRequests  = wireMockServer.findAll(getRequestedFor(urlMatching(GLOSSARY_ENDPOINT)));

    assertThat(loggedRequests, hasSize(1));
    assertThat(loggedRequests.get(0).getLoggedDate(), greaterThan(beginningOfTheTest));

    Thread.sleep(10);
    given()
        .when()
        .get(GLOSSARY_ENDPOINT_URL)
        .then().
        assertThat().statusCode(200);

    loggedRequests  = wireMockServer.findAll(getRequestedFor(urlMatching(GLOSSARY_ENDPOINT)));
    assertThat(loggedRequests, hasSize(2));
    assertThat(loggedRequests.get(0).getLoggedDate(), greaterThan(beginningOfTheTest));
    assertThat(loggedRequests.get(1).getLoggedDate(), greaterThan(loggedRequests.get(0).getLoggedDate()));
  }


  @Test
  public void requestTimestampsShouldBeRecordedAlsoForEndpointsWithoutStubs() throws Exception {
    var beginningOfTheTest = new Date();
    Thread.sleep(10);

    var unknownEndpoint = "/unknown";
    given()
        .when()
        .get("http://localhost:" + WIREMOCK_PORT + unknownEndpoint)
        .then().
        assertThat().statusCode(404);

    var loggedRequests  = wireMockServer.findAll(getRequestedFor(urlMatching(unknownEndpoint)));

    assertThat(loggedRequests, hasSize(1));
    assertThat(loggedRequests.get(0).getLoggedDate(), greaterThan(beginningOfTheTest));
  }

  private void setupStubs() {
    wireMockServer.stubFor(get(urlEqualTo("/glossary"))
        .willReturn(aResponse().withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBodyFile("glossary.json")));
  }

}
