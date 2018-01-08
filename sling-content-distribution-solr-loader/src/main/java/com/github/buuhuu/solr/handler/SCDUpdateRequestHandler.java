package com.github.buuhuu.solr.handler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.sling.distribution.packaging.impl.DistributionPackageUtils;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.loader.ContentStreamLoader;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;

/**
 * This ContentStreamHandler extends the {@link org.apache.solr.handler.UpdateRequestHandler} but supports the binary
 * format sent by Sling Content Distribution. The header of any package is parsed and added to the SolrParams given to
 * the loader.
 */
public class SCDUpdateRequestHandler extends org.apache.solr.handler.UpdateRequestHandler {

    @Override
    protected Map<String, ContentStreamLoader> createDefaultLoaders(NamedList args) {
        Map<String, ContentStreamLoader> originals = super.createDefaultLoaders(args);

        for (String key : new ArrayList<String>(originals.keySet())) {
            originals.put(key, new SCDContentStreamLoader(originals.get(key)));
        }

        return originals;
    }

    private class SCDContentStreamLoader extends ContentStreamLoader {

        private final ContentStreamLoader delegate;

        SCDContentStreamLoader(ContentStreamLoader delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getDefaultWT() {
            return delegate.getDefaultWT();
        }

        @Override
        public void load(SolrQueryRequest req, SolrQueryResponse rsp, ContentStream stream, UpdateRequestProcessor processor)
                throws Exception {
            InputStream inputStream = stream.getStream();

            if (!inputStream.markSupported()) {
                inputStream = new BufferedInputStream(inputStream);
            }

            Map<String, Object> info = new HashMap<String, Object>();
            DistributionPackageUtils.readInfo(inputStream, info);
            ModifiableSolrParams solrParams = ModifiableSolrParams.of(req.getParams());

            for (Map.Entry<String, Object> entry : info.entrySet()) {
                Object value = entry.getValue();
                String[] vals;
                if (value instanceof Object[]) {
                    if (value instanceof String[]) {
                        vals = (String[]) value;
                    } else {
                        Object[] values = (Object[]) value;
                        vals = new String[values.length];
                        for (int i = 0; i < values.length; i++) {
                            vals[i] = String.valueOf(values[i]);
                        }
                    }
                } else {
                    vals = new String[] { String.valueOf(value) };
                }
                solrParams.add(entry.getKey(), vals);
            }

            req.setParams(solrParams);

            delegate.load(req, rsp, new SCDContentStream(inputStream, stream), processor);
        }
    }

    private class SCDContentStream extends ContentStreamBase {

        private final InputStream stream;
        private final ContentStream base;

        SCDContentStream(InputStream stream, ContentStream base) {
            this.stream = stream;
            this.base = base;
        }

        @Override
        public String getName() {
            return base.getName();
        }

        @Override
        public String getSourceInfo() {
            return base.getSourceInfo();
        }

        @Override
        public String getContentType() {
            return base.getContentType();
        }

        @Override
        public Long getSize() {
            return base.getSize();
        }

        public InputStream getStream() throws IOException {
            return stream;
        }
    }
}
