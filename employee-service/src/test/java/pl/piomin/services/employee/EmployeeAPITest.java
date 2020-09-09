package pl.piomin.services.employee;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import pl.piomin.services.employee.model.Employee;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableKubernetesMockClient(crud = true)
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
class EmployeeAPITest {

    static KubernetesClient client;

    @Autowired
    TestRestTemplate restTemplate;

    @BeforeAll
    static void init() {
        System.setProperty(Config.KUBERNETES_MASTER_SYSTEM_PROPERTY,
            client.getConfiguration().getMasterUrl());
        System.setProperty(Config.KUBERNETES_TRUST_CERT_SYSTEM_PROPERTY,
            "true");
        System.setProperty(
            Config.KUBERNETES_AUTH_TRYKUBECONFIG_SYSTEM_PROPERTY, "false");
        System.setProperty(
            Config.KUBERNETES_AUTH_TRYSERVICEACCOUNT_SYSTEM_PROPERTY, "false");
        System.setProperty(Config.KUBERNETES_HTTP2_DISABLE, "true");
        System.setProperty(Config.KUBERNETES_NAMESPACE_SYSTEM_PROPERTY,
            "default");
        client.configMaps().inNamespace("default").createNew()
            .withNewMetadata().withName("employee").endMetadata()
            .addToData("application.properties",
                "spring.data.mongodb.uri=mongodb://localhost:27017/test")
            .done();
    }

    @Test
    void addEmployeeTest() {
        Employee employee = new Employee("1", "1", "Test", 30, "test");
        employee = restTemplate.postForObject("/", employee, Employee.class);
        Assertions.assertNotNull(employee);
        Assertions.assertNotNull(employee.getId());
    }

    @Test
    void addAndThenFindEmployeeByIdTest() {
        Employee employee = new Employee("1", "2", "Test2", 20, "test2");
        employee = restTemplate.postForObject("/", employee, Employee.class);
        Assertions.assertNotNull(employee);
        Assertions.assertNotNull(employee.getId());
        employee = restTemplate
            .getForObject("/{id}", Employee.class, employee.getId());
        Assertions.assertNotNull(employee);
        Assertions.assertNotNull(employee.getId());
    }

    @Test
    void findAllEmployeesTest() {
        Employee[] employees =
            restTemplate.getForObject("/", Employee[].class);
        Assertions.assertEquals(2, employees.length);
    }

    @Test
    void findEmployeesByDepartmentTest() {
        Employee[] employees =
            restTemplate.getForObject("/department/1", Employee[].class);
        Assertions.assertEquals(1, employees.length);
    }

    @Test
    void findEmployeesByOrganizationTest() {
        Employee[] employees =
            restTemplate.getForObject("/organization/1", Employee[].class);
        Assertions.assertEquals(2, employees.length);
    }

}
