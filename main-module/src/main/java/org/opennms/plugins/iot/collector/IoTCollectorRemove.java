package org.opennms.plugins.iot.collector;

import java.util.Map;
import java.util.HashMap;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoTCollectorRemove implements ServiceCollector {
	private static final Logger LOG = LoggerFactory.getLogger(IoTCollectorRemove.class);

	@Override
	public void initialize() throws CollectionInitializationException {
		LOG.debug("initialize: Initializing IotCollector.");

	}

	@Override
	public void validateAgent(CollectionAgent agent, Map<String, Object> parameters)
			throws CollectionInitializationException {
		LOG.debug("validate agent called IotCollector.");

	}

	@Override
	public CollectionSet collect(CollectionAgent agent, Map<String, Object> parameters) throws CollectionException {
		CollectionSetBuilder builder = new CollectionSetBuilder(agent);
		builder.withStatus(CollectionStatus.FAILED);

		LOG.debug("No groups to collect. - collecting from file. IoTcollector");
		builder.withStatus(CollectionStatus.SUCCEEDED);
		return builder.build();

	}

	@Override
	public RrdRepository getRrdRepository(String collectionName) {
		LOG.debug("getRRdRepository called  IoTcollector");
		return null;
	}

	@Override
	public Map<String, Object> getRuntimeAttributes(CollectionAgent agent, Map<String, Object> parameters) {
		LOG.debug("getRuntimeAttributes called  IoTcollector");
		return new HashMap<String,Object>();
	}

	@Override
	public String getEffectiveLocation(String location) {
		LOG.debug("getEffectiveLocaton called  IoTcollector location="+location);
		return location;
	}

	@Override
	public Map<String, String> marshalParameters(Map<String, Object> parameters) {
		LOG.debug("marshalParameters called  IoTcollector");
		return new HashMap<String,String>();
	}

	@Override
	public Map<String, Object> unmarshalParameters(Map<String, String> parameters) {
		LOG.debug("unmarshalParameters called  IoTcollector");
		return new HashMap<String,Object>();
	}

}
