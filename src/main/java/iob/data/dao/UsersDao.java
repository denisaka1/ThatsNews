package iob.data.dao;

import iob.data.UserEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UsersDao extends PagingAndSortingRepository<UserEntity, String> {}
