package pl.piomin.services.department.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import pl.piomin.services.department.model.Department;
import pl.piomin.services.department.model.Employee;
import pl.piomin.services.department.repository.DepartmentRepository;

import java.util.Arrays;
import java.util.List;

public class DepartmentWithRestTemplateController {


    private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentController.class);

    DepartmentRepository repository;
    RestTemplate restTemplate;

    public DepartmentWithRestTemplateController(DepartmentRepository repository, RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/")
    public Department add(@RequestBody Department department) {
        LOGGER.info("Department add: {}", department);
        return repository.save(department);
    }

    @GetMapping("/{id}")
    public Department findById(@PathVariable("id") String id) {
        LOGGER.info("Department find: id={}", id);
        return repository.findById(id).get();
    }

    @GetMapping("/{id}/with-employees")
    public Department findByIdWithEmployees(@PathVariable("id") String id) {
        LOGGER.info("Department findByIdWithEmployees: id={}", id);
        Department department = repository.findById(id).orElseThrow();
        department.setEmployees(findEmployeesByDepartment(department.getId()));
        return department;
    }

    @GetMapping("/")
    public Iterable<Department> findAll() {
        LOGGER.info("Department find");
        return repository.findAll();
    }

    @GetMapping("/organization/{organizationId}")
    public List<Department> findByOrganization(@PathVariable("organizationId") String organizationId) {
        LOGGER.info("Department find: organizationId={}", organizationId);
        return repository.findByOrganizationId(organizationId);
    }

    @GetMapping("/organization/{organizationId}/with-employees")
    public List<Department> findByOrganizationWithEmployees(@PathVariable("organizationId") String organizationId) {
        LOGGER.info("Department find: organizationId={}", organizationId);
        List<Department> departments = repository.findByOrganizationId(organizationId);
        departments.forEach(d -> d.setEmployees(findEmployeesByDepartment(d.getId())));
        return departments;
    }

    private List<Employee> findEmployeesByDepartment(String departmentId) {
        Employee[] employees = restTemplate
                .getForObject("http://employee//department/{departmentId}", Employee[].class, departmentId);
        return Arrays.asList(employees);
    }

}
