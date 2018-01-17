# sling-content-distribution-solr

Solr ingestion using sling content distribution example built for AEM 6.3. There are no dependencies to AEM so it will probably also run fine one any kind of Sling application, though the setup might differ slightly. 

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

## Why?

- For some requirmenets AEM's (Sling's, Oak's) fulltext search capabilities are not sufficient (OAK-6597, OAK-7109 to name only a few)
- AEM's internal indexes are always on technical, storage level not on business level
- Standalone (isolated) search solutions of much more flexibility to configure and tune to get the best results (not only for fulltext search but also other search based applications)

## How?

- In sling-content-distribution-solr-bundle there is a DistributionPackageSerializer implemented that writes changes (add, remove) to solr-json format according to https://lucene.apache.org/solr/guide/7_2/uploading-data-with-index-handlers.html
- The serializer in that example only writes the exported resource's name and path but that can be anything, for example defined by a service implemention and/or a Sling Model
- In sling-content-distribution-solr-bundle there is an extension of solr's UpdateRequestHandler implemented that can process the binary stream written by Sling Content Distribution (independently from the serializer)

## Required changes on top of Sling Content Distribution Core 0.2.11-SNAPSHOT and Jackrabbit File Vault 3.1.43-SNAPSHOT

- https://issues.apache.org/jira/browse/JCRVLT-257
- https://issues.apache.org/jira/browse/SLING-7357
- https://issues.apache.org/jira/browse/SLING-7358
- https://issues.apache.org/jira/browse/SLING-7359
- https://issues.apache.org/jira/browse/SLING-7360

## Setup

Configure solr to use the SCDUpdateRequestHandler, for example by putting the following configuration into your solrconfig.xml (make sure to load the sling-content-distribution-solr-loader as lib)

```
<requestHandler name="/update/scd" class="com.github.buuhuu.solr.handler.SCDUpdateRequestHandler" />
```

Note: the above is not necessary anymore with the change proposed in https://github.com/apache/sling-org-apache-sling-distribution-core/pull/6

After depoloying the **sling-content-distribution-solr-bundle**:

- Configure a new Package Builder Factory (either file or resource) with the format solr-json
- Configure a Distribution Event Trigger (for example for path /content)
- Configure a new Forward Distribution Agent that uses the previously created package builder and trigger with its endpoint pointing to the SCDUpdateRequestHandler configured above: http://localhost:8983/solr/aem/update/scd?commitWithin=1000

Next configure AEM's default replication action to use sling content distribution and replicate something
