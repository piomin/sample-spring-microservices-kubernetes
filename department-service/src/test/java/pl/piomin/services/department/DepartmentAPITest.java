package pl.piomin.services.department;

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
import pl.piomin.services.department.model.Department;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.kubernetes.discovery.enabled=false",
                "spring.cloud.kubernetes.config.enabled=false"})
@Testcontainers
@TestMethodOrder(MethodOrderer.MethodName.class)
public class DepartmentAPITest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:4.4");

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void addDepartmentTest() {
        Department department = new Department("1", "Test");
        department = restTemplate.postForObject("/", department, Department.class);
        assertNotNull(department);
        assertNotNull(department.getId());
    }

    @Test
    void addAndThenFindDepartmentByIdTest() {
        Department department = new Department("2", "Test2");
        department = restTemplate.postForObject("/", department, Department.class);
        assertNotNull(department);
        assertNotNull(department.getId());
        department = restTemplate.getForObject("/{id}", Department.class, department.getId());
        assertNotNull(department);
        assertNotNull(department.getId());
    }

    @Test
    void findAllDepartmentsTest() {
        Department[] departments = restTemplate.getForObject("/", Department[].class);
        assertEquals(2, departments.length);
    }

    @Test
    void findDepartmentsByOrganizationTest() {
        Department[] departments = restTemplate.getForObject("/organization/1", Department[].class);
        assertEquals(1, departments.length);
    }

}
