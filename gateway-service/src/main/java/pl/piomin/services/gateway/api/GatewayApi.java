package pl.piomin.services.gateway.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Flux;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class GatewayApi {

	@Autowired
	RouteDefinitionLocator locator;

	@Primary
	@Bean
	public SwaggerResourcesProvider swaggerResourcesProvider() {
		return () -> {
			List<SwaggerResource> resources = new ArrayList<>();
			Flux<RouteDefinition> definitions = locator.getRouteDefinitions();
			definitions
					.filter(routeDefinition -> !routeDefinition.getId().startsWith("ReactiveCompositeDiscoveryClient_"))
					.subscribe(routeDefinition -> resources.add(createResource(routeDefinition.getId(), "2.0")));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return resources;
		};
	}

	private SwaggerResource createResource(String location, String version) {
		SwaggerResource swaggerResource = new SwaggerResource();
		swaggerResource.setName(location);
		swaggerResource.setLocation("/" + location + "/v2/api-docs");
		swaggerResource.setSwaggerVersion(version);
		return swaggerResource;
	}

}
