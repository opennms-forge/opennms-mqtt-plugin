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

public class XMLCollectorTests {
	private static final Logger LOG = LoggerFactory.getLogger(XMLCollectorTests.class);

	@Test
	public void testMarshalUnmarshalConfig() throws JAXBException, FileNotFoundException {

		JAXBContext jaxbContext = JAXBContext.newInstance(XmlDataCollectionConfig.class);

		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter sw = new StringWriter();
		XmlGroups xmlGroups = new XmlGroups();
		XmlGroup xmlGroup = new XmlGroup();
		XmlObject xmlObject = new XmlObject();
		AttributeType attributeType = AttributeType.GAUGE;
		xmlObject.setDataType(attributeType);
		xmlObject.setName("name");
		xmlObject.setXpath("xpath");

		xmlGroup.addXmlObject(xmlObject);
		xmlGroups.addXmlGroup(xmlGroup);
		jaxbMarshaller.marshal(xmlGroups, sw);
		System.out.println("marshalled object:" + sw.toString());

		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		XmlGroups xmlFile1XmlGroups = (XmlGroups) jaxbUnmarshaller
				.unmarshal(new FileInputStream("src/test/resources/xml-datacollection/xmlfile1-config.xml"));
		sw = new StringWriter();
		jaxbMarshaller.marshal(xmlFile1XmlGroups, sw);
		System.out.println("xmlFile1XmlGroups:" + sw.toString());

	}

	@Test
	public void testloaddocument() throws Exception {
		ExtendedDefaultXmlCollectionHandler xmlCollectionHandler = new ExtendedDefaultXmlCollectionHandler();
		String currentDir = System.getProperty("user.dir");
		System.out.println("working directory:" + currentDir);
		Document doc = xmlCollectionHandler.getXmlDocument(
				"file:///" + currentDir + "/src/test/resources/xmlFiles/nokia_oly-mpls0-cpeData.xml", null);

		JAXBContext jaxbContext = JAXBContext.newInstance(XmlDataCollectionConfig.class);
		XmlSource source = new XmlSource();
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		XmlGroups xmlGroups = (XmlGroups) jaxbUnmarshaller
				.unmarshal(new FileInputStream("src/test/resources/xml-datacollection/xmlfile1-config.xml"));
		source.setXmlGroups(xmlGroups.getXmlGroups());

		int nodeId = 1;
		String nodeLabel = "locahost";
		String foreignSource = "testforeignsource";
		String foreignId = "testforeignid";
		InetAddress ipAddress = InetAddress.getLocalHost();

		CollectionAgent agent = new MockCollectionAgent(nodeId, nodeLabel, foreignSource, foreignId, ipAddress);
		CollectionSetBuilder builder = new CollectionSetBuilder(agent);

		xmlCollectionHandler.fillCollectionSet(agent, builder, source, doc);

		System.out.println(
				"builder numAttributes " + builder.getNumAttributes() + " numResources " + builder.getNumResources());
		CollectionSetDTO collectionSetDTO = builder.build();
		System.out.println("collectionSetDTO  " + collectionSetDTO.toString().replace(",", ",\n"));

	}

}
