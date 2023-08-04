package org.opennms.plugins.iot.collectionsim.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.Date;

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

@XmlRootElement(name = "equipment.SystemCpuStatsLogRecord")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class SystemCpuStatsLogRecord {

	private long systemCpuUsage = 0;
	private long timeCaptured = 0;
	private long periodicTime = 0;
	private String monitoredObjectClass = "equipment.SystemStatsHolder";
	private String monitoredObjectPointer = "network:64.184.178.239:shelf-1:systemStatsHolder";
	private String displayedName = "systemStatsHolder";
	private String monitoredObjectSiteId = "64.184.178.239";
	private String monitoredObjectSiteName = "oly-mpls0";
	private boolean suspect = false;

	public long getSystemCpuUsage() {
		return systemCpuUsage;
	}

	public void setSystemCpuUsage(long systemCpuUsage) {
		this.systemCpuUsage = systemCpuUsage;
	}

	public long getTimeCaptured() {
		return timeCaptured;
	}

	public void setTimeCaptured(long timeCaptured) {
		this.timeCaptured = timeCaptured;
	}

	public long getPeriodicTime() {
		return periodicTime;
	}

	public void setPeriodicTime(long periodicTime) {
		this.periodicTime = periodicTime;
	}

	public String getMonitoredObjectClass() {
		return monitoredObjectClass;
	}

	public void setMonitoredObjectClass(String monitoredObjectClass) {
		this.monitoredObjectClass = monitoredObjectClass;
	}

	public String getMonitoredObjectPointer() {
		return monitoredObjectPointer;
	}

	public void setMonitoredObjectPointer(String monitoredObjectPointer) {
		this.monitoredObjectPointer = monitoredObjectPointer;
	}

	public String getDisplayedName() {
		return displayedName;
	}

	public void setDisplayedName(String displayedName) {
		this.displayedName = displayedName;
	}

	public String getMonitoredObjectSiteId() {
		return monitoredObjectSiteId;
	}

	public void setMonitoredObjectSiteId(String monitoredObjectSiteId) {
		this.monitoredObjectSiteId = monitoredObjectSiteId;
	}

	public String getMonitoredObjectSiteName() {
		return monitoredObjectSiteName;
	}

	public void setMonitoredObjectSiteName(String monitoredObjectSiteName) {
		this.monitoredObjectSiteName = monitoredObjectSiteName;
	}

	public boolean isSuspect() {
		return suspect;
	}

	public void setSuspect(boolean suspect) {
		this.suspect = suspect;
	}

}
