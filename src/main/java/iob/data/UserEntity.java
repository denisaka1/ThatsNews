package iob.data;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Collection;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
public class UserEntity {

    @Id
    private String id;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    private String username;
    private String avatar;

    public UserEntity(String id, UserRole role, String username, String avatar) {
        this.id = id;
        this.role = role;
        this.username = username;
        this.avatar = avatar;
    }

    @ManyToMany
    @ToString.Exclude
    private Collection<InstanceEntity> favoriteArticles;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserEntity that = (UserEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (role != null ? role.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        return result;
    }
}
