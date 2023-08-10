package org.opennms.plugins.iot.collectionsim;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Random;

import java.util.concurrent.atomic.AtomicLong;

import org.opennms.plugins.iot.collectionsim.model.LogToFileResponse;
import org.opennms.plugins.iot.collectionsim.model.SystemCpuStatsLogRecord;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.stereotype.Component;

@Component
public class CollectionSimService {

	public static final int DEFAULT_POLLING_INTERVAL = 1000 * 60; // 60 seconds
	public static final int DEFAULT_COUNT = 5; // 60 seconds
	
	private static long startTime = new Date().getTime();

	private static AtomicLong timeLastPoll = new AtomicLong(startTime);

	public LogToFileResponse getResponse( Integer count, Integer interval) {

		LogToFileResponse logToFileResponse = new LogToFileResponse();

		long currentTime = new Date().getTime();

		count = (count != null) ? count : DEFAULT_COUNT;
		
		interval = (interval!=null) ?  interval : DEFAULT_POLLING_INTERVAL ;

		for (int i = count + 1; i > 0; i--) {
			long sampleTime = currentTime - interval * i;

			// round to minute
			Calendar calendar = new GregorianCalendar();
			calendar.setTimeInMillis(sampleTime);

			// round to nearest minute - 'add' cause changing larger fields if necessary
			calendar.add(Calendar.SECOND, 30);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			sampleTime = calendar.getTimeInMillis();

			long timeinterval = DEFAULT_POLLING_INTERVAL;
			SystemCpuStatsLogRecord record = templateSystemCpuStatsLogRecord();
			record.setTimeCaptured(sampleTime);
			record.setPeriodicTime(timeinterval);
			
			Random rand = new Random();
			long cpu = 1000 * rand.nextInt(10);
			record.setSystemCpuUsage(cpu);

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
