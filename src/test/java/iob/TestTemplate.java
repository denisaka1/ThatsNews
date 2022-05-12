package iob;

import iob.boundary.ActivityBoundary;
import iob.boundary.InstanceBoundary;
import iob.boundary.NewUserBoundary;
import iob.boundary.UserBoundary;
import iob.boundary.inner.ActivityInstanceBoundary;
import iob.boundary.inner.CreatorBoundary;
import iob.boundary.inner.InvokerBoundary;
import iob.boundary.inner.LocationBoundary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;

import static iob.controller.ControllerConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public abstract class TestTemplate {
    protected int port;
    protected String baseUrl, activitiesUrl, instancesUrl, usersUrl;
    protected RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.application.name}")
    private String domain;

    private int currentUserNum = 0;

    @LocalServerPort
    public void setPort(int port) {
        this.port = port;
    }

    @PostConstruct
    public void init() {
        baseUrl = "http://localhost:" + port + "/" + IOB_PATH;
        activitiesUrl = baseUrl + "/" + ACTIVITIES_PATH;
        instancesUrl = baseUrl + "/" + INSTANCES_PATH;
        usersUrl = baseUrl + "/" + USERS_PATH;
    }

    protected UserBoundary createAndSaveNewUser() {
        return createAndSaveNewUser("PLAYER");
    }

    protected UserBoundary createAndSaveNewUser(String role) {
        String email = "dummyUser" + currentUserNum++ + "@gmail.com",
                username = "testUser",
                avatar = "anAvatarURL";

        UserBoundary newUser = restTemplate.postForObject(
                baseUrl + "/" + USERS_PATH,
                new NewUserBoundary(email, role, username, avatar),
                UserBoundary.class
        );

        assertThat(newUser).isNotNull();
        assertThat(newUser.getUserId()).isNotNull();
        assertThat(newUser.getUserId().getDomain()).isNotNull();
        assertThat(newUser.getUserId().getDomain()).isEqualTo(domain);
        assertThat(newUser.getUserId().getEmail()).isEqualTo(email);
        assertThat(newUser.getRole()).isEqualTo(role);
        assertThat(newUser.getUsername()).isEqualTo(username);
        assertThat(newUser.getAvatar()).isEqualTo(avatar);

        return newUser;
    }

    protected InstanceBoundary createAndSaveNewInstance() {
        return createAndSaveNewInstance(true);
    }

    protected InstanceBoundary createAndSaveNewInstance(boolean active) {
        return createAndSaveNewInstance(active, "testInstance", "testType", null);
    }

    protected InstanceBoundary createAndSaveNewInstance(boolean active, String name, String type, LocationBoundary location) {
        InstanceBoundary newInstance = new InstanceBoundary();
        newInstance.setName(name);
        newInstance.setType(type);
        newInstance.setActive(active);
        Map<String, Object> attributesMap = new HashMap<>();
        attributesMap.put("testKey", "testValue");
        newInstance.setInstanceAttributes(attributesMap);
        newInstance.setLocation(location);
        newInstance.setCreatedBy(new CreatorBoundary(createAndSaveNewUser("MANAGER").getUserId()));
        InstanceBoundary savedInstance = restTemplate.postForObject(
                instancesUrl,
                newInstance,
                InstanceBoundary.class
        );

        assertThat(savedInstance).isNotNull();
        assertThat(savedInstance.getInstanceId()).isNotNull();
        assertThat(savedInstance.getInstanceId().getDomain()).isNotNull();
        assertThat(savedInstance.getInstanceId().getDomain()).isEqualTo(domain);
        assertThat(savedInstance.getInstanceId().getId()).isNotNull();
        assertThat(savedInstance.getName()).isEqualTo(newInstance.getName());
        assertThat(savedInstance.getType()).isEqualTo(newInstance.getType());
        assertThat(savedInstance.getInstanceAttributes()).isEqualTo(newInstance.getInstanceAttributes());
        assertThat(savedInstance.getCreatedBy()).isEqualTo(newInstance.getCreatedBy());
        assertThat(savedInstance.getActive()).isEqualTo(newInstance.getActive());

        return savedInstance;
    }

    protected ActivityBoundary createAndSaveNewActivity() {
        Map<String, Object> attributesMap = new HashMap<>();
        attributesMap.put("testKey", "testValue");
        InstanceBoundary instanceBoundary = createAndSaveNewInstance();
        ActivityBoundary activityBoundary = new ActivityBoundary();
        UserBoundary userBoundary = createAndSaveNewUser("PLAYER");
        activityBoundary.setType("TEST_TYPE");
        activityBoundary.setInvokedBy(new InvokerBoundary(userBoundary.getUserId()));
        activityBoundary.setActivityAttributes(attributesMap);
        activityBoundary.setInstance(new ActivityInstanceBoundary(instanceBoundary.getInstanceId()));
        ActivityBoundary savedActivity = restTemplate.postForObject(
                activitiesUrl,
                activityBoundary,
                ActivityBoundary.class
        );
        assertThat(savedActivity).isNotNull();
        assertThat(savedActivity.getActivityId()).isNotNull();
        assertThat(savedActivity.getActivityId().getDomain()).isNotNull();
        assertThat(savedActivity.getActivityId().getId()).isNotNull();
        assertThat(savedActivity.getType()).isEqualTo(activityBoundary.getType());
        assertThat(savedActivity.getActivityAttributes()).isEqualTo(activityBoundary.getActivityAttributes());
        assertThat(savedActivity.getInvokedBy()).isEqualTo(activityBoundary.getInvokedBy());

        return savedActivity;
    }

    // make sure that after each test, the database is empty
    @AfterEach
    public void tearDown() {
        restTemplate.delete(baseUrl + "/" + ADMIN_PATH + "/" + ACTIVITIES_PATH);
        restTemplate.delete(baseUrl + "/" + ADMIN_PATH + "/" + INSTANCES_PATH);
        restTemplate.delete(baseUrl + "/" + ADMIN_PATH + "/" + USERS_PATH);
    }

}
