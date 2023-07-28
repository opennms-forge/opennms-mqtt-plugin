package org.opennms.xmlparsing;

import static org.junit.Assert.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import javax.xml.bind.Marshaller;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.io.FileNotFoundException;

import org.opennms.core.collection.test.MockCollectionAgent;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.dto.CollectionSetDTO;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.protocols.xml.config.XmlGroup;
import org.opennms.protocols.xml.config.XmlGroups;
import org.opennms.protocols.xml.config.XmlObject;
import org.opennms.protocols.xml.config.XmlSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.protocols.xml.config.XmlDataCollectionConfig;
import org.opennms.protocols.xml.collector.DefaultXmlCollectionHandler;
import org.opennms.protocols.xml.collector.DocumentNamespaceResolver;
import org.opennms.protocols.xml.config.Request;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.junit.Test;

public class XMLCollectorTests2 {
    private static final Logger LOG = LoggerFactory.getLogger(XMLCollectorTests2.class);


	@Test 
	public void testparseGroup() throws Exception {
		
		int nodeId=1;
		String nodeLabel="locahost";
		String foreignSource= "testforeignsource";
		String foreignId="testforeignid";
        InetAddress ipAddress = InetAddress.getLocalHost();
		
		CollectionAgent agent = new MockCollectionAgent(nodeId, nodeLabel, foreignSource, foreignId,  ipAddress);
        CollectionSetBuilder builder = new CollectionSetBuilder(agent);
		
		
		
		
		ExtendedDefaultXmlCollectionHandler xmlCollectionHandler = new ExtendedDefaultXmlCollectionHandler();
		String currentDir = System.getProperty("user.dir");
		LOG.debug("working directory:"+currentDir);
		Document doc = xmlCollectionHandler.getXmlDocument("file:///"+currentDir+"/src/test/resources/xmlFiles/nokia_oly-mpls0-cpeData.xml", null);
LOG.debug(doc.getNodeName());

        NamespaceContext nc = new DocumentNamespaceResolver(doc);
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(nc);
		
		JAXBContext jaxbContext = JAXBContext.newInstance( XmlDataCollectionConfig.class );
		XmlSource source = new XmlSource();
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		XmlGroups xmlGroups = (XmlGroups) jaxbUnmarshaller.unmarshal( new FileInputStream( "src/test/resources/xml-datacollection/xmlfile1-config.xml" ));
		source.setXmlGroups(xmlGroups.getXmlGroups());

		
        for (XmlGroup group : source.getXmlGroups()) {
            LOG.debug("fillCollectionSet: getting resources for XML group {} using XPATH {}", group.getName(), group.getResourceXpath());
            Date timestamp = xmlCollectionHandler.getTimeStamp(doc, xpath, group);
            LOG.debug("timestamp="+timestamp);
           
            String xxx = xpath.evaluate(group.getResourceXpath(), doc);
            LOG.debug(doc.toString());
            LOG.debug("evaluated xpath="+xxx);
            
            NodeList resourceList = (NodeList) xpath.evaluate(group.getResourceXpath(), doc, XPathConstants.NODESET);

            LOG.debug("resourceList.getLength() = "+resourceList.getLength());
            
            for (int j = 0; j < resourceList.getLength(); j++) {
                Node resource = resourceList.item(j);
                LOG.debug("resource.nodename="+resource.getNodeName()+ " resource.notetype="+resource.getNodeType()+" resource.nodevalue="+resource.getNodeValue());
                String resourceName = xmlCollectionHandler.getResourceName(xpath, group, resource);
                LOG.debug("resource name="+resourceName+" resource type= "+group.getResourceType());
                final Resource collectionResource = xmlCollectionHandler.getCollectionResource(agent, resourceName, group.getResourceType(), timestamp);
                LOG.debug("fillCollectionSet: processing resource {}", collectionResource);
            }
        }
	}

}
