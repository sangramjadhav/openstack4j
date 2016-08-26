package org.openstack4j.osgi;

import org.openstack4j.core.transport.HttpExecutorService;

import java.util.HashSet;
import java.util.Set;

/**
 * Connector Registry to track connectors available in OSGi environment.
 */
public class ConnectorRegistry {

    private static final Set<HttpExecutorService> connectors = new HashSet<>();

    public static void addConnector(final HttpExecutorService executorService){
        connectors.add(executorService);
    }

    public static void removeConnector(final HttpExecutorService executorService){
        connectors.remove(executorService);
    }

	public static Iterable<HttpExecutorService> fromRegistry() {
      return connectors;
	}

    public static void clear(){
        connectors.clear();
    }
}
