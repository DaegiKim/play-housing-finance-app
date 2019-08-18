package models;

import io.ebean.Finder;
import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User extends Model {
    public static final Finder<Long, User> find = new Finder<>(User.class);

    @Id
    public Long id;

    @Constraints.Required
    @Column(unique = true)
    public String username;

    @Constraints.Required
    public String password;

    public static User findByUsername(String usenrame) {
        return find.query().where().eq("username", usenrame).findOne();
    }
}