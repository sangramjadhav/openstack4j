package org.openstack4j.osgi;

import static org.openstack4j.osgi.Bundles.instantiateAvailableClasses;
import static org.openstack4j.osgi.Bundles.stringsForResourceInBundle;
import static org.osgi.framework.BundleEvent.STARTED;
import static org.osgi.framework.BundleEvent.STOPPED;
import static org.osgi.framework.BundleEvent.STOPPING;

import org.openstack4j.core.transport.HttpExecutorService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * A {@link BundleListener} that listens for {@link BundleEvent}
 * This is used as a workaround for OSGi environments where the ServiceLoader cannot cross bundle boundaries.
 */
public class MetadataBundleListener implements BundleListener {

    private final Multimap<Long, HttpExecutorService> connectorsMap = ArrayListMultimap.create();

    /**
     * Starts the listener. Checks the bundles that are already active and registers  and
     * found. Registers the itself as a {@link BundleListener}.
     *
     * @param bundleContext
     */
    public synchronized void start(BundleContext bundleContext) {
        bundleContext.addBundleListener(this);
        for (Bundle bundle : bundleContext.getBundles()) {
            if (bundle.getState() == Bundle.ACTIVE) {
                addBundle(bundle);
            }
        }
        bundleContext.addBundleListener(this);
    }

    /**
     * Stops the listener. Removes itself from the {@link BundleListener}s. Clears metadata maps and listeners lists.
     *
     * @param bundleContext
     */
    public void stop(BundleContext bundleContext) {
        bundleContext.removeBundleListener(this);
        connectorsMap.clear();
    }

    @Override
    public synchronized void bundleChanged(BundleEvent event) {
        switch (event.getType()) {
            case STARTED:
                addBundle(event.getBundle());
                break;
            case STOPPING:
            case STOPPED:
                removeBundle(event.getBundle());
                break;
        }
    }

    /**
     *
     * @param bundle
     */
    private synchronized void addBundle(Bundle bundle) {
        for (HttpExecutorService executorService : listConnectors(bundle)) {
            if (executorService != null) {
                ConnectorRegistry.addConnector(executorService);
                connectorsMap.put(bundle.getBundleId(), executorService);
            }
        }
    }

    private synchronized void removeBundle(Bundle bundle) {
        for (HttpExecutorService executorService : connectorsMap.removeAll(bundle.getBundleId())) {
            ConnectorRegistry.removeConnector(executorService);
        }
    }

    public Iterable<HttpExecutorService> listConnectors(Bundle bundle) {
        Iterable<String> classNames = stringsForResourceInBundle(
                "/META-INF/services/org.openstack4j.core.transport.HttpExecutorService", bundle);
        return instantiateAvailableClasses(bundle, classNames, HttpExecutorService.class);
    }
}
