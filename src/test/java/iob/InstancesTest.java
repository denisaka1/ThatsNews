package iob;

import iob.boundary.InstanceBoundary;
import iob.boundary.UserBoundary;
import iob.boundary.inner.LocationBoundary;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static iob.controller.ControllerConstants.SEARCH_PATH;
import static org.assertj.core.api.Assertions.assertThat;

public class InstancesTest extends TestTemplate {

    private final String aUniqueStringToLookFor = UUID.randomUUID().toString();

    // test creating new instance
    @Test
    public void testCreateInstance() {
        createAndSaveNewInstance();
    }

    // test getting all instances
    @Test
    public void testGetAllInstances() {
        UserBoundary manager = createAndSaveNewUser("MANAGER");
        InstanceBoundary instanceBoundary = createAndSaveNewInstance();
        InstanceBoundary inactiveInstanceBoundary = createAndSaveNewInstance(false);

        InstanceBoundary[] allInstances = restTemplate.getForObject(
                instancesUrl + "?userDomain={userDomain}&userEmail={userEmail}&size={size}&page={page}",
                InstanceBoundary[].class,
                manager.getUserId().getDomain(),
                manager.getUserId().getEmail(),
                20,
                0
        );

        assertThat(allInstances)
                .isNotNull()
                .hasSize(2)
                .contains(instanceBoundary, inactiveInstanceBoundary);
    }

    @Test
    public void testGetActiveInstances() {
        UserBoundary player = createAndSaveNewUser("PLAYER");
        InstanceBoundary instanceBoundary = createAndSaveNewInstance();
        InstanceBoundary inactiveInstanceBoundary = createAndSaveNewInstance(false);

        InstanceBoundary[] allInstances = restTemplate.getForObject(
                instancesUrl + "?userDomain={userDomain}&userEmail={userEmail}",
                InstanceBoundary[].class,
                player.getUserId().getDomain(),
                player.getUserId().getEmail()
        );

        assertThat(allInstances)
                .isNotNull()
                .hasSize(1)
                .contains(instanceBoundary)
                .doesNotContain(inactiveInstanceBoundary);
    }

    // test getting instance by id
    @Test
    public void testGetInstanceById() {
        InstanceBoundary instanceBoundary = createAndSaveNewInstance();
        InstanceBoundary instance = restTemplate.getForObject(
                instancesUrl + "/{instanceDomain}/{instanceId}",
                InstanceBoundary.class,
                instanceBoundary.getInstanceId().getDomain(),
                instanceBoundary.getInstanceId().getId()
        );

        assertThat(instance).isNotNull();
        assertThat(instance).isEqualTo(instanceBoundary);
    }

    // test updating instance
    @Test
    public void testUpdateInstance() {
        InstanceBoundary instance = createAndSaveNewInstance();

        LocationBoundary location = new LocationBoundary(42.0, 13.37);
        instance.setLocation(location);
        Map<String, Object> updatedAttrMap = new HashMap<>();
        updatedAttrMap.put("attr1", "updatedAttr1");
        instance.setInstanceAttributes(updatedAttrMap);

        restTemplate.put(
                instancesUrl + "/{instanceDomain}/{instanceId}",
                instance,
                instance.getInstanceId().getDomain(),
                instance.getInstanceId().getId()
        );

        InstanceBoundary updated = restTemplate.getForObject(
                instancesUrl + "/{instanceDomain}/{instanceId}",
                InstanceBoundary.class,
                instance.getInstanceId().getDomain(),
                instance.getInstanceId().getId()
        );
        assertThat(updated).isNotNull();
        assertThat(updated.getLocation()).isEqualTo(location);
        assertThat(updated.getInstanceAttributes()).containsAllEntriesOf(instance.getInstanceAttributes());
    }

    @Test
    public void testSearchInstancesByName() {
        List<InstanceBoundary> shouldBeFound = IntStream.range(0, 5)
                .mapToObj(i -> createAndSaveNewInstance(false, aUniqueStringToLookFor, "instance" + i, null))
                .collect(Collectors.toList());

        List<InstanceBoundary> shouldNotBeFound = IntStream.range(0, 5)
                .mapToObj(i -> createAndSaveNewInstance(false, "instance" + i, "instance" + i, null))
                .collect(Collectors.toList());

        InstanceBoundary[] allInstances = restTemplate.getForObject(
                instancesUrl + "/" + SEARCH_PATH +
                        "/byName/{name}",
                InstanceBoundary[].class,
                aUniqueStringToLookFor
        );

        assertThat(allInstances)
                .isNotNull()
                .containsAll(shouldBeFound)
                .doesNotContainAnyElementsOf(shouldNotBeFound);
    }

