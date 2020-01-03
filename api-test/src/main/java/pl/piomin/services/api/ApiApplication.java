package pl.piomin.services.api;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


import javax.annotation.PostConstruct;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;
import one.util.streamex.StreamEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.kubernetes.PodUtils;
//import org.springframework.cloud.kubernetes.discovery.ext.KubernetesRegistration;
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
	DiscoveryClient discoveryClient;
	@Autowired
	PodUtils utils;

	@PostConstruct
	public void init() {
		try {
			Endpoints e = client.endpoints().inNamespace("external").withName("employee").get();
			LOGGER.info("E: {}", e);
			printEndpoints(e);
		} catch (Exception e) {

		}

		LOGGER.info("Services: {}", discoveryClient.getServices());
		Stream<ServiceInstance> s = discoveryClient.getServices().stream().flatMap(it -> discoveryClient.getInstances(it).stream());
		s.forEach(it -> LOGGER.info("Instance: url={}:{}, id={}, service={}", it.getHost(), it.getPort(), it.getInstanceId(), it.getServiceId()));

//		EndpointsList el = client.endpoints().inAnyNamespace().list();
//		Stream<Endpoints> s = el.getItems().stream().filter(endpoint -> endpoint.getMetadata().getName().equals("api-test"));
//		s.forEach(this::printEndpoints);

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
		LOGGER.info("Listing: {}", e);
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

//	@Autowired
//    KubernetesRegistration registration;

//	@Scheduled(fixedDelay = 10000)
//	public void update() {
//		if (registration.getMetadata().isEmpty())
//			return;
//		Resource<Endpoints, DoneableEndpoints> resource = client.endpoints()
//				.inNamespace(registration.getMetadata().get("namespace"))
//				.withName(registration.getMetadata().get("name"));
//		Endpoints endpoints = resource.get();
//
//		LOGGER.info("Updating: {}", endpoints);
//		Optional<EndpointSubset> optSubset = endpoints.getSubsets().stream().filter(s -> s.getPorts().get(0).getPort().equals(registration.getPort())).findFirst();
//		optSubset.ifPresent(subset -> {
//			final int index = endpoints.getSubsets().indexOf(subset);
//			subset.getAddresses().stream()
//					.filter(address -> address.getIp().equals(registration.getHost()))
//					.map(address -> {
//						address.setAdditionalProperty("lastUpdated", System.currentTimeMillis());
//						return address;
//					})
//					.findAny().ifPresent(address -> {
//						int i = subset.getAddresses().indexOf(address);
//						subset.getAddresses().set(i, address);
//						endpoints.getSubsets().set(index, subset);
//						LOGGER.info("Endpoint updated: {}", endpoints);
//						client.endpoints().createOrReplace(endpoints);
//					});
//		});
//	}

}
