/* ***************************************************************************
 * Copyright 2018 OpenNMS Group Inc, Entimoss Ltd. Or their affiliates.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ****************************************************************************/

package org.opennms.plugins.messagenotifier.mqttclient;

import java.security.KeyStore;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLSocketFactory;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.opennms.plugins.messagenotifier.MessageNotification;
import org.opennms.plugins.messagenotifier.MessageNotificationClient;
import org.opennms.plugins.messagenotifier.MessageNotifier;
import org.opennms.plugins.messagenotifier.mqttclient.MQTTTopicSubscription;
import org.opennms.plugins.messagenotifier.mqttclient.MQTTClientConfig;
import org.opennms.plugins.mqtt.utils.AwsIotTlsSocketFactory;
import org.opennms.plugins.mqtt.utils.IotConnectionException;
import org.opennms.plugins.mqtt.utils.SampleUtil;
import org.opennms.plugins.mqtt.utils.SampleUtil.KeyStorePasswordPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQTTClientImpl implements MqttCallback, MessageNotifier {
	private static final Logger LOG = LoggerFactory.getLogger(MQTTClientImpl.class);

	// maximum time to wait for connection
	private long m_clientConnectionMaxWait = 30000; // 30 s wait to connect or to disconnect

	// m_connectionRetryInterval   interval (ms) before re attempting connection.
	private Integer m_connectionRetryInterval=40000; // 40 seconds
	private String m_brokerUrl;
	private String m_password;
	private String m_userName;

	private String m_instanceId = "instanceId_Not_Set";

	private AtomicInteger reconnectionCount = new AtomicInteger(0);

	// Private instance variables
	private MqttAsyncClient 	m_client;
	private MqttConnectOptions 	m_conOpt;
	private boolean clean=true;

	private AtomicBoolean m_clientConnected = new AtomicBoolean(false); 

	private Thread m_connectionRetryThread=null;

	private Set<MessageNotificationClient> messageNotificationClientList = Collections.synchronizedSet(new HashSet<MessageNotificationClient>());

	private Set<MQTTTopicSubscription> topicList = Collections.synchronizedSet(new HashSet<MQTTTopicSubscription>());

	/**
	 * adds new MessageNotificationClient to list of clients which will be sent database notifications
	 * @param MessageNotificationClient
	 */
	@Override
	public void addMessageNotificationClient(MessageNotificationClient messageNotificationClient){
		LOG.debug("client instanceId:"+m_instanceId+": adding messageNotificationClient:"+messageNotificationClient.toString());
		messageNotificationClientList.add(messageNotificationClient);
	}

	/**
	 * removes messageNotificationClient from list of clients which will be sent database notifications
	 * @param messageNotificationClient
	 */
	@Override
	public void removeMessageNotificationClient(MessageNotificationClient messageNotificationClient){
		LOG.debug("client instanceId:"+m_instanceId+": removing messageNotificationClient:"+messageNotificationClient.toString());
		messageNotificationClientList.remove(messageNotificationClient);
	}

	/**
	 * adds a list of subscriptions which will be subscribed when the class connects to the broker
	 * @param topicList
	 */
	public void setTopicList(Set<MQTTTopicSubscription> topicList){
		this.topicList.addAll(topicList);
	}

	public boolean isClientConnected() {
		return m_clientConnected.get();
	}



	/**
	 * @param brokerUrl the url to connect to
	 * @param clientId the m_client id to connect with
	 * @param userName the username to connect with
	 * @param password the password for the user
	 * @param connectionRetryInterval interval (ms) before re attempting connection.
	 * @param certificateFile fully qualified path to certificate.pem file if null TLS connection is not attempted
	 * @param privateKeyFile fully qualified path to private.key file if null TLS connection is not attempted
	 */
	public MQTTClientImpl(String brokerUrl, String clientId, String userName, String password, String connectionRetryInterval, String clientConnectionMaxWait, String certificateFile, String privateKeyFile) {
		if ((brokerUrl == null) || (brokerUrl.trim().equals(""))) throw new IllegalArgumentException("mqtt m_brokerUrl cannot be empty or null");

		SSLSocketFactory sslSocketFactory=null; // socketFactory will be null if TLS socket not defined
		
		try{
			this.m_connectionRetryInterval=Integer.parseInt(connectionRetryInterval);
		} catch (Exception e){
			throw new IllegalArgumentException("mqtt m_connectionRetryInterval "+connectionRetryInterval+" is not an integer value",e);
		}

		try{
			this.m_clientConnectionMaxWait=Long.parseLong(clientConnectionMaxWait);
		} catch (Exception e){
			throw new IllegalArgumentException("mqtt m_clientConnectionMaxWait  "+clientConnectionMaxWait+" is not an long value",e);
		}

		m_brokerUrl = brokerUrl;
		m_userName = userName;
		m_password = password;

		if ((certificateFile != null && privateKeyFile== null) ||(certificateFile == null && privateKeyFile!= null) ) {
			throw new IllegalArgumentException("if using TLS you must define both certificateFile ("
					+ certificateFile +") and privateKeyFile ("+privateKeyFile + ")");
		}

		if (certificateFile != null){
			try {
				String algorithm = null;
				KeyStorePasswordPair pair = SampleUtil.getKeyStorePasswordPair(certificateFile, privateKeyFile, algorithm);
				String keyPassword = pair.keyPassword;
				KeyStore keyStore = pair.keyStore;
		        sslSocketFactory = new AwsIotTlsSocketFactory(keyStore, keyPassword);
			} catch (IotConnectionException e) {
				throw new IllegalArgumentException("cannot create socket from TLS certificateFile ("
						+ certificateFile +") and privateKeyFile ("+privateKeyFile + ")",e);
			}
		}

		MemoryPersistence persistence = new MemoryPersistence();

		try {
			// Construct the connection options object that contains connection parameters
			// such as cleanSession and LWT
			m_conOpt = new MqttConnectOptions();
			m_conOpt.setCleanSession(clean);
			if(m_userName != null && ! m_userName.trim().equals("") ) {
				m_conOpt.setUserName(m_userName);
			}
			if(m_password!= null && ! m_password.trim().equals("")) {
				m_conOpt.setPassword(m_password.toCharArray());
			}
			
			if (sslSocketFactory!=null){
				LOG.warn("client instanceId:"+m_instanceId+": using TLS certificateFile ("
					+ certificateFile +") and privateKeyFile ("+privateKeyFile + ")");
				m_conOpt.setSocketFactory(sslSocketFactory);
			}

			// Construct a non-blocking MQTT m_client instance
			m_client = new MqttAsyncClient(this.m_brokerUrl,clientId, persistence);

			// Set this wrapper as the callback handler
			m_client.setCallback(this);

		} catch (MqttException e) {
			LOG.error("client instanceId:"+m_instanceId+": Unable to set up MQTT m_client",e);
			throw new RuntimeException("client instanceId:"+m_instanceId+": Unable to set up MQTT m_client",e);
		}
	}

	/**
	 * constructor using MQTTClientConfig
	 * @param mQTTClientConfig
	 */
	public MQTTClientImpl(MQTTClientConfig mQTTClientConfig){
		this(mQTTClientConfig.getBrokerUrl(), 
				mQTTClientConfig.getClientId(), 
				mQTTClientConfig.getUserName(), 
				mQTTClientConfig.getPassword(), 
				mQTTClientConfig.getConnectionRetryInterval(),
				mQTTClientConfig.getClientConnectionMaxWait(), null, null);

		m_instanceId = mQTTClientConfig.getClientInstanceId();
		setTopicList(mQTTClientConfig.getTopicList());

	}

	/**
	 * Connect to the MQTT server
	 * issue a non-blocking connect and then use the token to wait until the
	 * connect completes.
	 * @return true if connected, false if not
	 */
	public synchronized boolean connect(){
		if (m_client.isConnected()) {
			m_clientConnected.set(true);
			return true;
		}
		LOG.warn("client instanceId:"+m_instanceId+": Connecting to "+m_brokerUrl + " with m_client ID "+m_client.getClientId()
				+" (number of connection attempts since start="
				+ reconnectionCount.incrementAndGet()+")");
		IMqttToken conToken;
		try {
			conToken = m_client.connect(m_conOpt,null,null);
			conToken.waitForCompletion(m_clientConnectionMaxWait);
		} catch (MqttException e1) {
			// An exception is thrown if connect fails.
			LOG.error("client instanceId:"+m_instanceId+": failed to connect to MQTT broker:"+ m_brokerUrl ,e1);
			m_clientConnected.set(false);
			return false;
		}
		LOG.warn("client instanceId:"+m_instanceId+": Connected to MQTT broker");
		m_clientConnected.set(true);
		return true;
	}

	/**
	 * Synchronously Publish / send a message to an MQTT server. Call returns once message has been sent
	 * @param topicName the name of the topic to publish to
	 * @param qos the quality of service to delivery the message at (0,1,2)
	 * @param payload the set of bytes to send to the MQTT server
	 */
	public void publishSynchronous(String topicName, int qos, byte[] payload) {
		if(! m_clientConnected.get()){
			if (LOG.isDebugEnabled()) LOG.debug("client instanceId:"+m_instanceId+": m_client disconnected. not publishing message");
			return; 
		}

		// Construct the message to send
		MqttMessage message = new MqttMessage(payload);
		message.setQos(qos);

		if (LOG.isDebugEnabled()){
			String time = new Timestamp(System.currentTimeMillis()).toString();
			LOG.debug("client instanceId:"+m_instanceId+": Publishing synchronous message at: "+time+ " to topic \""+topicName+"\" qos "+qos);
		}	
		// Send the message to the server, control is returned as soon
		// as the MQTT m_client has accepted to deliver the message.
		// Use the delivery token to wait until the message has been
		// delivered

		try {
			IMqttDeliveryToken pubToken = m_client.publish(topicName, message, null, null);
			pubToken.waitForCompletion();
		} catch (MqttException  e) {
			throw new RuntimeException("client instanceId:"+m_instanceId+"problem synchronously publishing message.",e);
		} 	
		LOG.debug("client instanceId:"+m_instanceId+": Published");

	}

	/**
	 * Asynchronously Publish / send a message to an MQTT server. Call returns as soon as message scheduled for sending
	 * @param topicName the name of the topic to publish to
	 * @param qos the quality of service to delivery the message at (0,1,2)
	 * @param payload the set of bytes to send to the MQTT server
	 */
	public void publishAsynchronous(String topicName, int qos, byte[] payload) {
		if(! m_clientConnected.get()){
			if (LOG.isDebugEnabled()) LOG.debug("client instanceId:"+m_instanceId+": m_client disconnected. not publishing message");
			return; 
		}

		if (LOG.isDebugEnabled()){
			String time = new Timestamp(System.currentTimeMillis()).toString();
			LOG.debug("client instanceId:"+m_instanceId+": Publishing asynchronous message at: "+time+ " to topic \""+topicName+"\" qos "+qos);
		}

		// Construct the message to send
		MqttMessage message = new MqttMessage(payload);
		message.setQos(qos);

		try {
			@SuppressWarnings("unused")
			IMqttDeliveryToken pubToken = m_client.publish(topicName, message, null, null);
		} catch (MqttException e) {
			throw new RuntimeException("client instanceId:"+m_instanceId+": problem synchronously publishing message",e);
		} 	
	}

	/**
	 * subscribe to topic - this is not persistent across disconnections
	 * @param topic
	 * @param qos
	 */
	public void subscribe(String topic, int qos){
		try {
			m_client.subscribe(topic,qos);
		} catch (MqttException e) {
			throw new RuntimeException("client instanceId:"+m_instanceId+": problem subscribing to topic:"+topic+ " qos "+qos,e);
		}
	}

	/** 
	 * destroy method
	 */
	public void destroy(){
		try {
			stopConnectionRetryThead();
			m_client.disconnectForcibly(m_clientConnectionMaxWait,m_clientConnectionMaxWait );
		} catch (MqttException e) {
			LOG.error("client instanceId:"+m_instanceId+": problem disconnecting m_client",e);
		} finally {
			try {
				// once closed a client can never be reused
				m_client.close();
			} catch (MqttException e) {
				LOG.error("client instanceId:"+m_instanceId+": problem closing m_client",e);
			}
		}
	}

	/**
	 * init method
	 */
	public void init(){
		startConnectionRetryThead();
	}


	/**
	 * Disconnect the m_client
	 * Issue the disconnect and then use a token to wait until
	 * the disconnect completes.
	 */
	public synchronized void disconnect(){
		LOG.debug("client instanceId:"+m_instanceId+": Disconnecting from MQTT broker");
		if(m_clientConnected.getAndSet(false)){
			try {
				IMqttToken discToken = m_client.disconnect(null, null);
				discToken.waitForCompletion();
			} catch (MqttException e1) {
				// An exception is thrown if disconnect fails.
				LOG.error("client instanceId:"+m_instanceId+": error when disconnecting from MQTT broker:",e1);
			}
		}
		LOG.debug("client instanceId:"+m_instanceId+": Disconnected from MQTT broker");
	}

	/**
	 * Starts trying to reconnect to the broker in a separate thread. 
	 * the retry interval sets how long between connection attempts the m_client waits
	 */
	private synchronized void startConnectionRetryThead(){

		if (m_connectionRetryThread==null){

			if(m_connectionRetryInterval==null) throw new RuntimeException("client instanceId:"+m_instanceId+": connectionretryInterval cannot be null");

			m_connectionRetryThread = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						while (!Thread.currentThread().isInterrupted()) {

							LOG.debug("client instanceId:"+m_instanceId+": trying to connect to Mqtt broker");
							boolean success = false;
							try{
								success = connect();
							} catch(Exception e){
								LOG.error("client instanceId:"+m_instanceId+": exception thrown when trying to start MQTT connection.",e);
								throw new InterruptedException();
							}
							if(success) {
								// if connected then try to subscribe to topics. Try all topics and log failures
								LOG.debug("client instanceId:"+m_instanceId+": connected to Mqtt broker. Trying to subscribe to pre-set topics "+topicList.size());
								for(MQTTTopicSubscription subscription:topicList){
									try{
										LOG.debug("client instanceId:"+m_instanceId+":  subscribing to topic:"+subscription.getTopic()+" qos:"+subscription.getQos());
										subscribe(subscription.getTopic(), Integer.parseInt(subscription.getQos()));
									} catch(Exception e){
										LOG.error("client instanceId:"+m_instanceId+": exception thrown when trying to subscribe to topic:"+subscription.getTopic()+" qos:"+subscription.getQos(),e);
									}
								}

								throw new InterruptedException();
							}
							LOG.debug("client instanceId:"+m_instanceId+": waiting "+m_connectionRetryInterval
									+ "ms before retrying to connect to Mqtt broker");
							Thread.sleep(m_connectionRetryInterval);
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					LOG.debug("client instanceId:"+m_instanceId+": connection retry thread complete.");
				}
			});

			m_connectionRetryThread.start();
			LOG.warn("client instanceId:"+m_instanceId+": connection retry thread started: retryInterval="+m_connectionRetryInterval);
		}
	}

	private synchronized void stopConnectionRetryThead(){
		LOG.warn("client instanceId:"+m_instanceId+": disconnecting and stopping connection retry thread");
		disconnect();
		if (m_connectionRetryThread!=null){
			m_connectionRetryThread.interrupt();
			m_connectionRetryThread=null;
			LOG.warn("client instanceId:"+m_instanceId+": connection retry thread stopped");
		}
	}


	/****************************************************************/
	/* Methods to implement the MqttCallback interface              */
	/****************************************************************/

	/**
	 * Called when the connection to the server has been lost. 
	 * An application may choose to implement reconnection
	 * logic at this point.
	 */
	@Override
	public void connectionLost(Throwable cause) {
		LOG.debug("client instanceId:"+m_instanceId+": Connection to " + m_brokerUrl + " lost! Cause:" + cause);
		stopConnectionRetryThead();
		startConnectionRetryThead();
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// not used

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws MqttException {
		// Called when a message arrives from the server that matches any
		// subscription made by the m_client
		if(LOG.isDebugEnabled()){
			String time = new Timestamp(System.currentTimeMillis()).toString();
			LOG.debug("client instanceId:"+m_instanceId+": Time:\t" +time +
					"  Topic:\t" + topic +
					"  Message:\t" + new String(message.getPayload()) +
					"  QoS:\t" + message.getQos());
		}

		byte[] payload = message.getPayload();
		int qos = message.getQos();

		MessageNotification dbn = new MessageNotification(topic, qos, payload);

		// send notifications to registered clients - note each m_client must return quickly
		synchronized(messageNotificationClientList) {
			Iterator<MessageNotificationClient> i = messageNotificationClientList.iterator(); // Must be in synchronized block
			while (i.hasNext()){
				try{
					i.next().sendMessageNotification(dbn);
				} catch (Exception e){
					LOG.error("client instanceId:"+m_instanceId+": Problem actioning message notification.",e);
				}
			}         
		}

	}


	/****************************************************************/
	/* End of Methods to implement the MqttCallback interface              */
	/****************************************************************/


}
