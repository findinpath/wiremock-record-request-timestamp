Record the timestamp of requests made towards WireMock server
=============================================================

This project is a proof of concept for showing how to log the timestamp of the requests
made toward the [WireMock](http://wiremock.org/) server.

WireMock doesn't record by default information about which requests are made to the mock
server or when these requests were made. On the other hand, WireMock provides a series
of instruments on how to get these information:

- `com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilter`
- `com.github.tomakehurst.wiremock.extension.PostServeAction`
- `com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer`

When registering an extension based on these classes in WireMock, there could be accessed
the requests made towards WireMock and therefor the requests and their corresponding timestamps 
could be logged to a repository used for test purposes.

Such a functionality can come in handy in tests where it is needed to make sure that a certain
API call happens only after another event (e.g. : the API call made to insert the corresponding
entry in the search index engine - Apache Solr, elasticearch - is done only after the entry has 
been saved in the relational database - otherwise the search engine could return inaccurate data 
in case of database failure).

Out of the classes enumerated earlier, the interface  `com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilter`
is the most appropriate for recording the exact timestamp when a request has been made because it doesn't
take into consideration the eventual delays configured for the stub endpoint on WireMock.

As described in the `com.findinpath.DemoTest` class, 
by means of a `Consumer` for the requests sent towards WireMock, 
the timestamps of the requests (even for unconfigured stubs) are
recorded and can be therefor used in the assertions from the tests.  

```java
    Consumer<Request> consumerRequest = request -> requestTimestampsMap.computeIfAbsent(
        new RequestId(request.getUrl(), request.getMethod()),
        requestId -> new LinkedList<>()
    ).add(Instant.now());
    var consumerRequestFilter = new ConsumerRequestFilter(consumerRequest);
    var configuration = new WireMockConfiguration()
        .extensions(consumerRequestFilter)
        .port(WIREMOCK_PORT);


    ...

    List<Instant> glossaryRequestTimestamps = getRecordedTimestamps(GLOSSARY_ENDPOINT, RequestMethod.GET);
    assertThat(glossaryRequestTimestamps, hasSize(2));
```