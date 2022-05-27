package iob.controller;

import iob.boundary.*;
import iob.logic.EnhancedInstancesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static iob.controller.ControllerConstants.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @see <a href="https://drive.google.com/file/d/1IEDICBpfOxERCd3FKGwqzGWsJwF2-Ezi/view">Specification</a>
 * @see <a href="https://drive.google.com/file/d/1PnDtyr2uDuNh6P9XWJpFFcqa6RmSme-Q/view">Specification Update</a>
 */
@RestController
public class InstancesController {
    private final EnhancedInstancesService instancesService;

    @Autowired
    public InstancesController(EnhancedInstancesService instancesService) {
        this.instancesService = instancesService;
    }

    @RequestMapping(
            method = RequestMethod.GET,
            path = "/" + IOB_PATH +
                    "/" + INSTANCES_PATH,
            produces = APPLICATION_JSON_VALUE
    )
    public InstanceBoundary[] getAllInstances(
            @RequestParam(name = "userDomain", required = false) String userDomain,
            @RequestParam(name = "userEmail", required = false) String userEmail,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page
    ) {
        return instancesService.getAllInstances(userDomain, userEmail, size, page).toArray(new InstanceBoundary[0]);
    }

    @RequestMapping(
            method = RequestMethod.GET,
            path = "/" + IOB_PATH +
                    "/" + INSTANCES_PATH +
                    "/{instanceDomain}/{instanceId}",
            produces = APPLICATION_JSON_VALUE
    )
    public InstanceBoundary getInstance(
            @PathVariable("instanceDomain") String instanceDomain,
            @PathVariable("instanceId") String instanceId,
            @RequestParam(name = "userDomain", required = false) String userDomain,
            @RequestParam(name = "userEmail", required = false) String userEmail
    ) {
        return instancesService.getSpecificInstance(userDomain, userEmail, instanceDomain, instanceId);
    }

    @RequestMapping(
            method = RequestMethod.PUT,
            path = "/" + IOB_PATH +
                    "/" + INSTANCES_PATH +
                    "/{instanceDomain}/{instanceId}",
            consumes = APPLICATION_JSON_VALUE
    )
    public void updateInstance(
            @RequestBody InstanceBoundary instanceDetails,
            @PathVariable("instanceDomain") String instanceDomain,
            @PathVariable("instanceId") String instanceId,
            @RequestParam(name = "userDomain", required = false) String userDomain,
            @RequestParam(name = "userEmail", required = false) String userEmail
    ) {
        instancesService.updateInstance(userDomain, userEmail, instanceDomain, instanceId, instanceDetails);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            path = "/" + IOB_PATH +
                    "/" + INSTANCES_PATH,
            produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_JSON_VALUE
    )
    public InstanceBoundary createInstance(
            @RequestBody InstanceBoundary instanceDetails
    ) {
        return instancesService.createInstance(instanceDetails);
    }

    @RequestMapping(
            method = RequestMethod.GET,
            path = "/" + IOB_PATH +
                    "/" + INSTANCES_PATH +
                    "/" + SEARCH_PATH +
                    "/byName/{name}",
            produces = APPLICATION_JSON_VALUE
    )
    public InstanceBoundary[] getInstancesByName(
            @PathVariable("name") String instanceName,
            @RequestParam(name = "userDomain", required = false) String userDomain,
            @RequestParam(name = "userEmail", required = false) String userEmail,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page
    ) {
        return instancesService.findInstancesByName(userDomain, userEmail, instanceName, size, page).toArray(new InstanceBoundary[0]);
    }

    @RequestMapping(
            method = RequestMethod.GET,
            path = "/" + IOB_PATH +
                    "/" + INSTANCES_PATH +
                    "/" + SEARCH_PATH +
                    "/byType/{type}",
            produces = APPLICATION_JSON_VALUE
    )
    public InstanceBoundary[] getInstancesByType(
            @PathVariable("type") String instanceType,
            @RequestParam(name = "userDomain", required = false) String userDomain,
            @RequestParam(name = "userEmail", required = false) String userEmail,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page
    ) {
        return instancesService.findInstancesByType(userDomain, userEmail, instanceType, size, page).toArray(new InstanceBoundary[0]);
    }

    @RequestMapping(
            method = RequestMethod.GET,
            path = "/" + IOB_PATH +
                    "/" + INSTANCES_PATH +
                    "/" + SEARCH_PATH +
                    "/near/{lat}/{lng}/{distance}",
            produces = APPLICATION_JSON_VALUE
    )
    public InstanceBoundary[] getInstancesByLocation(
            @PathVariable("lat") Double instanceLat,
            @PathVariable("lng") Double instanceLng,
            @PathVariable("distance") Double distance,
            @RequestParam(name = "userDomain", required = false) String userDomain,
            @RequestParam(name = "userEmail", required = false) String userEmail,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page
    ) {
        return instancesService
                .findInstancesInDistance(
                        userDomain,
                        userEmail,
                        instanceLat,
                        instanceLng,
                        distance + 0.001, // add a margin of 1 meter to account for floating point errors.
                        size,
                        page)
                .toArray(new InstanceBoundary[0]);
    }
}