    @Test
    public void testSearchActiveInstancesByName() {
        UserBoundary player = createAndSaveNewUser("PLAYER");
        String playerDomain = player.getUserId().getDomain();
        String playerEmail = player.getUserId().getEmail();

        List<InstanceBoundary> shouldBeFound = IntStream.range(0, 5)
                .mapToObj(i -> createAndSaveNewInstance(true, aUniqueStringToLookFor, "instance" + i, null))
                .collect(Collectors.toList());

        List<InstanceBoundary> shouldNotBeFoundBecauseNotActive = IntStream.range(0, 5)
                .mapToObj(i -> createAndSaveNewInstance(false, aUniqueStringToLookFor, "instance" + i, null))
                .collect(Collectors.toList());

        List<InstanceBoundary> shouldNotBeFoundBecauseDifferentName = IntStream.range(0, 5)
                .mapToObj(i -> createAndSaveNewInstance(true, "testName" + i, "instance" + i, null))
                .collect(Collectors.toList());

        InstanceBoundary[] allInstances = restTemplate.getForObject(
                instancesUrl + "/" + SEARCH_PATH +
                        "/byName/{name}?userDomain={userDomain}&userEmail={userEmail}",
                InstanceBoundary[].class,
                aUniqueStringToLookFor,
                playerDomain,
                playerEmail
        );

        assertThat(allInstances)
                .isNotNull()
                .containsAll(shouldBeFound)
                .doesNotContainAnyElementsOf(shouldNotBeFoundBecauseNotActive)
                .doesNotContainAnyElementsOf(shouldNotBeFoundBecauseDifferentName);
    }

    @Test
    public void testSearchInstancesByType() {
        List<InstanceBoundary> shouldBeFound = IntStream.range(0, 5)
                .mapToObj(i -> createAndSaveNewInstance(false, "testInstance" + i, aUniqueStringToLookFor, null))
                .collect(Collectors.toList());

        List<InstanceBoundary> shouldNotBeFound = IntStream.range(0, 5)
                .mapToObj(i -> createAndSaveNewInstance(false, "testInstance" + i, "testType" + i, null))
                .collect(Collectors.toList());

        InstanceBoundary[] allInstances = restTemplate.getForObject(
                instancesUrl + "/" + SEARCH_PATH +
                        "/byType/{type}",
                InstanceBoundary[].class,
                aUniqueStringToLookFor
        );

        assertThat(allInstances)
                .isNotNull()
                .containsAll(shouldBeFound)
                .doesNotContainAnyElementsOf(shouldNotBeFound);
    }

    @Test
    public void testSearchActiveInstancesByType() {
        UserBoundary player = createAndSaveNewUser("PLAYER");
        String playerDomain = player.getUserId().getDomain();
        String playerEmail = player.getUserId().getEmail();

        List<InstanceBoundary> shouldBeFound = IntStream.range(0, 5)
                .mapToObj(i -> createAndSaveNewInstance(true, "testInstance" + i, aUniqueStringToLookFor, null))
                .collect(Collectors.toList());

        List<InstanceBoundary> shouldNotBeFoundBecauseNotActive = IntStream.range(0, 2)
                .mapToObj(i -> createAndSaveNewInstance(false, "testInstance" + i, aUniqueStringToLookFor, null))
                .collect(Collectors.toList());

        List<InstanceBoundary> shouldNotBeFoundBecauseDifferentType = IntStream.range(0, 2)
                .mapToObj(i -> createAndSaveNewInstance(true, "testInstance" + i, "testType" + i, null))
                .collect(Collectors.toList());

        InstanceBoundary[] allInstances = restTemplate.getForObject(
                instancesUrl + "/" + SEARCH_PATH +
                        "/byType/{type}?userDomain={userDomain}&userEmail={userEmail}",
                InstanceBoundary[].class,
                aUniqueStringToLookFor,
                playerDomain,
                playerEmail
        );

        assertThat(allInstances)
                .isNotNull()
                .containsAll(shouldBeFound)
                .doesNotContainAnyElementsOf(shouldNotBeFoundBecauseNotActive)
                .doesNotContainAnyElementsOf(shouldNotBeFoundBecauseDifferentType);
    }

