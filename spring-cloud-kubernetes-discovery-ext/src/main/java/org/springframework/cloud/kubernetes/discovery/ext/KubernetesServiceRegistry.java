package org.springframework.cloud.kubernetes.discovery.ext;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryProperties;

import java.util.Collections;

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
        Resource<Endpoints, DoneableEndpoints> resource = client.endpoints()
                .inNamespace(registration.getMetadata().get("namespace"))
                .withName(registration.getMetadata().get("name"));
        Endpoints endpoints = resource.get();
        if (endpoints == null) {
            Endpoints e = client.endpoints().create(create(registration));
            LOG.info("New endpoint: {}", e);
            Service s = client.services().createNew()
                    .withMetadata(new ObjectMetaBuilder()
                            .withName(registration.getMetadata().get("name"))
                            .withLabels(Collections.singletonMap("app", registration.getMetadata().get("name")))
                            .build())
                    .withSpec(new ServiceSpecBuilder()
                            .withPorts(new ServicePortBuilder().withProtocol("TCP").withPort(registration.getPort()).build())
                            .withSelector(Collections.singletonMap("app", registration.getMetadata().get("name")))
                            .build())
                    .done();
            LOG.info("New service: {}", s);
        } else {
            try {
                Endpoints updatedEndpoints = resource.edit()
                        .editMatchingSubset(builder -> builder.hasMatchingPort(v -> v.getPort().equals(registration.getPort())))
                        .addToAddresses(new EndpointAddressBuilder().withIp(registration.getHost()).build())
                        .endSubset()
                        .done();
                LOG.info("Endpoint updated: {}", updatedEndpoints);
            } catch (RuntimeException e) {
                Endpoints updatedEndpoints = resource.edit()
                        .addNewSubset()
                        .withPorts(new EndpointPortBuilder().withPort(registration.getPort()).build())
                        .withAddresses(new EndpointAddressBuilder().withIp(registration.getHost()).build())
                        .endSubset()
                        .done();
                LOG.info("Endpoint updated: {}", updatedEndpoints);
            }
        }

    }

    @Override
    public void deregister(KubernetesRegistration registration) {
        LOG.info("De-registering service with kubernetes: " + registration.getInstanceId());
        Resource<Endpoints, DoneableEndpoints> resource = client.endpoints()
                .inNamespace(registration.getMetadata().get("namespace"))
                .withName(registration.getMetadata().get("name"));

        EndpointAddress address = new EndpointAddressBuilder().withIp(registration.getHost()).build();
        Endpoints updatedEndpoints = resource.edit()
                .editMatchingSubset(builder -> builder.hasMatchingPort(v -> v.getPort().equals(registration.getPort())))
                .removeFromAddresses(address)
                .endSubset()
                .done();
        LOG.info("Endpoint updated: {}", updatedEndpoints);

        resource.get().getSubsets().stream()
                .filter(subset -> subset.getAddresses().size() == 0)
                .forEach(subset -> resource.edit()
                        .removeFromSubsets(subset)
                        .done());
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
