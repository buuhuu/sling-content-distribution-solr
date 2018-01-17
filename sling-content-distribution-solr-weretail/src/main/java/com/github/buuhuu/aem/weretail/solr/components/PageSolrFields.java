package com.github.buuhuu.aem.weretail.solr.components;

import static com.github.buuhuu.aem.weretail.solr.FieldNames.DESCRIPTION;
import static com.github.buuhuu.aem.weretail.solr.FieldNames.TAGS;
import static com.github.buuhuu.aem.weretail.solr.FieldNames.TITLE;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.github.buuhuu.sling.distribution.solr.documents.models.HasSolrFields;
import com.github.buuhuu.sling.distribution.solr.documents.models.SolrFieldGetter;

@Model(adaptables = Resource.class, adapters = { PageSolrFields.class,
        HasSolrFields.class }, resourceType = "weretail/components/structure/page")
public class PageSolrFields implements HasSolrFields {

    @Self
    private Resource self;

    @ValueMapValue(name = "jcr:title", injectionStrategy = InjectionStrategy.OPTIONAL)
    private String jcrTitle;

    @ValueMapValue(name = "jcr:description", injectionStrategy = InjectionStrategy.OPTIONAL)
    private String jcrDescription;

    @ValueMapValue(name = "pageTitle", injectionStrategy = InjectionStrategy.OPTIONAL)
    private String pageTitle;

    @ValueMapValue(name = "cq:tags", injectionStrategy = InjectionStrategy.OPTIONAL)
    private String[] tags;

    private Function<Tag, String> tagTranslator;
    private TagManager tagManager;

    @PostConstruct
    private void postConstruct() {
        PageManager pageManager = self.getResourceResolver().adaptTo(PageManager.class);

        tagManager = self.getResourceResolver().adaptTo(TagManager.class);
        tagTranslator = Optional.ofNullable(pageManager.getContainingPage(self))
                .map(Page::getLanguage)
                .<Function<Tag, String>>map(locale -> tag -> tag.getTitle(locale)).orElse(tag -> tag.getTitle());
    }

    @SolrFieldGetter(name = TITLE)
    public String getTitle() {
        if (!StringUtils.isBlank(pageTitle)) {
            return pageTitle;
        } else if (!StringUtils.isBlank(jcrTitle)) {
            return jcrTitle;
        } else {
            return self.getName();
        }
    }

    @SolrFieldGetter(name = DESCRIPTION)
    public String getDescription() {
        return jcrDescription;
    }

    @SolrFieldGetter(name = TAGS)
    public String[] getTags() {
        return Optional.ofNullable(tags)
                .map(tags -> Arrays.stream(tags)
                        .map(tagManager::resolve)
                        .filter(Objects::nonNull)
                        .map(tagTranslator)
                        .toArray(size -> new String[size]))
                .orElse(new String[0]);
    }
}
