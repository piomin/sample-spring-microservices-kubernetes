package pl.piomin.services.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import one.util.streamex.StreamEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.piomin.services.api.registration.KubernetesRegistration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.kubernetes.PodUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
public class ApiApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

	@Autowired
	KubernetesClient client;
	@Autowired
	PodUtils utils;

	@PostConstruct
	public void init() {
//		Endpoints e = client.endpoints().inNamespace("c").withName("employee").get();
//		printEndpoints(e);

		EndpointsList el = client.endpoints().inAnyNamespace().list();
		Stream<Endpoints> s = el.getItems().stream().filter(endpoint -> endpoint.getMetadata().getName().equals("api-test"));
		s.forEach(this::printEndpoints);

//		List<Endpoints> l = s.collect(Collectors.toList());
//
//		LOGGER.info("Size: {}", l.size());
//		if (l.size() > 0)
//			LOGGER.info("Pods: {}", l.get(0).getSubsets().size());

//		s.forEach(this::printEndpoints);
//		Map<String, List<Endpoints>> m = s.collect(Collectors.groupingBy(endpoints -> endpoints.getMetadata().getNamespace()));
//		if (m.keySet().size() > 1)
//			LOGGER.info("Non unique name across many namespaces");

//		if (!utils.isInsideKubernetes()) {
//			LOGGER.info("Registering...");
//			e = client.endpoints().inNamespace("default").withName("testx").get();
//			printEndpoints(e);
//			if (!endpointExists(e, "192.168.99.1", 8080))
//				LOGGER.info("Required");
//			else
//				LOGGER.info("Exists");
//		}
//		ObjectMeta metadata = new ObjectMetaBuilder().withName("testx").withNamespace("default").build();
//		EndpointAddress address = new EndpointAddressBuilder().withIp("192.168.99.1").build();
//		EndpointPort port = new EndpointPortBuilder().withPort(8080).build();
//		EndpointSubset subset = new EndpointSubsetBuilder().withAddresses(address).withPorts(port).build();
//		Endpoints endpoints = client.endpoints().createNew().withMetadata(metadata).withSubsets(subset).done();
//		printEndpoints(endpoints);

//		e = client.endpoints().inNamespace("default").withName("testx").get();
//		printEndpoints(e);
	}

	private void printEndpoints(Endpoints e) {
		List<EndpointSubset> s = e.getSubsets();
		LOGGER.info("List for: uid={}, generated={}", e.getMetadata().getUid(), e.getMetadata().getGenerateName());
		s.forEach(subset -> {
			subset.getAddresses().forEach(address -> {
				LOGGER.info("IP: {}.{}->{}", e.getMetadata().getName(), e.getMetadata().getNamespace(), address.getIp());
			});
			subset.getPorts().forEach(port -> {
				LOGGER.info("PORT: {}.{}->{}", e.getMetadata().getName(), e.getMetadata().getNamespace(), port.getPort());
			});
		});
	}

	private boolean endpointExists(Endpoints e, String ip, Integer port) {
		boolean exists = false;
		for (EndpointSubset subset : e.getSubsets()) {
			exists = StreamEx.of(subset.getAddresses())
					.zipWith(subset.getPorts().stream())
					.anyMatch(entry -> entry.getKey().getIp().equals(ip) && entry.getValue().getPort().equals(port));
			if (exists)
				return true;
		}
		return false;
	}

	@Autowired
	KubernetesRegistration registration;

	@Scheduled(fixedDelay = 10000)
	public void watch() {
		client.endpoints()
				.inNamespace(registration.getMetadata().get("namespace"))
				.withName(registration.getMetadata().get("name"))
				.edit()
				.editMatchingSubset(builder -> builder.hasMatchingAddress(v -> v.getIp().equals(registration.getHost())));
	}

}
