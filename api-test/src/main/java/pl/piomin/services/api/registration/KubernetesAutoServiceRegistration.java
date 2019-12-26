package pl.piomin.services.api.registration;

import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.cloud.kubernetes.PodUtils;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryProperties;

public class KubernetesAutoServiceRegistration extends AbstractAutoServiceRegistration<KubernetesRegistration> {

	private KubernetesDiscoveryProperties properties;
	private KubernetesRegistration registration;
	private PodUtils podUtils;

	KubernetesAutoServiceRegistration(ServiceRegistry<KubernetesRegistration> serviceRegistry,
			AutoServiceRegistrationProperties autoServiceRegistrationProperties,
			KubernetesDiscoveryProperties properties, KubernetesRegistration registration,
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
	protected KubernetesRegistration getRegistration() {
		return registration;
	}

	@Override
	protected KubernetesRegistration getManagementRegistration() {
		return registration;
	}

}
