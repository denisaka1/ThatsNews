package iob.data.dao;

import iob.data.InstanceEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InstancesDao extends PagingAndSortingRepository<InstanceEntity, String> {

    List<InstanceEntity> findAllByActiveIsTrue(Pageable pageable);

    Optional<InstanceEntity> findByInstanceIdAndActiveIsTrue(@Param("instanceId") String instanceId);

    List<InstanceEntity> findInstanceEntitiesByName(@Param("name") String name, Pageable pageable);

    List<InstanceEntity> findInstanceEntitiesByNameAndActiveIsTrue(@Param("name") String name, Pageable pageable);

    List<InstanceEntity> findInstanceEntitiesByType(@Param("type") String type, Pageable pageable);

    List<InstanceEntity> findInstanceEntitiesByTypeAndActiveIsTrue(@Param("type") String type, Pageable pageable);

    /**
     * <a href="https://gautamsuraj.medium.com/haversine-formula-for-spring-data-jpa-db6a53516dc9">Haversine formula for spring data jpa</a>
     * <br/><br/>
     * 6371 is the radius of the earth in km.
     * <br/><br/>
     * @see <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine Formula on Wikipedia</a>
     */
    String HAVERSINE_FORMULA = "6371 * acos(cos(radians(:lat)) * cos(radians(ie.lat)) * cos(radians(ie.lng) - radians(:lng)) + sin(radians(:lat)) * sin(radians(ie.lat)))";

    // HQL query, so still should be DB independent, and is still using pagination with passed `pageable`.
    @Query("SELECT ie FROM InstanceEntity ie " +
            "WHERE " + HAVERSINE_FORMULA + " <= :distance " +
            "ORDER BY " + HAVERSINE_FORMULA + " ASC")
    List<InstanceEntity> findInstanceEntitiesInDistance(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("distance") double distance,
            Pageable pageable
    );

    // HQL query, so still should be DB independent, and is still using pagination with passed `pageable`.
    @Query("SELECT ie FROM InstanceEntity ie " +
            "WHERE ie.active = true " +
            "AND " + HAVERSINE_FORMULA + " <= :distance " +
            "ORDER BY " + HAVERSINE_FORMULA + " ASC")
    List<InstanceEntity> findInstanceEntitiesInDistanceAndActiveIsTrue(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("distance") double distance,
            Pageable pageable
    );

}