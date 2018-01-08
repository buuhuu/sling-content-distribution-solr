package com.github.buuhuu.sling.distribution.solr.serialization.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.distribution.DistributionRequest;
import org.apache.sling.distribution.DistributionRequestType;
import org.apache.sling.distribution.common.DistributionException;
import org.apache.sling.distribution.serialization.DistributionContentSerializer;
import org.apache.sling.distribution.serialization.DistributionExportFilter;
import org.apache.sling.distribution.serialization.DistributionExportOptions;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link DistributionContentSerializer} writes in the solr json format for creating or removing documents.
 */
@Component(
        service = DistributionContentSerializer.class,
        property = "name=solr-json"
)
public class SolrJsonSerializer implements DistributionContentSerializer {

    private static final Logger LOG = LoggerFactory.getLogger(SolrJsonSerializer.class);

    public void exportToStream(ResourceResolver resourceResolver, DistributionExportOptions distributionExportOptions,
            OutputStream outputStream) {
        DistributionRequest request = distributionExportOptions.getRequest();
        DistributionRequestType type = request.getRequestType();
        DistributionExportFilter filter = distributionExportOptions.getFilter();

        JsonGenerator generator = Json.createGeneratorFactory(null).createGenerator(outputStream, Charset.forName("UTF-8"));
        generator.writeStartObject();

        for (String path : request.getPaths()) {
            switch (type) {
            case ADD:
                exportAddPath(generator, path, filter, resourceResolver);
                break;
            case DELETE:
                exportDeletePath(generator, path, filter);
                break;
            default:
                LOG.debug("Ignore request of type '{}'.", type);
                break;
            }
        }

        generator.writeEnd();
        generator.flush();
    }

    private void exportAddPath(JsonGenerator generator, String path, DistributionExportFilter filter, ResourceResolver resourceResolver) {
        Resource resource = resourceResolver.getResource(path);

        if (resource == null) {
            LOG.debug("Resource at path '{}' is null, skip exporting.", path);
            return;
        }

        LOG.trace("Exporting 'add' to solr-json {}.", path);

        // here the actual document is created the only necessary field is id, which is used in exportDeletePath to delete documents by id
        // basically the document created should be delegated to any kind of service and or using sling models similar to its exporter
        // feature.
        generator.writeStartObject("add");
        generator.writeStartObject("doc");
        generator.write("id", path);
        generator.write("name", resource.getName());
        generator.write("path", resource.getPath());
        generator.writeEnd();
        generator.writeEnd();
    }

    private void exportDeletePath(JsonGenerator generator, String path, DistributionExportFilter filter) {
        LOG.trace("Exporting 'delete' to solr-json {}.", path);

        generator.writeStartObject("delete");
        generator.write("id", path);
        generator.writeEnd();
    }

    public void importFromStream(ResourceResolver resourceResolver, InputStream inputStream) throws DistributionException {
        throw new DistributionException("unsupported");
    }

    public String getName() {
        return "solr-json-serializer";
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
}
