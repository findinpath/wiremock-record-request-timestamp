Record the timestamp of requests made towards WireMock server
=============================================================

This project is a proof of concept for showing how to log the timestamp of the requests
made toward the [WireMock](http://wiremock.org/) server.

WireMock records by default information about which requests are made to the mock
server and also when these requests were made.

Such a functionality can come in handy in tests where it is needed to make sure that a certain
API call happens only after another event (e.g. : the API call made to insert the corresponding
entry in the search index engine - Apache Solr, elasticearch - is done only after the entry has 
been saved in the relational database - otherwise the search engine could return inaccurate data 
in case of database failure).


Consult [Wiremock Verifying](http://wiremock.org/docs/verifying/) section for more details on
the available operations for retrieving the details about the requests performed towards WireMock.

WireMock also provides a series of instruments that can be used to extend its basic functionality:

- `com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilter`
- `com.github.tomakehurst.wiremock.extension.PostServeAction`
- `com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer`

When registering an extension based on these classes in WireMock, there could be accessed
the requests made towards WireMock and specific callbacks could be registered depending on the
needs in the tests.

Out of the classes enumerated earlier, the interface  `com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilter`
is the most appropriate for recording the exact timestamp when a request has been made because it doesn't
take into consideration the eventual delays configured for the stub endpoint on WireMock.
