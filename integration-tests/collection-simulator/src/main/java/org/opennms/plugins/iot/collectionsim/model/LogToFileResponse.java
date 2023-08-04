package org.opennms.plugins.iot.collectionsim.model;

import java.util.List;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElement;

/*
 * <logToFileResponse>
   <equipment.SystemCpuStatsLogRecord>
      <systemCpuUsage>9</systemCpuUsage>
      <timeCaptured>1689362117555</timeCaptured>
      <periodicTime>70597</periodicTime>
      <monitoredObjectClass>equipment.SystemStatsHolder</monitoredObjectClass>
      <monitoredObjectPointer>network:64.184.178.239:shelf-1:systemStatsHolder</monitoredObjectPointer>
      <displayedName>systemStatsHolder</displayedName>
      <monitoredObjectSiteId>64.184.178.239</monitoredObjectSiteId>
      <monitoredObjectSiteName>oly-mpls0</monitoredObjectSiteName>
      <suspect>false</suspect>
   </equipment.SystemCpuStatsLogRecord>
   <logToFileResponse>
 */

@XmlRootElement()
@XmlAccessorType(XmlAccessType.PROPERTY)
public class LogToFileResponse {


	public List<SystemCpuStatsLogRecord> logToFileResponse = new ArrayList();

	//@XmlElementWrapper()
    @XmlElement(name="equipment.SystemCpuStatsLogRecord")
	public List<SystemCpuStatsLogRecord> getLogToFileResponse() {
		return logToFileResponse;
	}

	public void setLogToFileResponse(List<SystemCpuStatsLogRecord> logToFileResponse) {
		this.logToFileResponse = logToFileResponse;
	}

}