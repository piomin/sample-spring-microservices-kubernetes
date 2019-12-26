package pl.piomin.services.api.registration;

import io.fabric8.kubernetes.api.model.Endpoints;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryProperties;

import java.net.URI;
import java.util.Map;

public class KubernetesRegistration implements Registration {

    private final Endpoints endpoints;
    private KubernetesDiscoveryProperties properties;

    public KubernetesRegistration(Endpoints endpoints, KubernetesDiscoveryProperties properties) {
        this.endpoints = endpoints;
        this.properties = properties;
    }

    public Endpoints getService() {
        return endpoints;
    }

    @Override
    public String getInstanceId() {
        return null;
    }

    @Override
    public String getServiceId() {
        return endpoints.getMetadata().getName();
    }

    @Override
    public String getHost() {
        return endpoints.getSubsets().get(0).getAddresses().get(0).getIp();
    }

    @Override
    public int getPort() {
        return endpoints.getSubsets().get(0).getPorts().get(0).getPort();
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public URI getUri() {
        return null;
    }

    @Override
    public Map<String, String> getMetadata() {
        return null;
    }

    @Override
    public String getScheme() {
        return "http";
    }

    public KubernetesDiscoveryProperties getProperties() {
        return properties;
    }

}
