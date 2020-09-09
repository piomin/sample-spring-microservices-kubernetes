package pl.piomin.services.department;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import pl.piomin.services.department.model.Department;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableKubernetesMockClient(crud = true)
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class DepartmentAPITest {

    static KubernetesClient client;

    @Autowired
    TestRestTemplate restTemplate;

    @BeforeAll
    static void init() {
        System.setProperty(Config.KUBERNETES_MASTER_SYSTEM_PROPERTY,
                client.getConfiguration().getMasterUrl());
        System.setProperty(Config.KUBERNETES_TRUST_CERT_SYSTEM_PROPERTY, "true");
        System.setProperty(Config.KUBERNETES_AUTH_TRYKUBECONFIG_SYSTEM_PROPERTY,
                "false");
        System.setProperty(Config.KUBERNETES_AUTH_TRYSERVICEACCOUNT_SYSTEM_PROPERTY,
                "false");
        System.setProperty(Config.KUBERNETES_HTTP2_DISABLE, "true");
        System.setProperty(Config.KUBERNETES_NAMESPACE_SYSTEM_PROPERTY, "default");
        client.configMaps().inNamespace("default").createNew()
                .withNewMetadata().withName("department").endMetadata()
                .addToData("application.properties",
                        "spring.data.mongodb.uri=mongodb://localhost:27017/test")
                .done();
    }

    @Test
    void addDepartmentTest() {
        Department department = new Department("1", "Test");
        department = restTemplate.postForObject("/", department, Department.class);
        Assertions.assertNotNull(department);
        Assertions.assertNotNull(department.getId());
    }

    @Test
    void addAndThenFindDepartmentByIdTest() {
        Department department = new Department("2", "Test2");
        department = restTemplate.postForObject("/", department, Department.class);
        Assertions.assertNotNull(department);
        Assertions.assertNotNull(department.getId());
        department = restTemplate.getForObject("/{id}", Department.class, department.getId());
        Assertions.assertNotNull(department);
        Assertions.assertNotNull(department.getId());
    }

    @Test
    void findAllDepartmentsTest() {
        Department[] departments = restTemplate.getForObject("/", Department[].class);
        Assertions.assertEquals(2, departments.length);
    }

    @Test
    void findDepartmentsByOrganizationTest() {
        Department[] departments = restTemplate.getForObject("/organization/1", Department[].class);
        Assertions.assertEquals(1, departments.length);
    }

}
