package org.opennms.plugins.messagenotifier.osgi;

import org.opennms.plugins.messagenotifier.MessageNotification;

public interface OsgiIotMessageHandler {
	
	public void messageArrived(MessageNotification messageNotification) throws Exception ;

}
