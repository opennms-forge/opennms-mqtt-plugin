package org.opennms.plugins.iot.collector;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

// removes ambiguity from date formatter format and parse
public class FormatDate {
	
	DateFormat df;
	
    public FormatDate(String dateFormatStr) {
    	super();
    	 df = new SimpleDateFormat(dateFormatStr);
	}

	public String format(Date date) {
		return df.format(date);
	}
	
	public Date parse(String dateStr) throws ParseException {
		return df.parse(dateStr);
	}
	
	public Date currentTime() {
		return new Date();
	}
	
	public Date currentTime(long timeMs) {
		return new Date(timeMs);
	}
	
	public Date pastTime(long lessMilliSeconds) {
		long ms = new Date().getTime();
		return new Date(ms - lessMilliSeconds);
	}

}
