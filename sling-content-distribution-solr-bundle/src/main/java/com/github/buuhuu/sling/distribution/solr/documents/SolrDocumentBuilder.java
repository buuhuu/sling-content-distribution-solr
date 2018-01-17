package com.github.buuhuu.sling.distribution.solr.documents;

import java.util.Iterator;

import org.apache.sling.api.resource.Resource;

/**
 * Implementations of that provider interface are responsible for creating documents, represented as {@link Iterator} of {@link SolrField} for
 * a given {@link Resource}.
 */
public interface SolrDocumentBuilder {

    /**
     * Returns the id for the given {@link Resource}.
     *
     * @param resource
     * @return
     * @implNote in the default implementation the {@link Resource}'s path is the id returned.
     */
    default String getId(Resource resource) {
        return resource.getPath();
    }

    /**
     * Returns a document represented as {@link Iterator} of {@link SolrField} for the given {@link Resource}.
     *
     * @param resource
     * @return An {@link Iterator} of {@link SolrField} or an empty {@link Iterator} if there are no fields.
     */
    Iterator<SolrField> getSolrFields(Resource resource);

}
