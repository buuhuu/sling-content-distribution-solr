package com.github.buuhuu.sling.distribution.solr.documents.models;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to annotate a getter method of a class as {@link com.github.buuhuu.sling.distribution.solr.documents.SolrField}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SolrFieldGetter {
    /**
     * The fields name.
     *
     * @return
     */
    String name();
}
