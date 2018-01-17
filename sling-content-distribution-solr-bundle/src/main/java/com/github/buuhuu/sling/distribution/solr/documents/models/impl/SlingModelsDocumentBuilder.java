package com.github.buuhuu.sling.distribution.solr.documents.models.impl;

import java.util.Collections;
import java.util.Iterator;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.buuhuu.sling.distribution.solr.documents.SolrDocumentBuilder;
import com.github.buuhuu.sling.distribution.solr.documents.SolrField;
import com.github.buuhuu.sling.distribution.solr.documents.models.HasSolrFields;

@Component(
        service = SolrDocumentBuilder.class
)
public class SlingModelsDocumentBuilder implements SolrDocumentBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(SlingModelsDocumentBuilder.class);

    @Reference
    private ModelFactory modelFactory;

    @Override
    public Iterator<SolrField> getSolrFields(Resource resource) {
        try {
            return modelFactory.createModel(resource, HasSolrFields.class).getSolrFields();
        } catch (Exception ex) {
            LOG.debug("Failed to get SolrField iterator of {}", resource, ex);
            return Collections.emptyIterator();
        }
    }
}
