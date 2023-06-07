package pl.piomin.services.gateway.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayApi {

	@Autowired
	RouteDefinitionLocator locator;

}
