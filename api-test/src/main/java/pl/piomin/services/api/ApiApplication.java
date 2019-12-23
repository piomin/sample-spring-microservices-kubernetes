package pl.piomin.services.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import io.fabric8.kubernetes.api.model.EndpointSubset;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.EndpointsList;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

	@Autowired
	KubernetesClient client;

	@PostConstruct
	public void init() {
		Endpoints e = client.endpoints().inNamespace("c").withName("employee").get();
		printEndpoints(e);

		EndpointsList el = client.endpoints().inAnyNamespace().list();
		Stream<Endpoints> s = el.getItems().stream().filter(endpoint -> endpoint.getMetadata().getName().equals("employee"));
		List<Endpoints> l = s.collect(Collectors.toList());

		LOGGER.info("Size: {}", l.size());
		LOGGER.info("Pods: {}", l.get(0).getSubsets().size());

//		s.forEach(this::printEndpoints);
//		Map<String, List<Endpoints>> m = s.collect(Collectors.groupingBy(endpoints -> endpoints.getMetadata().getNamespace()));
//		if (m.keySet().size() > 1)
//			LOGGER.info("Non unique name across many namespaces");

	}

	private void printEndpoints(Endpoints e) {
		List<EndpointSubset> s = e.getSubsets();
		s.forEach(subset -> {
			subset.getAddresses().forEach(address -> {
				LOGGER.info("IP: {}.{}->{}", e.getMetadata().getName(), e.getMetadata().getNamespace(), address.getIp());
			});
		});
	}

}
