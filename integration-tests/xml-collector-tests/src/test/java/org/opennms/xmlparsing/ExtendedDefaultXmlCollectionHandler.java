package org.opennms.xmlparsing;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.protocols.xml.config.Request;
import org.opennms.protocols.xml.config.XmlSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class ExtendedDefaultXmlCollectionHandler extends ExtendedAbstractXmlCollectionHandler {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ExtendedDefaultXmlCollectionHandler.class);

    @Override
    public void processXmlResource(CollectionSetBuilder builder, Resource collectionResource, String resourceTypeName, String group) { };

    @Override
    public void fillCollectionSet(String urlString, Request request, CollectionAgent agent, CollectionSetBuilder builder, XmlSource source) throws Exception {
        final Document doc = getXmlDocument(urlString, request);
        LOG.debug("fillCollectionSet: parsed document for source url '{}' collection", urlString);
        fillCollectionSet(agent, builder, source, doc);
    }

}