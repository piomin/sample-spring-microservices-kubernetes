package pl.piomin.services.employee;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.piomin.services.employee.model.Employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.cloud.kubernetes.discovery.enabled=false",
        "spring.cloud.kubernetes.config.enabled=false"})
@Testcontainers
@TestMethodOrder(MethodOrderer.MethodName.class)
class EmployeeAPITest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:5.0");

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void addEmployeeTest() {
        Employee employee = new Employee("1", "1", "Test", 30, "test");
        employee = restTemplate.postForObject("/", employee, Employee.class);
        assertNotNull(employee);
        assertNotNull(employee.getId());
    }

    @Test
    void addAndThenFindEmployeeByIdTest() {
        Employee employee = new Employee("1", "2", "Test2", 20, "test2");
        employee = restTemplate.postForObject("/", employee, Employee.class);
        assertNotNull(employee);
        assertNotNull(employee.getId());
        employee = restTemplate
            .getForObject("/{id}", Employee.class, employee.getId());
        assertNotNull(employee);
        assertNotNull(employee.getId());
    }

    @Test
    void findAllEmployeesTest() {
        Employee[] employees =
            restTemplate.getForObject("/", Employee[].class);
        assertEquals(2, employees.length);
    }

    @Test
    void findEmployeesByDepartmentTest() {
        Employee[] employees =
            restTemplate.getForObject("/department/1", Employee[].class);
        assertEquals(1, employees.length);
    }

    @Test
    void findEmployeesByOrganizationTest() {
        Employee[] employees =
            restTemplate.getForObject("/organization/1", Employee[].class);
        assertEquals(2, employees.length);
    }

}
