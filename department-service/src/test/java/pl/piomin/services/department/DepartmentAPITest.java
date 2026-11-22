package pl.piomin.services.department;

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
import pl.piomin.services.department.model.Department;

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
public class DepartmentAPITest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:8.0");

    @Autowired
    RestTestClient restClient;

    @Test
    void addDepartmentTest() {
        Department department = new Department("1", "Test");
        restClient.post()
                .uri("/")
                .body(department)
                .exchange()
                .expectBody(Department.class)
                .value(Assertions::assertNotNull)
                .value(department1 -> assertNotNull(department1.getId()));
    }

    @Test
    void addAndThenFindDepartmentByIdTest() {
        Department department = new Department("2", "Test2");
        department = restClient.post()
                .uri("/")
                .body(department)
                .exchange()
                .returnResult(Department.class)
                .getResponseBody();
        assertNotNull(department);
        assertNotNull(department.getId());
        department = restClient.get()
                .uri("/{id}", department.getId())
                .exchange()
                .returnResult(Department.class)
                .getResponseBody();
        assertNotNull(department);
        assertNotNull(department.getId());
    }

    @Test
    void findAllDepartmentsTest() {
        restClient.get()
                .uri("/")
                .exchange()
                .expectBody(Department[].class)
                .value(Assertions::assertNotNull)
                .value(departments -> assertEquals(2, departments.length));
    }

    @Test
    void findDepartmentsByOrganizationTest() {
        restClient.get()
                .uri("/organization/1")
                .exchange()
                .expectBody(Department[].class)
                .value(Assertions::assertNotNull)
                .value(departments -> assertEquals(1, departments.length));
    }

}
