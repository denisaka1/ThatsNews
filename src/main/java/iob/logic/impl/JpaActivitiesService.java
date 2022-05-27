package iob.logic.impl;

import iob.boundary.ActivityBoundary;
import iob.boundary.inner.ActivityID;
import iob.data.*;
import iob.data.dao.ActivitiesDao;
import iob.data.dao.InstancesDao;
import iob.data.dao.UsersDao;
import iob.logic.EnhancedActivitiesService;
import iob.logic.exception.EntityNotFoundException;
import iob.logic.exception.InvalidInputException;
import iob.util.ObjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JpaActivitiesService implements EnhancedActivitiesService {

    private final ActivitiesDao activitiesDao;
    private final UsersDao usersDao;            // for input validation
    private final InstancesDao instancesDao;    // for input validation
    private final ObjectConverter converter;
    private final Authorizer authorizer;

    @Value("${spring.application.name}")
    private String domain;

    @Autowired
    public JpaActivitiesService(
            ActivitiesDao activitiesDao,
            UsersDao usersDao,
            InstancesDao instancesDao,
            ObjectConverter converter,
            Authorizer authorizer
    ) {
        this.activitiesDao = activitiesDao;
        this.usersDao = usersDao;
        this.instancesDao = instancesDao;
        this.converter = converter;
        this.authorizer = authorizer;
    }

    // ***************************** Deprecated *****************************

    @Override
    @Deprecated
    public List<ActivityBoundary> getAllActivities() {
        throw new UnsupportedOperationException("Method not supported, use enhanced method instead.");
    }

    @Override
    @Deprecated
    public void deleteAllActivities() {
        throw new UnsupportedOperationException("Method not supported, use enhanced method instead.");
    }

    // ***************************************************************************

    @Override
    @Transactional
    public Object invokeActivity(ActivityBoundary activityBoundary) {
        if (activityBoundary.getInvokedBy() == null || activityBoundary.getInvokedBy().getUserId() == null) {
            throw new InvalidInputException("blank `invokedBy` field");
        }

        UserEntity authorizedUser = authorizer.authorize(
                true,
                activityBoundary.getInvokedBy().getUserId().getDomain(),
                activityBoundary.getInvokedBy().getUserId().getEmail(),
                UserRole.PLAYER
        );

        if (activityBoundary.getType() == null || activityBoundary.getType().isEmpty()) {
            throw new InvalidInputException("blank activity type");
        }

        usersDao
                .findById(converter.toEntity(activityBoundary.getInvokedBy().getUserId()))
                .orElseThrow(() -> new EntityNotFoundException(activityBoundary.getInvokedBy().getUserId().toString()));

        InstanceEntity instance = instancesDao
                .findById(converter.toEntity(activityBoundary.getInstance().getInstanceId()))
                .orElseThrow(() -> new EntityNotFoundException(activityBoundary.getInstance().getInstanceId().toString()));

        if (instance == null || !instance.getActive()) {
            throw new InvalidInputException("Inactive instance was referenced: " + activityBoundary.getInstance().getInstanceId());
        }

        if (activityBoundary.getActivityId() == null) {
            activityBoundary.setActivityId(new ActivityID());
        }
        activityBoundary.getActivityId().setId(UUID.randomUUID().toString());
        activityBoundary.getActivityId().setDomain(domain);
        activityBoundary.setCreatedTimestamp(new Date() /* now */);

        ActivityEntity entity = converter.toEntity(activityBoundary);
        ActivityEntity savedActivityEntity = activitiesDao.save(entity);

        switch (activityBoundary.getType()) {
            case "SAVE_FAVORITE": {

                // check if instance is already in favorites by article's URL
                if (authorizedUser.getFavoriteArticles().stream()
                        .noneMatch(inst -> inst.getName().equals(instance.getName()))
                ) {
                    authorizedUser.getFavoriteArticles().add(instance);
                    usersDao.save(authorizedUser);
                }
                return instance;
            }
            case "REMOVE_FAVORITE": {
                authorizedUser.getFavoriteArticles().remove(instance);
                usersDao.save(authorizedUser);
            }
            case "GET_FAVORITES": {
                return authorizedUser.getFavoriteArticles().stream()
                        .map(converter::toBoundary)
                        .collect(Collectors.toList());
            }
            default:
                break;
        }
        return converter.toBoundary(savedActivityEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityBoundary> getAllActivities(String requesterDomain, String requesterEmail, int size, int page) {
        authorizer.authorize(requesterDomain, requesterEmail, UserRole.ADMIN);

        return activitiesDao
                .findAll(PageRequest.of(page, size, Sort.Direction.DESC, "createdTimestamp", "activityId"))
                .getContent().stream()
                .map(converter::toBoundary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAllActivities(String requesterDomain, String requesterEmail) {
        authorizer.authorize(requesterDomain, requesterEmail, UserRole.ADMIN);
        activitiesDao.deleteAll();
    }
}
