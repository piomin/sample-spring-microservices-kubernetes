package pl.piomin.services.api.registration;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryProperties;

public class KubernetesServiceRegistry implements ServiceRegistry<KubernetesRegistration> {

    private final KubernetesClient client;
    private KubernetesDiscoveryProperties properties;

    public KubernetesServiceRegistry(KubernetesClient client, KubernetesDiscoveryProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void register(KubernetesRegistration registration) {
        client.endpoints().createOrReplace(registration.getService());
    }

    @Override
    public void deregister(KubernetesRegistration registration) {
        client.endpoints().delete(registration.getService());
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
}
