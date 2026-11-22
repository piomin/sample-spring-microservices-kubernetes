package pl.piomin.services.employee;

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
import pl.piomin.services.employee.model.Employee;

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
class EmployeeAPITest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:8.0");

    @Autowired
    RestTestClient restClient;

    @Test
    void addEmployeeTest() {
        Employee employee = new Employee("1", "1", "Test", 30, "test");
        restClient.post()
                .uri("/")
                .body(employee)
                .exchange()
                .expectBody(Employee.class)
                .value(Assertions::assertNotNull)
                .value(employee1 -> assertNotNull(employee1.getId()));
    }

    @Test
    void addAndThenFindEmployeeByIdTest() {
        Employee employee = new Employee("1", "2", "Test2", 20, "test2");
        employee = restClient.post()
                .uri("/")
                .body(employee)
                .exchange()
                .returnResult(Employee.class)
                .getResponseBody();
        assertNotNull(employee);
        assertNotNull(employee.getId());
        restClient.get()
                .uri("/{id}", employee.getId())
                .exchange()
                .expectBody(Employee.class)
                .value(Assertions::assertNotNull)
                .value(employee1 -> assertNotNull(employee1.getId()));
    }

    @Test
    void findAllEmployeesTest() {
        restClient.get()
                .uri("/")
                .exchange()
                .expectBody(Employee[].class)
                .value(Assertions::assertNotNull)
                .value(employees -> assertEquals(2, employees.length));
    }

    @Test
    void findEmployeesByDepartmentTest() {
        restClient.get()
                .uri("/department/1")
                .exchange()
                .expectBody(Employee[].class)
                .value(Assertions::assertNotNull)
                .value(employees -> assertEquals(1, employees.length));
    }

    @Test
    void findEmployeesByOrganizationTest() {
        restClient.get()
                .uri("/organization/1")
                .exchange()
                .expectBody(Employee[].class)
                .value(Assertions::assertNotNull)
                .value(employees -> assertEquals(2, employees.length));
    }

}
