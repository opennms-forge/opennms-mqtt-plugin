package org.opennms.plugins.iot.collectionsim;

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.jupiter.api.Test;
import org.opennms.plugins.iot.collectionsim.model.LogToFileResponse;

public class CollectionSimServiceTest {
	
	@Test
	void test() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(LogToFileResponse.class);  
        Marshaller marshaller = jc.createMarshaller();  
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);  

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        CollectionSimService collectionSimService = new CollectionSimService();
        
        LogToFileResponse logToFileResponse= collectionSimService.getResponse(5,60000);

        marshaller.marshal(logToFileResponse, baos);
        System.out.println("nokia data: \n"+baos.toString());
	}

}
