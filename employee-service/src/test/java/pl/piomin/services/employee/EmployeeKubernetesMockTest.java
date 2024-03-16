package pl.piomin.services.employee;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.piomin.services.employee.model.Employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.cloud-platform=KUBERNETES",
                "spring.cloud.bootstrap.enabled=true"})
@EnableKubernetesMockClient(crud = true)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public class EmployeeKubernetesMockTest {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeKubernetesMockTest.class);

    static KubernetesClient client;

    @Container
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:5.0");

    @BeforeAll
    static void setup() {
        System.setProperty(Config.KUBERNETES_MASTER_SYSTEM_PROPERTY, client.getConfiguration().getMasterUrl());
        System.setProperty(Config.KUBERNETES_TRUST_CERT_SYSTEM_PROPERTY, "true");
        System.setProperty(Config.KUBERNETES_NAMESPACE_SYSTEM_PROPERTY, "default");

        ConfigMap cm = client.configMaps()
                .resource(buildConfigMap(mongodb.getMappedPort(27017)))
                .create();
        LOG.info("!!! {}", cm);
    }

    private static ConfigMap buildConfigMap(int port) {
        return new ConfigMapBuilder().withNewMetadata()
                .withName("employee").withNamespace("default")
                .endMetadata()
                .addToData("application.properties",
                        """
                        spring.data.mongodb.host=localhost
                        spring.data.mongodb.port=%d
                        spring.data.mongodb.database=test
                        spring.data.mongodb.authentication-database=test
                        """.formatted(port))
                .build();
    }

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    @Order(1)
    void addEmployeeTest() {
        Employee employee = new Employee("1", "1", "Test", 30, "test");
        employee = restTemplate.postForObject("/", employee, Employee.class);
        assertNotNull(employee);
        assertNotNull(employee.getId());
    }

    @Test
    @Order(2)
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
    @Order(3)
    void findAllEmployeesTest() {
        Employee[] employees =
                restTemplate.getForObject("/", Employee[].class);
        assertEquals(2, employees.length);
    }

    @Test
    @Order(3)
    void findEmployeesByDepartmentTest() {
        Employee[] employees =
                restTemplate.getForObject("/department/1", Employee[].class);
        assertEquals(1, employees.length);
    }

    @Test
    @Order(3)
    void findEmployeesByOrganizationTest() {
        Employee[] employees =
                restTemplate.getForObject("/organization/1", Employee[].class);
        assertEquals(2, employees.length);
    }

}
