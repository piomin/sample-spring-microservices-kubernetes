package pl.piomin.services.department;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit5.HoverflyExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.piomin.services.department.model.Department;
import pl.piomin.services.department.model.Employee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.json;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.cloud-platform=KUBERNETES",
        "spring.cloud.bootstrap.enabled=true"})
@ExtendWith(HoverflyExtension.class)
@EnableKubernetesMockClient(crud = true)
@Testcontainers
public class DepartmentAPIAdvancedTest {

    private static final Logger LOG = LoggerFactory.getLogger(DepartmentAPIAdvancedTest.class);

    static KubernetesClient client;

    @Container
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:5.0");

    @BeforeAll
    static void setup() {
        System.setProperty(Config.KUBERNETES_MASTER_SYSTEM_PROPERTY, client.getConfiguration().getMasterUrl());
        System.setProperty(Config.KUBERNETES_TRUST_CERT_SYSTEM_PROPERTY, "true");
        System.setProperty(Config.KUBERNETES_AUTH_TRYKUBECONFIG_SYSTEM_PROPERTY, "false");
        System.setProperty(Config.KUBERNETES_AUTH_TRYSERVICEACCOUNT_SYSTEM_PROPERTY,
            "false");
        System.setProperty(Config.KUBERNETES_HTTP2_DISABLE, "true");
        System.setProperty(Config.KUBERNETES_NAMESPACE_SYSTEM_PROPERTY, "default");

        ConfigMap cm = client.configMaps()
                .resource(buildConfigMap(mongodb.getMappedPort(27017)))
                .create();
        LOG.info("!!! {}", cm);

        Service s = client.services()
                .resource(buildService())
                .create();
        LOG.info("!!! {}", s);

        Endpoints e = client.endpoints()
                .resource(buildEndpoints())
                .create();
        LOG.info("!!! {}", e);
    }


    private static ConfigMap buildConfigMap(int port) {
        return new ConfigMapBuilder().withNewMetadata()
            .withName("department").withNamespace("default")
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
    void addDepartmentTest() {
        Department department = new Department("1", "Test");
        department = restTemplate.postForObject("/", department, Department.class);
        assertNotNull(department);
        assertNotNull(department.getId());
    }

    @Test
    void findByOrganizationWithEmployees(Hoverfly hoverfly) {
        Department department = new Department("1", "Test");
        department = restTemplate.postForObject("/", department, Department.class);
        assertNotNull(department);
        assertNotNull(department.getId());

        hoverfly.simulate(
            dsl(service("employee.default:8080")
                .get("/department/" + department.getId())
                .willReturn(success().body(json(buildEmployees())))));

        Department[] departments = restTemplate
            .getForObject("/organization/{organizationId}/with-employees", Department[].class, 1L);
        assertEquals(1, departments.length);
        assertEquals(1, departments[0].getEmployees().size());
    }

    private static Service buildService() {
        return new ServiceBuilder().withNewMetadata().withName("employee")
                .withNamespace("default").withLabels(new HashMap<>())
                .withAnnotations(new HashMap<>()).endMetadata().withNewSpec().addNewPort()
                .withPort(8080).endPort().endSpec().build();
    }

    private static Endpoints buildEndpoints() {
        return new EndpointsBuilder().withNewMetadata()
            .withName("employee").withNamespace("default")
            .endMetadata()
            .addNewSubset().addNewAddress()
            .withIp("employee.default").endAddress().addNewPort().withName("http")
            .withPort(8080).endPort().endSubset()
            .build();
    }

    private List<Employee> buildEmployees() {
        List<Employee> employees = new ArrayList<>();
        Employee employee = new Employee();
        employee.setId("abc123");
        employee.setAge(30);
        employee.setName("Test");
        employee.setPosition("test");
        employees.add(employee);
        return employees;
    }

}
