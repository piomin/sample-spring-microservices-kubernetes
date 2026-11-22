package pl.piomin.services.organization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.piomin.services.organization.model.Organization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.cloud.kubernetes.discovery.enabled=false",
        "spring.cloud.kubernetes.config.enabled=false"
    })
@Testcontainers
@TestMethodOrder(MethodOrderer.MethodName.class)
@DirtiesContext
@AutoConfigureRestTestClient
class OrganizationAPITest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:8.0");

    @Autowired
    RestTestClient restClient;

    @Test
    void addOrganizationTest() {
        Organization organization = new Organization("Test", "Test Address");
        restClient.post()
                .uri("/")
                .body(organization)
                .exchange()
                .expectBody(Organization.class)
                .value(Assertions::assertNotNull)
                .value(org -> assertNotNull(org.getId()));
    }

    @Test
    void addAndThenFindOrganizationByIdTest() {
        Organization organization = new Organization("Test2", "Test Address 2");
        organization = restClient.post()
                .uri("/")
                .body(organization)
                .exchange()
                .returnResult(Organization.class)
                .getResponseBody();
        assertNotNull(organization);
        assertNotNull(organization.getId());
        organization = restClient.get()
                .uri("/{id}", organization.getId())
                .exchange()
                .returnResult(Organization.class)
                .getResponseBody();
        assertNotNull(organization);
        assertNotNull(organization.getId());
    }

    @Test
    void findAllOrganizationsTest() {
        restClient.get()
                .uri("/")
                .exchange()
                .expectBody(Organization[].class)
                .value(Assertions::assertNotNull)
                .value(organizations -> assertEquals(2, organizations.length));
    }

}
