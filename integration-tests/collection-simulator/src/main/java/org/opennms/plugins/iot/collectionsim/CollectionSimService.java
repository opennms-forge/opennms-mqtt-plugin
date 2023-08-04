package org.opennms.plugins.iot.collectionsim;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.opennms.plugins.iot.collectionsim.model.LogToFileResponse;
import org.opennms.plugins.iot.collectionsim.model.SystemCpuStatsLogRecord;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.stereotype.Component;


@Component
public class CollectionSimService {

	public static final long POLLING_INTERVAL = 1000 * 60; // 60 seconds

	public static long startTime = new Date().getTime();

	public static AtomicLong timeLastPoll = new AtomicLong(startTime);

	public LogToFileResponse getResponse(@PathVariable("count") Integer n) {

		LogToFileResponse logToFileResponse = new LogToFileResponse();

		long currentTime = new Date().getTime();

		n = (n != null) ? n : 5;

		for (int i = 1; i < n + 1; i++) {
			long sampleTime = currentTime - POLLING_INTERVAL * i;
			long timeinterval = currentTime - sampleTime;
			SystemCpuStatsLogRecord record = templateSystemCpuStatsLogRecord();
			record.setTimeCaptured(sampleTime);
			record.setPeriodicTime(timeinterval);

			logToFileResponse.getLogToFileResponse().add(record);
		}

		timeLastPoll.set(currentTime);

		return logToFileResponse;
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
