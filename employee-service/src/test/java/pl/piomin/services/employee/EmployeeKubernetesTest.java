package pl.piomin.services.employee;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
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
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import pl.piomin.services.employee.model.Employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.cloud-platform=KUBERNETES",
                "spring.cloud.bootstrap.enabled=true"})
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public class EmployeeKubernetesTest {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeKubernetesTest.class);

    @Container
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:5.0");
    @Container
    static K3sContainer k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.21.3-k3s1"));

    @BeforeAll
    static void setup() {
        Config config = Config.fromKubeconfig(k3s.getKubeConfigYaml());
        KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build();

        System.setProperty(Config.KUBERNETES_MASTER_SYSTEM_PROPERTY, client.getConfiguration().getMasterUrl());
        System.setProperty(Config.KUBERNETES_CLIENT_CERTIFICATE_DATA_SYSTEM_PROPERTY,
                client.getConfiguration().getClientCertData());
        System.setProperty(Config.KUBERNETES_CA_CERTIFICATE_DATA_SYSTEM_PROPERTY,
                client.getConfiguration().getCaCertData());
        System.setProperty(Config.KUBERNETES_CLIENT_KEY_DATA_SYSTEM_PROPERTY,
                client.getConfiguration().getClientKeyData());
        System.setProperty(Config.KUBERNETES_TRUST_CERT_SYSTEM_PROPERTY, "true");
        System.setProperty(Config.KUBERNETES_AUTH_TRYKUBECONFIG_SYSTEM_PROPERTY, "false");
        System.setProperty(Config.KUBERNETES_HTTP2_DISABLE, "true");
        System.setProperty(Config.KUBERNETES_NAMESPACE_SYSTEM_PROPERTY, "default");

        ConfigMap cm = client.configMaps().inNamespace("default")
                .createOrReplace(buildConfigMap(mongodb.getMappedPort(27017)));
        LOG.info("!!! {}", cm);
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

    private static ConfigMap buildConfigMap(int port) {
        return new ConfigMapBuilder().withNewMetadata()
                .withName("employee").withNamespace("default")
                .endMetadata()
                .addToData("application.properties",
                        "spring.data.mongodb.uri=mongodb://localhost:" + port + "/test")
                .build();
    }
}
