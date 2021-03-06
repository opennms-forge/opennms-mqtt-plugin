/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.plugins.messagenotifier.rest;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.opennms.plugins.messagenotifier.MessageNotification;
import org.opennms.plugins.messagenotifier.MessageNotificationClient;
import org.opennms.plugins.mqtt.config.MQTTTopicSubscriptionXml;
import org.opennms.plugins.mqtt.config.MessageDataParserConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttRxServiceImpl implements  MqttRxService {
	private static final Logger LOG = LoggerFactory.getLogger(MqttRxServiceImpl.class);

	private Set<MessageNotificationClient> messageNotificationClientList = Collections.synchronizedSet(new HashSet<MessageNotificationClient>());

	// topic, MQTTTopicSubscriptionXml
	private Map<String,MQTTTopicSubscriptionXml> topicMap = Collections.synchronizedMap(new HashMap<String,MQTTTopicSubscriptionXml>());

	// configuration properties name, value
	private Map<String,String> configuration = Collections.synchronizedMap(new HashMap<String,String>());

	private String serviceName="undefined";

	private String serviceType="undefined";	

	@Override
	public Set<MQTTTopicSubscriptionXml> getTopicList() {
		HashSet<MQTTTopicSubscriptionXml> topicValues = new HashSet<MQTTTopicSubscriptionXml>();
		topicValues.addAll(topicMap.values());
		return topicValues;
	}

	@Override
	public void setTopicList(Set<MQTTTopicSubscriptionXml> topicList) {
		for (MQTTTopicSubscriptionXml topicSubscription : topicList){
			topicMap.put(topicSubscription.getTopic(), topicSubscription);
		}
	}

	/**
	 * adds new MessageNotificationClient to list of clients which will be sent notifications
	 * @param MessageNotificationClient
	 */
	@Override
	public void addMessageNotificationClient(MessageNotificationClient messageNotificationClient){
		LOG.debug("adding messageNotificationClient:"+messageNotificationClient.toString());
		messageNotificationClientList.add(messageNotificationClient);
	}

	/**
	 * removes messageNotificationClient from list of clients which will be sent notifications
	 * @param messageNotificationClient
	 */
	@Override
	public void removeMessageNotificationClient(MessageNotificationClient messageNotificationClient){
		LOG.debug("removing messageNotificationClient:"+messageNotificationClient.toString());
		messageNotificationClientList.remove(messageNotificationClient);
	}


	/**
	 * Called when a message notification arrives
	 */
	@Override
	public void messageArrived(MessageNotification messageNotification) throws Exception {

		// if message not in matching log warning and throw exception
		
		// see if this topic should be processed
		// looks for direct match of incoming topic against topicFilter map
		// e.g. subscribed topic filter /a/b == received topic name /a/b
		// if no direct match, iterate and try and match each topicFilter
		// e.g. topicFilter "foo/+" matches received topic "foo/bar/baz"
		String topic= messageNotification.getTopic();
		boolean topicFound = topicMap.containsKey(topic);
		if (!topicFound){
			
			String topicFilter=null;
			Iterator<String> itr = topicMap.keySet().iterator();
			while(!topicFound && itr.hasNext()){
				topicFilter= itr.next();
				topicFound = MqttTopic.isMatched(topicFilter, topic);
			}
			if(LOG.isDebugEnabled()){
				if(topicFound) LOG.debug("matched incoming topic {} against message topicFilter {}", topic, topicFilter);
				else LOG.debug("no message topicFilter match found against incoming topic {}", topic);
			} 
		}

		if (! topicFound) {
			String time = new Timestamp(System.currentTimeMillis()).toString();
			String msg ="Message received from unknown topic. Time:\t" +time +
					"  Topic:\t" + messageNotification.getTopic() +
					"  Message:\t" + new String(messageNotification.getPayload()) +
					"  QoS:\t" + messageNotification.getQos();
			LOG.warn(msg);

		} else {
			// forward messages to receivers
			
			if(LOG.isDebugEnabled()) {
				String time = new Timestamp(System.currentTimeMillis()).toString();
				LOG.debug("Message received. Forwarding to "+messageNotificationClientList.size()+ " clients."
						+ " Time:\t" +time +
						"  Topic:\t" + messageNotification.getTopic() +
						"  Message:\t" + new String(messageNotification.getPayload()) +
						"  QoS:\t" + messageNotification.getQos());
			}

			// send notifications to registered clients - note each m_client must return quickly
			synchronized(messageNotificationClientList) {
				Iterator<MessageNotificationClient> i = messageNotificationClientList.iterator(); // Must be in synchronized block
				while (i.hasNext()){
					try{
						MessageNotificationClient mnclient=i.next();
						mnclient.sendMessageNotification(messageNotification);
					} catch (Exception e){
						LOG.error("Problem forwarding message notification.",e);
					}
				}         
			}
		}
	}

	@Override
	public void setClientInstanceId(String serviceName) {
		this.serviceName=serviceName;
	}

	@Override
	public String getClientInstanceId() {
		return serviceName;
	}

	@Override
	public void setClientType(String clientType) {
		this.serviceType=clientType;
	}

	@Override
	public String getClientType() {
		return serviceType;
	}

	@Override
	public void setConfiguration(Map<String, String> config) {
		configuration.clear();
		configuration.putAll(config);
	}

	@Override
	public Map<String, String> getConfiguration() {
		return configuration;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
}
