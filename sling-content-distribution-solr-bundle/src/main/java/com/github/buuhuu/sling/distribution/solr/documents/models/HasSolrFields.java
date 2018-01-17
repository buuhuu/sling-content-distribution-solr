package com.github.buuhuu.sling.distribution.solr.documents.models;

import java.util.Iterator;

import com.github.buuhuu.sling.distribution.solr.documents.SolrField;

/**
 * A marker interface for models to be picked up for SolrFields.
 * <p>
 * The implementation can overwrite the optional method {@link HasSolrFields#getSolrFields()} to provide custom behavior resolving the
 * {@link SolrField}s of the model. The default implementation uses {@link SolrFieldsUtil#getSolrFields(HasSolrFields)} to resolve any field
 * from a public getter annotated with {@link SolrFieldGetter}.
 */
public interface HasSolrFields {

    default Iterator<SolrField> getSolrFields() {
        return SolrFieldsUtil.getSolrFields(this);
    }
}
