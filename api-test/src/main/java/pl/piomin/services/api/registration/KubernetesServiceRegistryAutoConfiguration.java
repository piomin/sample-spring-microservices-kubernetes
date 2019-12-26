package pl.piomin.services.api.registration;

import io.fabric8.kubernetes.client.KubernetesClient;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.serviceregistry.ServiceRegistryAutoConfiguration;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.cloud.kubernetes.discovery.registration.enabled", value = "true")
@AutoConfigureBefore(ServiceRegistryAutoConfiguration.class)
public class KubernetesServiceRegistryAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public KubernetesServiceRegistry consulServiceRegistry(KubernetesClient client, KubernetesDiscoveryProperties properties) {
		return new KubernetesServiceRegistry(client, properties);
	}

}
