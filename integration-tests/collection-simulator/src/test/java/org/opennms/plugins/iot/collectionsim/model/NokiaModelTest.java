package org.opennms.plugins.iot.collectionsim.model;

import static org.junit.jupiter.api.Assertions.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;

import org.opennms.plugins.iot.collectionsim.model.LogToFileResponse;

import org.junit.jupiter.api.Test;

class NokiaModelTest {

	@Test
	void test() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(LogToFileResponse.class);  
        Marshaller marshaller = jc.createMarshaller();  
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);  

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        LogToFileResponse logToFileResponse= new LogToFileResponse();
        
        
       
		logToFileResponse.getLogToFileResponse().add(templateSystemCpuStatsLogRecord() );
		logToFileResponse.getLogToFileResponse().add(templateSystemCpuStatsLogRecord() );
        		
        
        marshaller.marshal(logToFileResponse, baos);
        System.out.println("nokia data: \n"+baos.toString());
	}
	
	public SystemCpuStatsLogRecord templateSystemCpuStatsLogRecord() {
		long systemCpuUsage = 9;
		long timeCaptured = 1689362117555L;
		long periodicTime = 70597;
		String monitoredObjectClass = "equipment.SystemStatsHolder";
		String monitoredObjectPointer = "network:64.184.178.239:shelf-1:systemStatsHolder";
		String displayedName = "systemStatsHolder";
		String monitoredObjectSiteId = "64.184.178.239";
		String monitoredObjectSiteName = "oly-mpls0";
		boolean suspect = false;

		SystemCpuStatsLogRecord systemCpuStatsLogRecord = new SystemCpuStatsLogRecord();
		systemCpuStatsLogRecord.setSystemCpuUsage(systemCpuUsage);
		systemCpuStatsLogRecord.setTimeCaptured(timeCaptured);
		systemCpuStatsLogRecord.setPeriodicTime(periodicTime);
		systemCpuStatsLogRecord.setMonitoredObjectClass(monitoredObjectClass);
		systemCpuStatsLogRecord.setMonitoredObjectPointer(monitoredObjectPointer);
		systemCpuStatsLogRecord.setDisplayedName(displayedName);
		systemCpuStatsLogRecord.setMonitoredObjectSiteId(monitoredObjectSiteId);
		systemCpuStatsLogRecord.setMonitoredObjectSiteName(monitoredObjectSiteName);
		systemCpuStatsLogRecord.setSuspect(suspect);

		return systemCpuStatsLogRecord;
	}

}
