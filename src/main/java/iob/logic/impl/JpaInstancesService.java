package iob.logic.impl;

import iob.boundary.InstanceBoundary;
import iob.boundary.inner.InstanceID;
import iob.data.*;
import iob.data.dao.InstancesDao;
import iob.logic.EnhancedInstancesService;
import iob.logic.exception.EntityNotFoundException;
import iob.logic.exception.InvalidInputException;
import iob.util.ObjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JpaInstancesService implements EnhancedInstancesService {

    private final InstancesDao instancesDao;
    private final ObjectConverter converter;
    private final Authorizer authorizer;

    private final SimpleDateFormat articleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");


    @Value("${spring.application.name}")
    private String domain;

    @Autowired
    public JpaInstancesService(InstancesDao instancesDao, ObjectConverter converter, Authorizer authorizer) {
        this.instancesDao = instancesDao;
        this.converter = converter;
        this.authorizer = authorizer;
        articleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    // ***************************** Deprecated ***************************

    @Override
    @Deprecated
    public InstanceBoundary updateInstance(String instanceDomain, String instanceId, InstanceBoundary update) {
        throw new UnsupportedOperationException("Method not supported, use enhanced method instead.");
    }

    @Override
    @Deprecated
    public InstanceBoundary getSpecificInstance(String instanceDomain, String instanceId) {
        throw new UnsupportedOperationException("Method not supported, use enhanced method instead.");
    }

    @Override
    @Deprecated
    public List<InstanceBoundary> getAllInstances() {
        throw new UnsupportedOperationException("Method not supported, use enhanced method instead.");
    }

    @Override
    @Deprecated
    public void deleteAllInstances() {
        throw new UnsupportedOperationException("Method not supported, use enhanced method instead.");
    }

    // ***************************************************************************

    @Override
    @Transactional
    public InstanceBoundary createInstance(InstanceBoundary instanceBoundary) {
        if (instanceBoundary.getName() == null || instanceBoundary.getName().isEmpty()) {
            throw new InvalidInputException("blank instance name");
        }
        if (instanceBoundary.getType() == null || instanceBoundary.getType().isEmpty()) {
            throw new InvalidInputException("blank instance type");
        }
        if (instanceBoundary.getCreatedBy() == null || instanceBoundary.getCreatedBy().getUserId() == null) {
            throw new InvalidInputException("blank `createdBy` field");
        }

        authorizer.authorize(
                true,
                instanceBoundary.getCreatedBy().getUserId().getDomain(),
                instanceBoundary.getCreatedBy().getUserId().getEmail(),
                UserRole.MANAGER
        );

        // if article already exists, return it instead of recreating it
        if (instanceBoundary.getType().equals("article")) {
            InstanceBoundary existingArticle = getArticleIfExists(instanceBoundary);
            if (existingArticle != null) return existingArticle;
        }

        if (instanceBoundary.getInstanceId() == null) {
            instanceBoundary.setInstanceId(new InstanceID());
        }
        instanceBoundary.getInstanceId().setId(UUID.randomUUID().toString());
        instanceBoundary.getInstanceId().setDomain(domain);
        instanceBoundary.setCreatedTimestamp(new Date() /* now */);

        InstanceEntity entity = converter.toEntity(instanceBoundary);

        return converter.toBoundary(instancesDao.save(entity));
    }

    private String getOrNull(Object obj) {
        return (obj == null || obj.toString().isEmpty()) ? null : obj.toString();
    }

    private InstanceBoundary getArticleIfExists(InstanceBoundary instanceBoundary) {
        if (instanceBoundary.getInstanceAttributes() == null) {
            throw new InvalidInputException("null instance attributes");
        }
        if (!instanceBoundary.getInstanceAttributes().containsKey("theArticle")) {
            throw new InvalidInputException("instance attributes is missing `theArticle` field");
        }

        Map<String, Object> theArticleAsMap = (Map<String, Object>) instanceBoundary.getInstanceAttributes().get("theArticle");

        if (theArticleAsMap == null) {
            throw new InvalidInputException("`theArticle` field value is null");
        }

        Map<String, Object> articleSourceAsMap = (Map<String, Object>) theArticleAsMap.get("source");

        Article.ArticleSource articleSource = null;
        if (articleSourceAsMap != null) {
            articleSource = new Article.ArticleSource(
                    getOrNull(articleSourceAsMap.get("id")),
                    getOrNull(articleSourceAsMap.get("name"))
            );
        }

        Date publishedAt;

        if (theArticleAsMap.containsKey("publishedAt")) {
            try {
                publishedAt = articleDateFormat.parse(theArticleAsMap.get("publishedAt").toString());
            } catch (ParseException e) {
                throw new InvalidInputException("invalid `publishedAt` field format, the correct format is: " + articleDateFormat.toPattern());
            }
        } else {
            publishedAt = null;
        }

        Article theArticle = Article
                .builder()
                .author(getOrNull(theArticleAsMap.get("author")))
                .category(Category.valueOf(theArticleAsMap.get("category").toString()))
                .content(getOrNull(theArticleAsMap.get("content")))
                .description(getOrNull(theArticleAsMap.get("description")))
                .publishedAt(publishedAt)
                .title(getOrNull(theArticleAsMap.get("title")))
                .source(articleSource)
                .url(getOrNull(theArticleAsMap.get("url")))
                .urlToImage(getOrNull(theArticleAsMap.get("urlToImage")))
                .build();

        String articleUrl = theArticle.getUrl();

        if (!articleUrl.equals(instanceBoundary.getName())) {
            throw new InvalidInputException("instance's name and article's url must match");
        }

        List<InstanceBoundary> articlesWithSameUrl = findInstancesByName(
                instanceBoundary.getCreatedBy().getUserId().getDomain(),
                instanceBoundary.getCreatedBy().getUserId().getEmail(),
                articleUrl,
                1,
                0
        );

        return articlesWithSameUrl.size() > 0 ? articlesWithSameUrl.get(0) : null;
    }

    @Override
    @Transactional
    public InstanceBoundary updateInstance(
            String requesterDomain,
            String requesterEmail,
            String instanceDomain,
            String instanceId,
            InstanceBoundary update
    ) {
        authorizer.authorize(requesterDomain, requesterEmail, UserRole.MANAGER);

        if (update.getInstanceId() != null) { // only if instanceId is not null then check integrity
            if (update.getInstanceId().getDomain() != null // only if instanceId.domain is not null then check integrity
                    && !update.getInstanceId().getDomain().equals(instanceDomain)
            ) {
                throw new InvalidInputException("Updating an instance's domain is forbidden");
            }
            if (update.getInstanceId().getId() != null // only if instanceId.id is not null then check integrity
                    && !update.getInstanceId().getId().equals(instanceId)
            ) {
                throw new InvalidInputException("Updating an instance's id is forbidden");
            }
        }

        InstanceEntity entity = instancesDao
                .findById(converter.toEntity(instanceDomain, instanceId))
                .orElseThrow(() -> new EntityNotFoundException("no such instance", instanceDomain, instanceId));

        if (update.getCreatedBy() != null // only if createdBy is not null then check integrity
                && !converter.toEntity(update.getCreatedBy()).equals(entity.getCreatedBy())
        ) {
            throw new InvalidInputException("Updating an instance's `createdBy` property is forbidden");
        }

        if (update.getCreatedTimestamp() != null // only if createdTimestamp is not null then check integrity
                && !update.getCreatedTimestamp().equals(entity.getCreatedTimestamp())
        ) {
            throw new InvalidInputException("Updating an instance's `createdTimestamp` property is forbidden");
        }


        if (update.getName() != null) {
            entity.setName(update.getName());
        }
        if (update.getType() != null) {
            entity.setType(update.getType());
        }
        if (update.getActive() != null) {
            entity.setActive(update.getActive());
        }
        if (update.getLocation() != null) {
            if (update.getLocation().getLat() != null) {
                entity.setLat(update.getLocation().getLat());
            }
            if (update.getLocation().getLng() != null) {
                entity.setLng(update.getLocation().getLng());
            }
        }
        if (update.getInstanceAttributes() != null) {
            Map<String, Object> attrs = converter.jsonStringToMap(entity.getInstanceAttributes());
            if (attrs == null) attrs = new HashMap<>();
            attrs.putAll(update.getInstanceAttributes());
            entity.setInstanceAttributes(converter.mapToJsonString(attrs));
        }

        return converter.toBoundary(instancesDao.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public InstanceBoundary getSpecificInstance(
            String requesterDomain,
            String requesterEmail,
            String instanceDomain,
            String instanceId
    ) {
        UserEntity authorized = authorizer.authorize(requesterDomain, requesterEmail, UserRole.MANAGER, UserRole.PLAYER);

        Optional<InstanceEntity> optionalInstance =
                authorized == null || authorized.getRole() == UserRole.MANAGER
                        ? instancesDao.findById(converter.toEntity(instanceDomain, instanceId))
                        : instancesDao.findByInstanceIdAndActiveIsTrue(converter.toEntity(instanceDomain, instanceId));

        return converter.toBoundary(
                optionalInstance.orElseThrow(() -> new EntityNotFoundException("no such instance", instanceDomain, instanceId))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstanceBoundary> getAllInstances(String requesterDomain, String requesterEmail, int size, int page) {
        UserEntity authorizedRequester = authorizer.authorize(
                requesterDomain,
                requesterEmail,
                UserRole.PLAYER,
                UserRole.MANAGER
        );

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdTimestamp", "instanceId");

        List<InstanceEntity> instances =
                authorizedRequester == null || authorizedRequester.getRole() == UserRole.MANAGER
                        ? instancesDao.findAll(pageable).getContent()
                        : instancesDao.findAllByActiveIsTrue(pageable);

        return instances.stream()
                .map(converter::toBoundary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAllInstances(String requesterDomain, String requesterEmail) {
        authorizer.authorize(requesterDomain, requesterEmail, UserRole.ADMIN);
        instancesDao.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstanceBoundary> findInstancesByName(String requesterDomain, String requesterEmail, String name, int size, int page) {
        UserEntity authorizedRequester = authorizer.authorize(
                requesterDomain,
                requesterEmail,
                UserRole.PLAYER,
                UserRole.MANAGER
        );

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdTimestamp", "instanceId");

        List<InstanceEntity> instances =
                authorizedRequester == null || authorizedRequester.getRole() == UserRole.MANAGER
                        ? instancesDao.findInstanceEntitiesByName(name, pageable)
                        : instancesDao.findInstanceEntitiesByNameAndActiveIsTrue(name, pageable);

        return instances.stream()
                .map(converter::toBoundary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstanceBoundary> findInstancesByType(String requesterDomain, String requesterEmail, String type, int size, int page) {
        UserEntity authorizedRequester = authorizer.authorize(
                requesterDomain,
                requesterEmail,
                UserRole.PLAYER,
                UserRole.MANAGER
        );

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdTimestamp", "instanceId");

        List<InstanceEntity> instances =
                authorizedRequester == null || authorizedRequester.getRole() == UserRole.MANAGER
                        ? instancesDao.findInstanceEntitiesByType(type, pageable)
                        : instancesDao.findInstanceEntitiesByTypeAndActiveIsTrue(type, pageable);

        return instances.stream()
                .map(converter::toBoundary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstanceBoundary> findInstancesInDistance(
            String requesterDomain,
            String requesterEmail,
            double lat,
            double lng,
            double distance,
            int size,
            int page
    ) {
        UserEntity authorizedRequester = authorizer.authorize(
                requesterDomain,
                requesterEmail,
                UserRole.PLAYER,
                UserRole.MANAGER
        );

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdTimestamp", "instanceId");

        List<InstanceEntity> instances =
                authorizedRequester == null || authorizedRequester.getRole() == UserRole.MANAGER
                        ? instancesDao.findInstanceEntitiesInDistance(lat, lng, distance, pageable)
                        : instancesDao.findInstanceEntitiesInDistanceAndActiveIsTrue(lat, lng, distance, pageable);

        return instances.stream()
                .map(converter::toBoundary)
                .collect(Collectors.toList());
    }

}