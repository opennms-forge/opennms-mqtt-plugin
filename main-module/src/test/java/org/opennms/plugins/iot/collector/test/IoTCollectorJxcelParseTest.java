package org.opennms.plugins.iot.collector.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opennms.plugins.iot.collector.IoTCollector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class IoTCollectorJxcelParseTest {
	private static final Logger LOG = LoggerFactory.getLogger(IoTCollectorJxcelParseTest.class);


	@Test
	public void test() {
		Map<String,Object> parameters = new HashMap<String,Object>();
		parameters.put("tttt", "2222");
		parameters.put("zzzz", "3333");
		parameters.put("now", new Date());
		
		Date d = new Date();
		d.getTime();
		
		parameters.put("dateFormat", "yyyy-MM-dd'T'HH:mm:ss'Z'");
		
		String str1="aaa_jxel[zzzz]bbbb   _jxel[tttt]yyy  _jxel[zzzz]  "
				+ " now= _jxel[_dateFunctions.format(now)]   "
				+ " now minus = _jxel[_dateFunctions.format(new('java.util.Date', now.getTime()-300000))]"
				+ " current time =_jxel[_dateFunctions.format(_dateFunctions.currentTime())]"
		        + " currentTime minus = _jxel[_dateFunctions.format(_dateFunctions.pastTime(300000))]";
		
		String result = IoTCollector.jxelFunctionSubstitution(str1, parameters);
		LOG.debug("\n expression:"+str1+"\n result:"+result+ "\n parameters:"+parameters);
	}

	
	@Test
	public void test2() {
		Map<String,Object> parameters = new HashMap<String,Object>();
		
		parameters.put("dateFormat", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		parameters.put("subscriptionId", "12345678-abcd-98765432-abcdef012345");
		
		String testStr="https://management.azure.com/subscriptions/12345678-abcd-98765432-abcdef012345/providers/microsoft.Insights/metrics?timespan=2023-06-25T22:20:00.000Z/2023-06-26T22:25:00.000Z&interval=PT5M&metricnames=Percentage CPU&aggregation=average&api-version=2021-05-01&region=eastus&metricNamespace=microsoft.compute/virtualmachines&$filter=Microsoft.ResourceId eq '*'";

		String str1= "https://management.azure.com/subscriptions/"
				+ "_jxel[subscriptionId]"
				+ "/providers/microsoft.Insights/metrics"
				+ "?timespan=_jxel[_dateFunctions.format(_dateFunctions.pastTime(300000))]/_jxel[_dateFunctions.format(_dateFunctions.currentTime())]"
				+ "&interval=PT5M"
				+ "&metricnames=Percentage CPU"
				+ "&aggregation=average&api-version=2021-05-01"
				+ "&region=eastus"
				+ "&metricNamespace=microsoft.compute/virtualmachines&$filter=Microsoft.ResourceId eq '*'";
		
		String result = IoTCollector.jxelFunctionSubstitution(str1, parameters);

		LOG.debug("\n test string:"+testStr+"\n      result:"+result+"\n expression:"+str1+ "\n parameters:"+parameters);
		//assertTrue(testStr.equals(result));
	}
}
