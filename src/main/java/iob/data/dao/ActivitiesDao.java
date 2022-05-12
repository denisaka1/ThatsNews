package iob.data.dao;

import iob.data.ActivityEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ActivitiesDao extends PagingAndSortingRepository<ActivityEntity, String> {}
