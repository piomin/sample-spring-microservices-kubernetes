package pl.piomin.services.api.registration;

import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.cloud.kubernetes.PodUtils;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryProperties;

public class KubernetesAutoServiceRegistration extends AbstractAutoServiceRegistration<KubernetesRegistration> {

	private KubernetesDiscoveryProperties properties;
	private KubernetesAutoRegistration registration;
	private PodUtils podUtils;

	KubernetesAutoServiceRegistration(ServiceRegistry<KubernetesRegistration> serviceRegistry,
			AutoServiceRegistrationProperties autoServiceRegistrationProperties,
			KubernetesDiscoveryProperties properties, KubernetesAutoRegistration registration,
			PodUtils podUtils) {
		super(serviceRegistry, autoServiceRegistrationProperties);
		this.properties = properties;
		this.registration = registration;
		this.podUtils = podUtils;
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
	protected KubernetesAutoRegistration getRegistration() {
		return registration;
	}

	@Override
	protected KubernetesAutoRegistration getManagementRegistration() {
		return registration;
	}

	public void setPort(Integer port) {
		registration.setPort(port);
	}

}
