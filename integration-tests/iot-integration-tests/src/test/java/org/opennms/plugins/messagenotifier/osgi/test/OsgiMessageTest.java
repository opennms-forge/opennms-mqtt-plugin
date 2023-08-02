/*
 *    Copyright 2023 Craig Gallen, OpenNMS Group
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.opennms.plugins.messagenotifier.osgi.test;

import org.opennms.plugins.messagenotifier.osgi.OsgiIotMessageHandler;
import org.opennms.plugins.messagenotifier.MessageNotification;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import static org.opennms.paxexam.container.OpenNMSRBCRemoteTargetOptions.location;
import static org.opennms.paxexam.container.OpenNMSRBCRemoteTargetOptions.waitForRBCFor;

import java.net.URLConnection;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import javax.inject.Inject;

import org.apache.karaf.features.FeaturesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PaxExam.class)
@ExamFactory(org.opennms.paxexam.container.OpenNMSPluginTestContainerFactory.class)

@ExamReactorStrategy(PerClass.class)

public class OsgiMessageTest {
	private static Logger LOG = LoggerFactory.getLogger(OsgiMessageTest.class);

	@Inject
	private BundleContext bc;

	@Inject
	protected OsgiIotMessageHandler osgiIotMessageHandlerservice;

	@Configuration
	public Option[] config() {
		return new Option[] { location("localhost", 55555), waitForRBCFor(10000) };
	}

	@Test
	public void osgiMessageTest() throws IOException {
		LOG.info("**** checking we have bundle context and iotMessageHandler ");
		assertThat(bc, is(notNullValue()));
		assertThat(osgiIotMessageHandlerservice, is(notNullValue()));

		String topic = "testTopic";
		int qos = 1;
		String urlStr = "file:///usr/share/opennms/share/misc/xmlFiles/nokia_oly-mpls0-cpeData.xml";
		byte[] messagebytes = copyURLToByteArray(urlStr, 100, 100);
		MessageNotification messageNotification = new MessageNotification(topic, qos, messagebytes);
		osgiIotMessageHandlerservice.messageArrived(messageNotification);

		LOG.info("**** finished test ");
	}

	public byte[] copyURLToByteArray(final String urlStr, final int connectionTimeout, final int readTimeout)
			throws IOException {
		final URL url = new URL(urlStr);
		final URLConnection connection = url.openConnection();
		connection.setConnectTimeout(connectionTimeout);
		connection.setReadTimeout(readTimeout);
		try (InputStream input = connection.getInputStream();
				ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			final byte[] buffer = new byte[8192];
			for (int count; (count = input.read(buffer)) > 0;) {
				output.write(buffer, 0, count);
			}
			return output.toByteArray();
		}
	}

}
