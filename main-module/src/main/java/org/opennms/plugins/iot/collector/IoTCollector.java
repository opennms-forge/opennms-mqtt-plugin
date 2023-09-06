/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.plugins.iot.collector;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;
import java.util.Base64;

import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.TrustManager;
import javax.net.ssl.SSLContext;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.opennms.integration.api.v1.collectors.CollectionRequest;
import org.opennms.integration.api.v1.collectors.CollectionSet;
import org.opennms.integration.api.v1.collectors.ServiceCollector;
import org.opennms.integration.api.v1.collectors.immutables.ImmutableNumericAttribute;
import org.opennms.integration.api.v1.collectors.immutables.ImmutableStringAttribute;
import org.opennms.integration.api.v1.collectors.resource.CollectionSetResource;
import org.opennms.integration.api.v1.collectors.resource.IpInterfaceResource;
import org.opennms.integration.api.v1.collectors.resource.NodeResource;
import org.opennms.integration.api.v1.collectors.resource.NumericAttribute;
import org.opennms.integration.api.v1.collectors.resource.Resource;
import org.opennms.integration.api.v1.collectors.resource.StringAttribute;
import org.opennms.integration.api.v1.collectors.resource.immutables.ImmutableCollectionSet;
import org.opennms.integration.api.v1.collectors.resource.immutables.ImmutableCollectionSetResource;
import org.opennms.integration.api.v1.collectors.resource.immutables.ImmutableIpInterfaceResource;
import org.opennms.integration.api.v1.collectors.resource.immutables.ImmutableNodeResource;
import org.opennms.integration.api.v1.runtime.RuntimeInfo;
import org.opennms.plugins.messagenotifier.MessageNotification;
import org.opennms.plugins.messagenotifier.osgi.OsgiIotMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoTCollector implements ServiceCollector {

	private static final Logger LOG = LoggerFactory.getLogger(IoTCollector.class);

	private static final String INSTANCE_NAME = "opennms";
	private static final String LOCATION_KEY = "location";
	public static final String MAGIC_NUMBER_PARM = "magicNumber";

	private final RuntimeInfo runtimeInfo;

	private OsgiIotMessageHandler osgiIotMessageHandlerservice;

	public IoTCollector(RuntimeInfo runtimeInfo) {
		LOG.debug("constructor called IotCollector.");
		this.runtimeInfo = Objects.requireNonNull(runtimeInfo);
	}

	public OsgiIotMessageHandler getOsgiIotMessageHandlerservice() {
		return osgiIotMessageHandlerservice;
	}

	public void setOsgiIotMessageHandlerservice(OsgiIotMessageHandler osgiIotMessageHandlerservice) {
		this.osgiIotMessageHandlerservice = osgiIotMessageHandlerservice;
	}

	@Override
	public void initialize() {
		LOG.debug("initialize: Initializing IotCollector.");
		// pass
	}
	
	public static final int DEFAULT_CONNECTION_TIMEOUT = 1000; // 1000ms = 1s
	public static final int DEFAULT_READ_TIMEOUT = 1000;
	public static final int DEFAULT_QOS = 0;

	public static final String COLLECTION_URL_KEY = "collectionUrl";
	public static final String CONNECTION_TIMEOUT_KEY = "connectionTimeout";
	public static final String READ_TIMEOUT_KEY = "readTimeout";

	public static final String COLLECTION_TOPIC_KEY = "collectionTopic";
	public static final String COLLECTION_QOS_KEY = "collectionQos";
	
	public static final String HTTP_REQUEST_METHOD_KEY = "requestMethod";
	public static final String IGNORE_HOST_CERTIFICATE_KEY = "ignoreHostCertificate";
	public static final String HTTP_ACCEPT_KEY = "acceptType";
	public static final String HTTP_CONTENT_TYPE_KEY = "contentType";
	public static final String HTTP_REQUEST_BODY_KEY = "requestBody";
	
	public static final String HTTP_BEARER_AUTHORISATION_KEY = "bearerAuthorisation";
	public static final String HTTP_USERNAME_KEY = "username";
	public static final String HTTP_PASSWORD_KEY = "password";

	@Override
	public CompletableFuture<CollectionSet> collect(CollectionRequest agent, Map<String, Object> parameters) {
		LOG.debug("collect called IotCollector.");
		final CompletableFuture<CollectionSet> future = new CompletableFuture<>();

		// actual monitoring is done through the Mqtt plugin using the topics specified

		String address = agent.getAddress().getHostAddress();
		int id = agent.getNodeId();

		StringBuffer sb = new StringBuffer("agent node address: " + address + " node id=" + id + " parameters:\n");
		for (String key : parameters.keySet()) {
			Object o = parameters.get(key);
			sb.append("  key: " + key + " value: " + o.toString() + "\n");
		}
		LOG.info("collection parameters: " + sb.toString());

		try {

			String urlStr = (String) parameters.get(COLLECTION_URL_KEY);
			if (urlStr == null)
				throw new IllegalArgumentException("collection url key " + COLLECTION_URL_KEY + " not set");

			int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
			int readTimeout = DEFAULT_READ_TIMEOUT;
			int qos = DEFAULT_QOS;
			
			String requestMethod=(String) parameters.get(HTTP_REQUEST_METHOD_KEY);
			String requestBodyString = (String)parameters.get(HTTP_REQUEST_BODY_KEY);
			String contentType=(String)parameters.get(HTTP_CONTENT_TYPE_KEY);
			String acceptType=(String) parameters.get(HTTP_ACCEPT_KEY);
			
			String bearerAuthorisation = (String) parameters.get(HTTP_BEARER_AUTHORISATION_KEY);
			String username = (String) parameters.get(HTTP_USERNAME_KEY);
			String password = (String) parameters.get(HTTP_PASSWORD_KEY);
			
			
			boolean ignoreHostCertificate = false;
			if (parameters.get(IGNORE_HOST_CERTIFICATE_KEY) != null) {
				ignoreHostCertificate = Boolean.valueOf((String) parameters.get(IGNORE_HOST_CERTIFICATE_KEY));
			}
			
			if (parameters.get(CONNECTION_TIMEOUT_KEY) != null) {
				connectionTimeout = Integer.parseInt((String) parameters.get(CONNECTION_TIMEOUT_KEY));
			}

			if (parameters.get(READ_TIMEOUT_KEY) != null) {
				readTimeout = Integer.parseInt((String) parameters.get(READ_TIMEOUT_KEY));
			}

			if (parameters.get(COLLECTION_QOS_KEY) != null) {
				qos = Integer.parseInt((String) parameters.get(COLLECTION_QOS_KEY));
			}

			
			byte[] messagebytes = null;
			URL url = new URL(urlStr);
			URLConnection connection = null;

			// check protocols
			if ("ftp".equals(url.getProtocol())) {
				// experimental ftp connection - may only work with sun jdk
				connection = url.openConnection();
				
			} else if ("https".equals(url.getProtocol()) || "http".equals(url.getProtocol())) {
				HttpURLConnection httpcon = null;

				if ("https".equals(url.getProtocol())) {

					HttpsURLConnection httpscon = (HttpsURLConnection) url.openConnection();
					
					if(ignoreHostCertificate) {
					// Install the all-trusting trust manager
					SSLContext sc = SSLContext.getInstance("SSL");
					sc.init(null, trustAllCerts, new java.security.SecureRandom());
					httpscon.setSSLSocketFactory(sc.getSocketFactory());
					}

					httpcon = httpscon;

				} else {
					httpcon = (HttpURLConnection) url.openConnection();
				}

				httpcon.setDoInput(true);
				httpcon.setDoOutput(true);
				httpcon.setUseCaches(false);
				
				httpcon.setRequestMethod(requestMethod);

				httpcon.setRequestProperty("Content-Type", contentType ); 
				httpcon.setRequestProperty("Accept", acceptType);
				
				if(bearerAuthorisation!=null){
					httpcon.setRequestProperty("Authorization", "Bearer "+ bearerAuthorisation);
				} else if(username !=null) {
					String auth = username + ":" + password;
					byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
					httpcon.setRequestProperty("Authorization", "Basic "+ new String(encodedAuth));
				}
				
//				Host: management.azure.com
//				Content-Type: application/json
//				Authorization: Bearer <access token>
				//
				connection = httpcon;

			} else {
				throw new IllegalArgumentException("unsupported protocol " + url.getProtocol() + " in url " + urlStr);
			}

			connection.setConnectTimeout(connectionTimeout);
			connection.setReadTimeout(readTimeout);

			try (InputStream inputstream = connection.getInputStream();
					ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
					OutputStream outputstream = connection.getOutputStream()

			) {

				// send request
				byte[] requestbytes = requestBodyString.getBytes("utf-8");
				outputstream.write(requestbytes, 0, requestbytes.length);

				// receive response
				final byte[] buffer = new byte[8192];
				for (int count; (count = inputstream.read(buffer)) > 0;) {
					bytestream.write(buffer, 0, count);
				}
				messagebytes = bytestream.toByteArray();
			}

			String topic = (String) parameters.get(COLLECTION_TOPIC_KEY);
			if (topic == null)
				throw new IllegalArgumentException("IoT collection topic  " + COLLECTION_TOPIC_KEY + " not set");

			MessageNotification messageNotification = new MessageNotification(topic, qos, messagebytes);
			osgiIotMessageHandlerservice.messageArrived(messageNotification);

			LOG.info("Sample Collector collection Succeeded");

		} catch (Exception ex) {
			LOG.error("Sample Collector collection Failed ", ex);
		}

		double magicNumber = getKeyAsDouble(MAGIC_NUMBER_PARM, parameters, Double.NaN);
		future.complete(buildCollectionSet(agent.getNodeId(), magicNumber));

		return future;
	}

	public static boolean validateCollectionSet(CollectionSet collectionSet, int nodeId, double magicNumber,
			String location) {
		LOG.debug("validateCollectionSet called IotCollector.");
		// Grab the first resource
		final CollectionSetResource<IpInterfaceResource> resource = collectionSet.getCollectionSetResources().get(0);
		if (!Objects.equals(Resource.Type.INTERFACE, resource.getResource().getResourceType())) {
			return false;
		}
		final IpInterfaceResource ipInterfaceResource = resource.getResource();
		if (!Objects.equals(INSTANCE_NAME, ipInterfaceResource.getInstance())) {
			return false;
		}
		final NodeResource nodeResource = ipInterfaceResource.getNodeResource();
		if (nodeId != nodeResource.getNodeId()) {
			return false;
		}
		if (Math.abs(magicNumber - resource.getNumericAttributes().get(0).getValue()) > 0.00001d) {
			return false;
		}
		// Verify that the string attribute is present and matches the expected location
		// this tells us the collector was actually invoked on a Minion
		return resource.getStringAttributes().stream()
				.anyMatch(s -> LOCATION_KEY.equals(s.getName()) && location.equals(s.getValue()));
	}

	private CollectionSet buildCollectionSet(int nodeId, double magicNumber) {
		// Build collection set with a IpInterface resource.
		NodeResource nodeResource = ImmutableNodeResource.newBuilder().setNodeId(nodeId).build();
		IpInterfaceResource ipInterfaceResource = ImmutableIpInterfaceResource.newInstance(nodeResource, INSTANCE_NAME);
		// Add attribute
		NumericAttribute numeric = ImmutableNumericAttribute.newBuilder().setGroup("group").setName("snmp")
				.setValue(magicNumber).setType(NumericAttribute.Type.GAUGE).build();
		StringAttribute string = ImmutableStringAttribute.newBuilder().setName(LOCATION_KEY).setGroup("group")
				.setValue(runtimeInfo.getSystemLocation()).build();
		// Build collection set
		CollectionSetResource<IpInterfaceResource> collectionSetResource = ImmutableCollectionSetResource
				.newBuilder(IpInterfaceResource.class).setResource(ipInterfaceResource).addNumericAttribute(numeric)
				.addStringAttribute(string).build();
		return ImmutableCollectionSet.newBuilder().addCollectionSetResource(collectionSetResource)
				.setTimestamp(System.currentTimeMillis()).build();
	}

	public static class CollectionRequestImpl implements CollectionRequest {
		private final int nodeId;
		private final InetAddress address;

		public CollectionRequestImpl(int nodeId, InetAddress address) {
			this.nodeId = nodeId;
			this.address = address;
		}

		@Override
		public InetAddress getAddress() {
			return address;
		}

		@Override
		public int getNodeId() {
			return nodeId;
		}
	}

	private static double getKeyAsDouble(String key, Map<String, Object> map, double defaultValue) {
		Object val = map.get(key);
		if (val == null) {
			return defaultValue;
		}
		if (val instanceof Number) {
			return ((Number) val).doubleValue();
		}
		try {
			return Double.parseDouble(val.toString());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	// see
	// https://stackoverflow.com/questions/1201048/allowing-java-to-use-an-untrusted-certificate-for-ssl-https-connection
	// Create a trust manager that does not validate certificate chains
	TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		}

		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		}
	} };

}
