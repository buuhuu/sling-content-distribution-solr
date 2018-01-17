package com.github.buuhuu.sling.distribution.solr.documents;

/**
 * An individual, indexed field within a document.
 */
public interface SolrField {

    String getName();

    Object getValue();

}
