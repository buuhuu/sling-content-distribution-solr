package com.github.buuhuu.aem.weretail.solr.components;

import static com.github.buuhuu.aem.weretail.solr.FieldNames.TEXT;

import java.util.Locale;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.adobe.cq.commerce.api.Product;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.github.buuhuu.sling.distribution.solr.documents.models.HasSolrFields;
import com.github.buuhuu.sling.distribution.solr.documents.models.SolrFieldGetter;

@Model(adaptables = Resource.class, adapters = { ProductSolrFields.class,
        HasSolrFields.class }, resourceType = "weretail/components/structure/product")
public class ProductSolrFields implements HasSolrFields {

    @Self
    private Resource self;

    @Self
    private Product product;

    @ValueMapValue(name = "cq:commerceType")
    private String type;

    private String language;

    @PostConstruct
    protected void postConstruct() {
        if (!StringUtils.equals("product", type)) {
            throw new IllegalArgumentException("Only products of type 'product' supported.");
        }

        PageManager pageManager = self.getResourceResolver().adaptTo(PageManager.class);
        language = Optional.ofNullable(pageManager.getContainingPage(self))
                .map(Page::getLanguage)
                .map(Locale::getLanguage)
                .orElse(StringUtils.EMPTY);
    }

    @SolrFieldGetter(name = TEXT)
    public String getIdentifier() {
        return product.getProperty("identifier", String.class);
    }

    @SolrFieldGetter(name = TEXT)
    public String getDescription() {
        return product.getDescription(language);
    }

    @SolrFieldGetter(name = TEXT)
    public String getFeatures() {
        return Jsoup.clean(product.getProperty("features", language, String.class), Whitelist.none());
    }

    @SolrFieldGetter(name = TEXT)
    public String getTitle() {
        return product.getTitle(language);
    }

    @SolrFieldGetter(name = TEXT)
    public String getSummary() {
        return product.getProperty("summary", language, String.class);
    }
}
