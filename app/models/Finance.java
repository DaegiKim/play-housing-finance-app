package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Table(name = "finance")
@Entity
public class Finance extends Model {
    public static final Finder<Long, Finance> find = new Finder<>(Finance.class);

    @Id
    public Long id;

    public Integer year;
    public Integer month;
    public Long amount;

    @ManyToOne
    @JsonBackReference
    public Bank bank;
}
