package com.github.buuhuu.aem.weretail.solr.components;

import static com.github.buuhuu.aem.weretail.solr.FieldNames.TEXT;

import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.content.FragmentRenderService;
import com.github.buuhuu.sling.distribution.solr.documents.models.HasSolrFields;
import com.github.buuhuu.sling.distribution.solr.documents.models.SolrFieldGetter;

@Model(adaptables = Resource.class, adapters = { ContentFragmentSolrFields.class,
        HasSolrFields.class }, resourceType = "dam/cfm/components/contentfragment")
public class ContentFragmentSolrFields implements HasSolrFields {

    @Self
    private Resource self;

    @ValueMapValue
    private String fileReference;

    @OSGiService
    private FragmentRenderService renderService;

    private String[] paragraphs;

    @PostConstruct
    protected void postConstruct() {
        Resource cf = self.getResourceResolver().getResource(fileReference);

        if (cf == null || cf.adaptTo(ContentFragment.class) == null) {
            throw new IllegalArgumentException("No content fragment could be resolved for: " + fileReference);
        }

        String content = renderService.render(self);
        if (StringUtils.isBlank(content)) {
            paragraphs = new String[0];
        } else {
            // from libs.dam.cfm.components.contentfragment.ContentFragmentUsePojo
            paragraphs = Arrays.stream(content.split("(?=(<p>|<h1>|<h2>|<h3>|<h4>|<h5>|<h6>))"))
                    .map(paragraph -> Jsoup.clean(paragraph, Whitelist.none()))
                    .toArray(String[]::new);
        }
    }

    @SolrFieldGetter(name = TEXT)
    public String[] getParagraphs() {
        return paragraphs;
    }
}
