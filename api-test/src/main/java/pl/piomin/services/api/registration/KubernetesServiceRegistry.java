package pl.piomin.services.api.registration;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryProperties;

import java.util.List;

public class KubernetesServiceRegistry implements ServiceRegistry<KubernetesRegistration> {

    private static final Logger LOG = LoggerFactory.getLogger(KubernetesServiceRegistry.class);

    private final KubernetesClient client;
    private KubernetesDiscoveryProperties properties;

    public KubernetesServiceRegistry(KubernetesClient client, KubernetesDiscoveryProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void register(KubernetesRegistration registration) {
        LOG.info("Registering service with kubernetes: " + registration.getServiceId());
        Endpoints endpoints = client.endpoints()
                .inNamespace(registration.getMetadata().get("namespace"))
                .withName(registration.getMetadata().get("name"))
                .get();
        if (endpoints == null) {
            client.endpoints().create(create(registration));
        } else {
            for (EndpointSubset subset : endpoints.getSubsets()) {
                if (subset.getPorts().get(0).getPort().equals(registration.getPort())) {
                    subset.getAddresses().add(new EndpointAddressBuilder().withIp(registration.getHost()).build());
                }
            }
            client.endpoints().createOrReplace(endpoints);
        }

    }

    @Override
    public void deregister(KubernetesRegistration registration) {
        LOG.info("De-registering service with kubernetes: " + registration.getInstanceId());
        Endpoints endpoints = client.endpoints()
                .inNamespace(registration.getService().getMetadata().getNamespace())
                .withName(registration.getService().getMetadata().getName())
                .get();
        List<EndpointAddress> addresses = endpoints.getSubsets().get(0).getAddresses();
        if (addresses.size() == 1 && addresses.get(0).getIp().equals(registration.getService().getSubsets().get(0).getAddresses().get(0).getIp())) {
            client.endpoints().delete(registration.getService());
        } else {
            addresses.remove(registration.getService().getSubsets().get(0).getAddresses().get(0));
            endpoints.getSubsets().get(0).setAddresses(addresses);
            client.endpoints().createOrReplace(endpoints);
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void setStatus(KubernetesRegistration registration, String status) {

    }

    @Override
    public <T> T getStatus(KubernetesRegistration registration) {
        return null;
    }

    private Endpoints create(KubernetesRegistration registration) {
        EndpointAddress address = new EndpointAddressBuilder().withIp(registration.getHost()).build();
        EndpointPort port = new EndpointPortBuilder().withPort(registration.getPort()).build();
        EndpointSubset subset = new EndpointSubsetBuilder().withAddresses(address).withPorts(port).build();
        ObjectMeta metadata = new ObjectMetaBuilder()
                .withName(registration.getMetadata().get("name"))
                .withNamespace(registration.getMetadata().get("namespace"))
                .build();
        Endpoints endpoints = new EndpointsBuilder().withSubsets(subset).withMetadata(metadata).build();
        return endpoints;
    }
}
