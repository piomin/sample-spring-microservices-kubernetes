package org.springframework.cloud.kubernetes.discovery.ext;

import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.cloud.kubernetes.PodUtils;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryProperties;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class KubernetesAutoServiceRegistration extends AbstractAutoServiceRegistration<KubernetesRegistration> {

    private KubernetesDiscoveryProperties properties;
    private KubernetesRegistrationProperties registrationProperties;
    private KubernetesRegistration registration;
    private PodUtils podUtils;

    KubernetesAutoServiceRegistration(ServiceRegistry<KubernetesRegistration> serviceRegistry,
                                      AutoServiceRegistrationProperties autoServiceRegistrationProperties,
                                      KubernetesRegistration registration, KubernetesDiscoveryProperties properties,
                                      KubernetesRegistrationProperties registrationProperties, PodUtils podUtils) {
        super(serviceRegistry, autoServiceRegistrationProperties);
        this.properties = properties;
        this.registrationProperties = registrationProperties;
        this.registration = registration;
        this.podUtils = podUtils;
    }

    public void setRegistration(int port) throws UnknownHostException {
        String ip = registrationProperties.getIpAddress() != null ? registrationProperties.getIpAddress() : InetAddress.getLocalHost().getHostAddress();
        registration.setHost(ip);
        registration.setPort(port);
        registration.setServiceId(getAppName(properties, getContext().getEnvironment()) + "." + getNamespace(getContext().getEnvironment()));
        registration.getMetadata().put("namespace", getNamespace(getContext().getEnvironment()));
        registration.getMetadata().put("name", getAppName(properties, getContext().getEnvironment()));
        this.registration = registration;
    }

    @Override
    protected Object getConfiguration() {
        return properties;
    }

    @Override
    protected boolean isEnabled() {
        return !podUtils.isInsideKubernetes();
    }

    @Override
    protected KubernetesRegistration getRegistration() {
        return registration;
    }

    @Override
    protected KubernetesRegistration getManagementRegistration() {
        return registration;
    }

    public String getAppName(KubernetesDiscoveryProperties properties, Environment env) {
        final String appName = properties.getServiceName();
        if (StringUtils.hasText(appName)) {
            return appName;
        }
        return env.getProperty("spring.application.name", "application");
    }

    public String getNamespace(Environment env) {
        return env.getProperty("KUBERNETES_NAMESPACE", "external");
    }

}
