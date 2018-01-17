package com.github.buuhuu.aem.weretail.solr.aggregates;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.sling.api.resource.Resource;

import com.github.buuhuu.sling.distribution.solr.documents.SolrDocumentBuilder;
import com.github.buuhuu.sling.distribution.solr.documents.SolrField;

class TraversingSolrFieldsIterator implements Iterator<SolrField> {

    private final Iterator<Iterator<SolrField>> solrFields;
    private Iterator<SolrField> currentFields;
    private SolrField next;

    TraversingSolrFieldsIterator(Resource resource, SolrDocumentBuilder documentBuilder) {
        solrFields = traverse(resource).map(documentBuilder::getSolrFields).iterator();
        seek();
    }

    private static Stream<Resource> traverse(Resource resource) {
        return Stream.concat(
                Stream.of(resource),
                StreamSupport.stream(resource.getChildren().spliterator(), false)
                        .flatMap(TraversingSolrFieldsIterator::traverse));
    }

    private void seek() {
        if (currentFields != null && currentFields.hasNext()) {
            next = currentFields.next();
        } else if (solrFields.hasNext()) {
            currentFields = solrFields.next();
            seek();
        } else {
            next = null;
        }
    }

    @Override public boolean hasNext() {
        return next != null;
    }

    @Override public SolrField next() {
        SolrField current = next;
        seek();
        return current;
    }
}
