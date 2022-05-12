package iob;

import iob.boundary.ActivityBoundary;
import iob.boundary.InstanceBoundary;
import iob.boundary.UserBoundary;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static iob.controller.ControllerConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

public class AdminTest extends TestTemplate {

    // test deleting all activities
    @Test
    public void testDeleteAllActivities() {
        createAndSaveNewActivity();

        UserBoundary adminUser = createAndSaveNewUser("ADMIN");
        String adminDomain = adminUser.getUserId().getDomain();
        String adminEmail = adminUser.getUserId().getEmail();

        restTemplate.delete(baseUrl + "/" + ADMIN_PATH + "/" + ACTIVITIES_PATH + "?userDomain=" + adminDomain + "&userEmail=" + adminEmail);

        ActivityBoundary[] activityBoundaries = restTemplate.getForObject(baseUrl + "/" + ADMIN_PATH + "/" + ACTIVITIES_PATH + "?userDomain=" + adminDomain + "&userEmail=" + adminEmail + "&size=10&page=0", ActivityBoundary[].class);
        assertThat(activityBoundaries)
                .isNotNull()
                .isEmpty();
    }

    // test deleting all instances
    @Test
    public void testDeleteAllInstances() {
        createAndSaveNewInstance();

        UserBoundary adminUser = createAndSaveNewUser("ADMIN");
        String adminDomain = adminUser.getUserId().getDomain();
        String adminEmail = adminUser.getUserId().getEmail();

        restTemplate.delete(baseUrl + "/" + ADMIN_PATH + "/" + INSTANCES_PATH + "?userDomain=" + adminDomain + "&userEmail=" + adminEmail);

        UserBoundary managerUser = createAndSaveNewUser("MANAGER");
        String managerDomain = managerUser.getUserId().getDomain();
        String managerEmail = managerUser.getUserId().getEmail();

        InstanceBoundary[] instanceBoundaries = restTemplate.getForObject(instancesUrl + "?userDomain=" + managerDomain + "&userEmail=" + managerEmail + "&size=10&page=0", InstanceBoundary[].class);
        assertThat(instanceBoundaries)
                .isNotNull()
                .isEmpty();

    }

    // test deleting all users
    @Test
    public void testDeleteAllUsers() {
        UserBoundary adminUser = createAndSaveNewUser("ADMIN");
        String adminDomain = adminUser.getUserId().getDomain();
        String adminEmail = adminUser.getUserId().getEmail();

        for (int i = 0; i < 4; i++) {
            createAndSaveNewUser();
        }

        restTemplate.delete(baseUrl + "/" + ADMIN_PATH + "/" + USERS_PATH + "?userDomain=" + adminDomain + "&userEmail=" + adminEmail);

        adminUser = createAndSaveNewUser("ADMIN");
        adminDomain = adminUser.getUserId().getDomain();
        adminEmail = adminUser.getUserId().getEmail();

        UserBoundary[] userBoundaries = restTemplate.getForObject(baseUrl + "/" + ADMIN_PATH + "/" + USERS_PATH + "?userDomain=" + adminDomain + "&userEmail=" + adminEmail + "&size=10&page=0", UserBoundary[].class);
        assertThat(userBoundaries)
                .isNotNull()
                .hasSize(1)
                .containsExactly(adminUser);
    }

    // test exporting all activities
    @Test
    public void testExportAllActivities() {
        UserBoundary adminUser = createAndSaveNewUser("ADMIN");
        String adminDomain = adminUser.getUserId().getDomain();
        String adminEmail = adminUser.getUserId().getEmail();
        ActivityBoundary activityBoundary = createAndSaveNewActivity();

        ActivityBoundary[] activityBoundaries = restTemplate.getForObject(baseUrl + "/" + ADMIN_PATH + "/" + ACTIVITIES_PATH + "?userDomain=" + adminDomain + "&userEmail=" + adminEmail, ActivityBoundary[].class);
        assertThat(activityBoundaries)
                .isNotNull()
                .hasSize(1)
                .containsExactly(activityBoundary);
    }

    // test exporting all users
    @Test
    public void testExportAllUsers() {
        UserBoundary adminUser = createAndSaveNewUser("ADMIN");
        String adminDomain = adminUser.getUserId().getDomain();
        String adminEmail = adminUser.getUserId().getEmail();

        List<UserBoundary> list = IntStream.range(0, 5)
                .mapToObj(x -> createAndSaveNewUser())
                .collect(Collectors.toList());

        UserBoundary[] userBoundaries = restTemplate.getForObject(baseUrl + "/" + ADMIN_PATH + "/" + USERS_PATH + "?userDomain=" + adminDomain + "&userEmail=" + adminEmail, UserBoundary[].class);
        assertThat(userBoundaries)
                .isNotNull()
                .hasSize(list.size() + 1) // +1 for admin
                .containsAll(list)
                .contains(adminUser);
    }
}