    /**
     * @see <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine Formula on Wikipedia</a>
     */
    double haversine(double lat1, double lon1, double lat2, double lon2) {
        // distance between latitudes and longitudes
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
    }

    @Test
    public void testSearchInstancesByLocation() {
        double lat = 42.0, lng = 13.37;

        LocationBoundary searchedLocation = new LocationBoundary(lat, lng);

        List<InstanceBoundary> shouldBeFound = IntStream.range(0, 5)
                .mapToObj(i -> createAndSaveNewInstance(false,
                                                        "testInstance" + i,
                                                        "testType" + i,
                                                        new LocationBoundary(lat + i, lng - i)))
                .collect(Collectors.toList());

        double distance = shouldBeFound.stream()
                .map(InstanceBoundary::getLocation)
                .mapToDouble(location -> haversine(searchedLocation.getLat(),
                                                   searchedLocation.getLng(),
                                                   location.getLat(),
                                                   location.getLng()))
                .max()
                .orElseThrow(() -> new IllegalStateException("No instances found"));

        List<InstanceBoundary> shouldNotBeFound = IntStream.range(10, 15)
                .mapToObj(i -> createAndSaveNewInstance(false,
                                                        "testInstance" + i,
                                                        "testType" + i,
                                                        new LocationBoundary(lat + i, lng - i)))
                .collect(Collectors.toList());

        InstanceBoundary[] allInstances = restTemplate.getForObject(
                instancesUrl + "/" + SEARCH_PATH +
                        "/near/{lat}/{lng}/{distance}",
                InstanceBoundary[].class,
                searchedLocation.getLat(),
                searchedLocation.getLng(),
                distance
        );

        assertThat(allInstances)
                .isNotNull()
                .containsAll(shouldBeFound)
                .doesNotContainAnyElementsOf(shouldNotBeFound);
    }

    @Test
    public void testSearchActiveInstancesByLocation() {
        UserBoundary player = createAndSaveNewUser("PLAYER");
        String playerDomain = player.getUserId().getDomain();
        String playerEmail = player.getUserId().getEmail();

        double lat = 42.0, lng = 13.37;

        LocationBoundary searchedLocation = new LocationBoundary(lat, lng);

        List<InstanceBoundary> shouldBeFound = IntStream.range(0, 5)
                .mapToObj(i -> createAndSaveNewInstance(true,
                                                        "testInstance" + i,
                                                        "testType" + i,
                                                        new LocationBoundary(lat + i, lng - i)))
                .collect(Collectors.toList());

        double distance = shouldBeFound.stream()
                .map(InstanceBoundary::getLocation)
                .mapToDouble(location -> haversine(searchedLocation.getLat(),
                                                   searchedLocation.getLng(),
                                                   location.getLat(),
                                                   location.getLng()))
                .max()
                .orElseThrow(() -> new IllegalStateException("No instances found"));

        List<InstanceBoundary> shouldNotBeFoundBecauseNotActive = IntStream.range(0, 2)
                .mapToObj(i -> createAndSaveNewInstance(false,
                                                        "testInstance" + i,
                                                        "testType" + i,
                                                        new LocationBoundary(lat + i, lng - i)))
                .collect(Collectors.toList());

        List<InstanceBoundary> shouldNotBeFoundBecauseOutsideRange = IntStream.range(10, 12)
                .mapToObj(i -> createAndSaveNewInstance(true,
                                                        "testInstance" + i,
                                                        "testType" + i,
                                                        new LocationBoundary(lat + i, lng - i)))
                .collect(Collectors.toList());

        InstanceBoundary[] allInstances = restTemplate.getForObject(
                instancesUrl + "/" + SEARCH_PATH +
                        "/near/{lat}/{lng}/{distance}?userDomain={userDomain}&userEmail={userEmail}",
                InstanceBoundary[].class,
                searchedLocation.getLat(),
                searchedLocation.getLng(),
                distance,
                playerDomain,
                playerEmail
        );

        assertThat(allInstances)
                .isNotNull()
                .containsAll(shouldBeFound)
                .doesNotContainAnyElementsOf(shouldNotBeFoundBecauseNotActive)
                .doesNotContainAnyElementsOf(shouldNotBeFoundBecauseOutsideRange);
    }


}
