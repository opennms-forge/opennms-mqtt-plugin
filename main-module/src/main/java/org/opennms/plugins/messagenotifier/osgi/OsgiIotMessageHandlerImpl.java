package org.opennms.plugins.messagenotifier.osgi;


import org.opennms.plugins.messagenotifier.MessageNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsgiIotMessageHandlerImpl implements  OsgiIotMessageHandler {
    private static final Logger LOG = LoggerFactory.getLogger(OsgiIotMessageHandlerImpl.class);

	@Override
	public void messageArrived(MessageNotification messageNotification) {
		LOG.info("messageArrived: QOS:"+ messageNotification.getQos()+" topic: "+messageNotification.getTopic()
				+ " payload bytes length "+ ( (messageNotification.getPayload()==null) ? null : messageNotification.getPayload().length ) );
		
	}

}
