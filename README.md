# sling-content-distribution-solr

Solr ingestion using sling content distribution example built for AEM 6.3 (though there are no dependencies so it should run on plain sling as well)

## Goal

Sling Content Distribution (esp forward distribution) supports a lot of features that make it quite simple to integrate content managed in sling into slor:

- (local) queuing that supports guarantee of processing
- guarantee of order
- concurrent execution with priority queues
- throttleing (concatenating multiple actions of the same type into a single package)
- delete, remove actions
- various triggers (jcr events, distribution events, manual, ...?)
- quite flexible SPIs to implement

all of that perfeclty fits for the usecase to integrate sling into solr by sending adds and removes in a guaranteed order from sling to solr.

## Required changes on top of Sling Content Distribution Core 0.2.11-SNAPSHOT and Jackrabbit File Vault 3.1.43-SNAPSHOT

- https://issues.apache.org/jira/browse/JCRVLT-257
- https://issues.apache.org/jira/browse/SLING-7357
- https://issues.apache.org/jira/browse/SLING-7358
- https://issues.apache.org/jira/browse/SLING-7359
- https://issues.apache.org/jira/browse/SLING-7360

## Setup

Configure solr to use the SCDUpdateRequestHandler, for example by putting the following configuration into your solrconfig.xml

```
<requestHandler name="/update/scd" class="com.github.buuhuu.solr.handler.SCDUpdateRequestHandler" />
```

After depoloying the **sling-content-distribution-solr-bundle**:

- Configure a new Package Builder Factory (either file or resource) with the format solr-json
- Configure a Distribution Event Trigger (for example for path /content)
- Configure a new Forward Distribution Agent that uses the previously created package builder and trigger

Next configure AEM's default replication action to use sling content distribution and replicate something
