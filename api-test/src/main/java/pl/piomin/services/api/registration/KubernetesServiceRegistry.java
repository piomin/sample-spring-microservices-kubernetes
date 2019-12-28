package pl.piomin.services.api.registration;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.EndpointAddress;
import io.fabric8.kubernetes.api.model.EndpointAddressBuilder;
import io.fabric8.kubernetes.api.model.EndpointPort;
import io.fabric8.kubernetes.api.model.EndpointPortBuilder;
import io.fabric8.kubernetes.api.model.EndpointSubset;
import io.fabric8.kubernetes.api.model.EndpointSubsetBuilder;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.EndpointsBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryProperties;

public class KubernetesServiceRegistry implements ServiceRegistry<KubernetesRegistration> {

    private static final Logger LOG = LoggerFactory.getLogger(KubernetesServiceRegistry.class);
    private static final int FIRST_PORT = 0;

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
            boolean updated = false;
            for (EndpointSubset subset : endpoints.getSubsets()) {
                if (subset.getPorts().get(FIRST_PORT).getPort().equals(registration.getPort())) {
                    subset.getAddresses().add(new EndpointAddressBuilder().withIp(registration.getHost()).build());
                    updated = true;
                }
            }
            if (!updated) {
                EndpointAddress address = new EndpointAddressBuilder().withIp(registration.getHost()).build();
                EndpointPort port = new EndpointPortBuilder().withPort(registration.getPort()).build();
                EndpointSubset subset = new EndpointSubsetBuilder().withAddresses(address).withPorts(port).build();
                endpoints.getSubsets().add(subset);
            }
            client.endpoints().createOrReplace(endpoints);
        }

    }

    @Override
    public void deregister(KubernetesRegistration registration) {
        LOG.info("De-registering service with kubernetes: " + registration.getInstanceId());
        Endpoints endpoints = client.endpoints()
                .inNamespace(registration.getMetadata().get("namespace"))
                .withName(registration.getMetadata().get("name"))
                .get();
        Optional<EndpointSubset> optSubset = endpoints.getSubsets().stream().filter(s -> s.getPorts().get(0).equals(registration.getPort())).findFirst();
        optSubset.ifPresent(s -> {
            EndpointSubset subset = optSubset.get();
            if (subset.getAddresses().size() == 1 && endpoints.getSubsets().size() == 1) {
                client.endpoints().delete(endpoints);
            } else {
                List<EndpointAddress> addresses = subset.getAddresses().stream()
                        .filter(address -> !address.getIp().equals(registration.getHost()))
                        .collect(Collectors.toList());
                if (addresses.size() > 0) {
                    subset.setAddresses(addresses);
                    int i = endpoints.getSubsets().indexOf(subset);
                    endpoints.getSubsets().set(i, subset);
                } else {
                    endpoints.getSubsets().remove(subset);
                }
                client.endpoints().createOrReplace(endpoints);
            }

        });
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
