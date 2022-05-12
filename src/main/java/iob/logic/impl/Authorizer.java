package iob.logic.impl;

import iob.data.UserEntity;
import iob.data.UserRole;
import iob.data.dao.UsersDao;
import iob.logic.exception.EntityNotFoundException;
import iob.logic.exception.UnauthorizedException;
import iob.util.ObjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Component
public class Authorizer {

    private final UsersDao usersDao;
    private final ObjectConverter converter;

    @Autowired
    public Authorizer(UsersDao usersDao, ObjectConverter converter) {
        this.usersDao = usersDao;
        this.converter = converter;
    }

    /**
     * Checks if user is authorized to perform action.
     * @param requesterDomain - domain of requester
     * @param requesterEmail - email of requester
     * @param authorizedRoles - authorized roles for calling action
     * @return UserEntity if user is authorized, otherwise if no user is passed and action can be performed by default - null.
     * @throws UnauthorizedException if user is not authorized
     * @throws EntityNotFoundException if there is no such user in the database
     */
    @Transactional(readOnly = true)
    public UserEntity authorize(String requesterDomain, String requesterEmail, UserRole... authorizedRoles) {
        return authorize(false, requesterDomain, requesterEmail, authorizedRoles);
    }

    /**
     * Checks if user is authorized to perform action.
     * @param userRequired - indicates that an actual user that is persisted in the database required for the action.
     * @param requesterDomain - domain of requester
     * @param requesterEmail - email of requester
     * @param authorizedRoles - authorized roles for calling action
     * @return UserEntity if user is authorized, otherwise if no user is passed and action can be performed by default - null.
     * @throws UnauthorizedException if user is not authorized
     * @throws EntityNotFoundException if there is no such user in the database
     */
    @Transactional(readOnly = true)
    public UserEntity authorize(boolean userRequired, String requesterDomain, String requesterEmail, UserRole... authorizedRoles) {
        // since it's in the specification that user parameters are optional, only check if they are actually passed.
        if (!userRequired && requesterDomain == null && requesterEmail == null) {
            return null;
        }

        UserEntity requester = usersDao
                .findById(converter.toEntity(requesterDomain, requesterEmail))
                .orElseThrow(() -> new EntityNotFoundException("no such user", requesterDomain, requesterEmail));

        for (UserRole authorizedRole : authorizedRoles) {
            if (requester.getRole() == authorizedRole) {
                return requester;
            }
        }

        throw new UnauthorizedException(
                String.format(
                        "Role mismatch for user [ %s/%s ]: one of these roles %s is required, while user is [ %s ]",
                        requesterDomain,
                        requesterEmail,
                        Arrays.toString(authorizedRoles),
                        requester.getRole()
                )
        );
    }

}
