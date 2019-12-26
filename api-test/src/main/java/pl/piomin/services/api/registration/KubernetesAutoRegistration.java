package pl.piomin.services.api.registration;

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

import org.springframework.cloud.commons.util.IdUtils;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

public class KubernetesAutoRegistration extends KubernetesRegistration {

	public static final char SEPARATOR = '-';

	private ApplicationContext applicationContext;

	public KubernetesAutoRegistration(Endpoints endpoints, KubernetesDiscoveryProperties properties,
			ApplicationContext applicationContext) {
		super(endpoints, properties);
		this.applicationContext = applicationContext;
	}

	public static KubernetesAutoRegistration registration(KubernetesDiscoveryProperties properties,
			ApplicationContext context) {
		String appName = getAppName(properties, context.getEnvironment());
		ObjectMeta metadata = new ObjectMetaBuilder().withName(appName).withNamespace("external").build();
		EndpointAddress address = new EndpointAddressBuilder().withIp("192.168.99.1").build();
		EndpointPort port = new EndpointPortBuilder().withPort(8080).build();
		EndpointSubset subset = new EndpointSubsetBuilder().withAddresses(address).withPorts(port).build();
		Endpoints endpoints = new EndpointsBuilder().withSubsets(subset).withMetadata(metadata).build();
		KubernetesAutoRegistration r = new KubernetesAutoRegistration(endpoints, properties, context);
		return r;
	}

	/**
	 * @param properties consul discovery properties
	 * @param env Spring environment
	 * @return the app name, currently the spring.application.name property
	 */
	public static String getAppName(KubernetesDiscoveryProperties properties, Environment env) {
		final String appName = properties.getServiceName();
		if (StringUtils.hasText(appName)) {
			return appName;
		}
		return env.getProperty("spring.application.name", "application");
	}

//	public static String getInstanceId(KubernetesDiscoveryProperties properties,
//			ApplicationContext context) {
//		if (!StringUtils.hasText(properties.getInstanceId())) {
//			return normalizeForDns(IdUtils.getDefaultInstanceId(context.getEnvironment(),
//					properties.isIncludeHostnameInInstanceId()));
//		}
//		return normalizeForDns(properties.getInstanceId());
//	}

	public static String normalizeForDns(String s) {
		if (s == null || !Character.isLetter(s.charAt(0))
				|| !Character.isLetterOrDigit(s.charAt(s.length() - 1))) {
			throw new IllegalArgumentException(
					"Consul service ids must not be empty, must start "
							+ "with a letter, end with a letter or digit, "
							+ "and have as interior characters only letters, "
							+ "digits, and hyphen: " + s);
		}

		StringBuilder normalized = new StringBuilder();
		Character prev = null;
		for (char curr : s.toCharArray()) {
			Character toAppend = null;
			if (Character.isLetterOrDigit(curr)) {
				toAppend = curr;
			}
			else if (prev == null || !(prev == SEPARATOR)) {
				toAppend = SEPARATOR;
			}
			if (toAppend != null) {
				normalized.append(toAppend);
				prev = toAppend;
			}
		}

		return normalized.toString();
	}

}
