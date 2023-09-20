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
//		try {
//			Object x = "".getClass().forName("java.util.Date").getConstructor().newInstance();
//			
//			System.out.println(x);
//		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		
		String str1="aaa_jxel[zzzz]bbbb   _jxel[tttt]yyy  _jxel[zzzz]  "
				+ " now= _jxel[_fd.format(now)]   "
				+ " now minus = _jxel[_fd.format(new('java.util.Date', now.getTime()-300000))]"
				+ " current time =_jxel[_fd.format(_fd.currentTime())]"
		        + " currentTime minus = _jxel[_fd.format(_fd.pastTime(300000))]";
		//String str1="aaa_jxel[zzzz]bbbb   _jxel[tttt]yyy  _jxel[zzzz]   _jxel[now.toString()]";
		
		
		String result = IoTCollector.jxelFunctionSubstitution(str1, parameters);
		LOG.debug("\n expression:"+str1+"\n result:"+result+ "\n parameters:"+parameters);
	}

}
