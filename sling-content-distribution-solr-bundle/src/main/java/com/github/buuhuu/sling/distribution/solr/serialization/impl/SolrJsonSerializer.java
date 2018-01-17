package com.github.buuhuu.sling.distribution.solr.serialization.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.distribution.DistributionRequest;
import org.apache.sling.distribution.DistributionRequestType;
import org.apache.sling.distribution.common.DistributionException;
import org.apache.sling.distribution.serialization.DistributionContentSerializer;
import org.apache.sling.distribution.serialization.DistributionExportOptions;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.buuhuu.sling.distribution.solr.documents.SolrDocumentBuilder;
import com.github.buuhuu.sling.distribution.solr.documents.SolrField;

/**
 * This {@link DistributionContentSerializer} writes in the solr json format for creating or removing documents.
 */
@Component(
        service = DistributionContentSerializer.class,
        property = "name=solr-json"
)
public class SolrJsonSerializer implements DistributionContentSerializer {

    private static final Logger LOG = LoggerFactory.getLogger(SolrJsonSerializer.class);

    @Reference(policyOption = ReferencePolicyOption.GREEDY)
    private SolrDocumentBuilder documentBuilder;

    public void exportToStream(ResourceResolver resourceResolver, DistributionExportOptions distributionExportOptions,
            OutputStream outputStream) throws DistributionException {
        DistributionRequest request = distributionExportOptions.getRequest();

        if (request == null) {
            throw new DistributionException("DistributionRequest is null.");
        }

        DistributionRequestType type = request.getRequestType();

        JsonGenerator generator = Json.createGeneratorFactory(null).createGenerator(outputStream, Charset.forName("UTF-8"));
        Instant now = Instant.now();
        generator.writeStartObject();

        for (String path : request.getPaths()) {
            switch (type) {
            case ADD:
                exportAddPath(generator, path, now, resourceResolver);
                break;
            case DELETE:
                exportDeletePath(generator, path);
                break;
            default:
                LOG.debug("Ignore request of type '{}'.", type);
                break;
            }
        }

        generator.writeEnd();
        generator.flush();
    }

    public void importFromStream(ResourceResolver resourceResolver, InputStream inputStream) throws DistributionException {
        throw new DistributionException("unsupported");
    }

    public String getName() {
        return "solr-json";
    }

    public String getContentType() {
        return "application/json; charset=UTF-8";
    }

    public boolean isRequestFiltering() {
        return true;
    }

    public boolean isDeletionSupported() {
        return true;
    }

    private void exportAddPath(JsonGenerator generator, String path, Instant indexTime, ResourceResolver resourceResolver)
            throws DistributionException {
        Resource resource = resourceResolver.getResource(path);

        if (resource == null) {
            LOG.debug("Resource at path '{}' is null, skip exporting.", path);
            return;
        }

        LOG.trace("Exporting 'add' to solr-json {}.", path);

        Iterator<SolrField> solrFields = documentBuilder.getSolrFields(resource);
        if (solrFields.hasNext()) {
            // here the actual document is created the only necessary field is id, which is used in exportDeletePath to delete documents by id
            // basically the document created should be delegated to any kind of service and or using sling models similar to its exporter
            // feature.
            generator.writeStartObject("add");
            generator.writeStartObject("doc");
            generator.write("id", documentBuilder.getId(resource));
            generator.write("_name", resource.getName());
            generator.write("_path", resource.getPath());
            generator.write("_indexedAt", indexTime.toString()); // ISO 8601

            while (solrFields.hasNext()) {
                write(generator, solrFields.next());
            }

            generator.writeEnd();
            generator.writeEnd();
        } else {
            // in case the document was indexed before we delete it now
            exportDeleteId(generator, path);
        }
    }

    private void write(JsonGenerator generator, SolrField solrField) {
        write(generator, solrField.getName(), solrField.getValue());
    }

    private void write(JsonGenerator generator, String name, Object value) {
        // value normalization
        if (value instanceof Object[] || value instanceof Iterable<?>) {
            generator.writeStartArray(name);
            if (value instanceof Object[]) {
                value = Arrays.asList((Object[]) value);
            }
            write(generator, (Iterable<?>) value);
            generator.writeEnd();
        } else {
            // single value
            value = normalizeValue(value);
            if (value == null) {
                generator.writeNull(name);
            } else if (value instanceof Double) {
                generator.write(name, (Double) value);
            } else if (value instanceof Long) {
                generator.write(name, (Long) value);
            } else if (value instanceof Boolean) {
                generator.write(name, (Boolean) value);
            } else {
                generator.write(name, String.valueOf(value));
            }
        }
    }

    private void write(JsonGenerator generator, Iterable<?> values) {
        for (Object singleValue : values) {
            singleValue = normalizeValue(singleValue);
            if (singleValue == null) {
                generator.writeNull();
            } else if (singleValue instanceof Double) {
                generator.write((Double) singleValue);
            } else if (singleValue instanceof Long) {
                generator.write((Long) singleValue);
            } else if (singleValue instanceof Boolean) {
                generator.write((Boolean) singleValue);
            } else {
                generator.write(String.valueOf(singleValue));
            }
        }
    }

    private void exportDeleteId(JsonGenerator generator, String path) {
        LOG.trace("Exporting 'delete' to solr-json id={}.", path);

        generator.writeStartObject("delete");
        generator.write("query", "id:\"" + path + '"');
        generator.writeEnd();
    }

    private void exportDeletePath(JsonGenerator generator, String path) {
        LOG.trace("Exporting 'delete' to solr-json _path={}*.", path);

        generator.writeStartObject("delete");
        generator.write("query", "_path:\"" + path + '"');
        generator.writeEnd();
    }

    private static Object normalizeValue(Object value) {
        // numeric normalisation
        if (value instanceof Float) {
            return new Double((Float) value);
        }
        if (value instanceof Integer) {
            return new Long((Integer) value);
        }
        // date/time normalisation
        if (value instanceof Date) {
            Calendar calendarValue = Calendar.getInstance();
            calendarValue.setTime((Date) value);
            value = calendarValue;
        }
        if (value instanceof Calendar) {
            value = ((Calendar) value).toInstant();
        }
        if (value instanceof Instant) {
            return value.toString();
        }

        return value;
    }
}
