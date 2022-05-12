package iob.logic;

import iob.boundary.UserBoundary;

import java.util.List;

public interface EnhancedUsersService extends UsersService {

    List<UserBoundary> getAllUsers(String requesterDomain, String requesterEmail, int page, int size);

    void deleteAllUsers(String requesterDomain, String requesterEmail);
}
