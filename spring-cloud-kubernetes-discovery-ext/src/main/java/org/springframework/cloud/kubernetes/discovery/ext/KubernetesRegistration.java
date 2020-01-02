package org.springframework.cloud.kubernetes.discovery.ext;

import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryProperties;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class KubernetesRegistration implements Registration {

    private KubernetesDiscoveryProperties properties;

    private String serviceId;
    private String instanceId;
    private String host;
    private int port;
    private Map<String, String> metadata = new HashMap<>();

    public KubernetesRegistration(KubernetesDiscoveryProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
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
        return metadata;
    }

    @Override
    public String getScheme() {
        return "http";
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

}
