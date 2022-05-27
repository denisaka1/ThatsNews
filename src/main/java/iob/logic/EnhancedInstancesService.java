package iob.logic;

import iob.boundary.InstanceBoundary;

import java.util.Collection;
import java.util.List;

public interface EnhancedInstancesService extends InstancesService {

    InstanceBoundary updateInstance(
            String requesterDomain,
            String requesterEmail,
            String instanceDomain,
            String instanceId,
            InstanceBoundary update
    );

    InstanceBoundary getSpecificInstance(
            String requesterDomain,
            String requesterEmail,
            String instanceDomain,
            String instanceId
    );

    List<InstanceBoundary> getAllInstances(
            String requesterDomain,
            String requesterEmail,
            int size,
            int page
    );

    void deleteAllInstances(
            String requesterDomain,
            String requesterEmail
    );

    List<InstanceBoundary> findInstancesByName(
            String requesterDomain,
            String requesterEmail,
            String name,
            int size,
            int page
    );

    List<InstanceBoundary> findInstancesByType(
            String requesterDomain,
            String requesterEmail,
            String type,
            int size,
            int page
    );

    List<InstanceBoundary> findInstancesInDistance(
            String requesterDomain,
            String requesterEmail,
            double lat,
            double lng,
            double distance,
            int size,
            int page
    );

}
