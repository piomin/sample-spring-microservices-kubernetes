package pl.piomin.services.employee;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import pl.piomin.services.employee.model.Employee;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
class EmployeeAPITest {

    @Autowired
    TestRestTemplate restTemplate;

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
