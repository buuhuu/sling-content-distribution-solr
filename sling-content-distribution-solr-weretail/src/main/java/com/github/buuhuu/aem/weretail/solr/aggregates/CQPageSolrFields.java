package com.github.buuhuu.aem.weretail.solr.aggregates;

import java.util.Iterator;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.github.buuhuu.sling.distribution.solr.documents.SolrDocumentBuilder;
import com.github.buuhuu.sling.distribution.solr.documents.SolrField;
import com.github.buuhuu.sling.distribution.solr.documents.models.HasSolrFields;

@Model(adaptables = Resource.class, resourceType = "cq:Page", adapters = { CQPageSolrFields.class, HasSolrFields.class })
public class CQPageSolrFields implements HasSolrFields {

    @ChildResource(name = "jcr:content")
    private Resource content;

    @OSGiService
    private SolrDocumentBuilder documentBuilder;

    @Override
    public Iterator<SolrField> getSolrFields() {
        return new TraversingSolrFieldsIterator(content, documentBuilder);
    }
}
