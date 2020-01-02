package org.springframework.cloud.kubernetes.discovery.ext;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.cloud.kubernetes.discovery")
public class KubernetesRegistrationProperties {

    /**
     * Used only for external app - IP address to use when accessing service (must also set preferIpAddress to use).
     */
    private String ipAddress;

    /**
     * Used only for external app - Hostname to use when accessing server.
     */
    private String hostname;

    /**
     * Used only for external app - Use ip address rather than hostname during registration.
     */
    private boolean preferIpAddress;

    /**
     * Used only for external app - Port to register the service under (defaults to listening port).
     */
    private Integer port;

    /**
     * Used only for external app - enable registration
     */
    private boolean register;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public boolean isPreferIpAddress() {
        return preferIpAddress;
    }

    public void setPreferIpAddress(boolean preferIpAddress) {
        this.preferIpAddress = preferIpAddress;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public boolean isRegister() {
        return register;
    }

    public void setRegister(boolean register) {
        this.register = register;
    }
}
