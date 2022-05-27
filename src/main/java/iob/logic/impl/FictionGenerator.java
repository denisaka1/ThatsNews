package iob.logic.impl;

import iob.boundary.InstanceBoundary;
import iob.boundary.NewUserBoundary;
import iob.boundary.UserBoundary;
import iob.boundary.inner.CreatorBoundary;
import iob.boundary.inner.UserID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * This class ensures that fictive users and instances that are needed for special operations
 * that otherwise might circumvent the specified REST API are persisted in the database.
 */
@Component
@Profile("!test") // don't run this in tests
public class FictionGenerator implements CommandLineRunner {

    public static final String FICTIVE_MANAGER = "manager@dummy.com";
    public static final String FICTIVE_INSTANCE = "fictiveInstance";

    private RestTemplate restTemplate;
    private String url;
    private int port;

    private String domain;

    @Value("${spring.application.name}")
    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Value("${server.port:8081}")
    public void setPort(int port) {
        this.port = port;
    }

    @PostConstruct
    public void init() {
        this.url = "http://localhost:" + this.port + "/iob";
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void run(String... args) {
        UserID userID = new UserID(domain, FICTIVE_MANAGER);
        UserBoundary createdManager;

        try {
            createdManager = restTemplate.getForObject(
                    url + "/users/login/{userDomain}/{userEmail}",
                    UserBoundary.class,
                    userID.getDomain(),
                    userID.getEmail()
            );
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                createdManager = createFictiveManager(userID);
            } else {
                throw e;
            }
        }

        InstanceBoundary[] fictiveInstances = restTemplate.getForObject(
                url + "/instances/search/byType/{type}",
                InstanceBoundary[].class,
                FICTIVE_INSTANCE
        );

        if (Objects.requireNonNull(fictiveInstances).length == 0) {
            createFictiveInstance(createdManager);
        }
    }

    private void createFictiveInstance(UserBoundary createdManager) {
        InstanceBoundary fictiveInstance = InstanceBoundary
                .builder()
                .name(FICTIVE_INSTANCE)
                .type(FICTIVE_INSTANCE)
                .active(true)
                .createdBy(new CreatorBoundary(Objects.requireNonNull(createdManager).getUserId()))
                .build();

        InstanceBoundary createFictiveInstance = restTemplate.postForObject(
                url + "/instances",
                fictiveInstance,
                InstanceBoundary.class
        );

        if (createFictiveInstance == null
                || createFictiveInstance.getInstanceId() == null
                || !createFictiveInstance.getType().equals(FICTIVE_INSTANCE)
        ) {
            throw new RuntimeException("Failed to create fictive instance");
        }
    }

    private UserBoundary createFictiveManager(UserID userID) {
        NewUserBoundary manager = new NewUserBoundary();
        manager.setUsername("manager");
        manager.setRole("MANAGER");
        manager.setAvatar("dummyAvatar");
        manager.setEmail(userID.getEmail());

        UserBoundary createdManager = restTemplate.postForObject(
                url + "/users",
                manager,
                UserBoundary.class
        );

        if (createdManager == null
                || createdManager.getUserId() == null
                || !createdManager.getUserId().equals(userID)
        ) {
            throw new RuntimeException("Failed to create manager user");
        }

        return createdManager;
    }
}
