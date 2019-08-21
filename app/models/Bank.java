package models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Table(name = "bank")
@Entity
public class Bank extends Model {
    public static Finder<Long, Bank> find = new Finder<>(Bank.class);

    @Id
    public Long id;

    @Column(unique = true)
    public String name;

    @OneToMany
    @JsonManagedReference
    public List<Finance> finances;
}
