package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.Finder;
import io.ebean.Model;
import play.libs.Json;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Table(name = "bank")
@Entity
public class Bank extends Model {
    public static Finder<Long, Bank> find = new Finder<>(Bank.class);

    @Id
    public Long id;

    @Column(unique = true)
    public String name;

    @OneToMany
    @JsonIgnore
    public List<Finance> finances;

    public static ArrayNode getBankList() {
        List<Bank> banks = find.all();

        ArrayNode arrayNode = Json.newArray();
        for (Bank bank : banks) {
            ObjectNode objectNode = Json.newObject();

            objectNode.put("id", bank.id);
            objectNode.put("bank", bank.name);

            arrayNode.add(objectNode);
        }

        return arrayNode;
    }

    public static ObjectNode getMaximumByYearly() {
        List<Bank> banks = find.all();

        Map<String, Map.Entry<Integer, Long>> map = new HashMap<>();

        for (Bank bank : banks) {
            List<Finance> finances = bank.finances;

            Map<Integer, Long> yearlyAmountMap = new HashMap<>();

            for (Finance finance : finances) {
                yearlyAmountMap.merge(finance.year, finance.amount, Long::sum);
            }

            Map.Entry<Integer, Long> max = Collections.max(yearlyAmountMap.entrySet(), Comparator.comparing(Map.Entry::getValue));
            map.put(bank.name, max);
        }

        Map.Entry<String, Map.Entry<Integer, Long>> max = Collections.max(map.entrySet(), Comparator.comparing(x -> x.getValue().getValue()));

        ObjectNode result = Json.newObject();
        result.put("year", max.getValue().getKey());
        result.put("bank", max.getKey());

        return result;
    }

    public static ObjectNode getMaxMinByYearly(String name) {
        ObjectNode resultNode = Json.newObject();
        resultNode.put("bank", name);

        Bank bank = Bank.find.query().where().eq("name", name).findOne();

        List<Finance> finances = Finance.find.query().where().eq("bank_id", bank.id).findList();

        Map<Integer, Long> collect = finances.stream().collect(Collectors.groupingBy(x -> x.year, Collectors.summingLong(x -> x.amount)));

        Map.Entry<Integer, Long> max = collect.entrySet().stream().max(Comparator.comparingLong(Map.Entry::getValue)).get();
        Map.Entry<Integer, Long> min = collect.entrySet().stream().min(Comparator.comparingLong(Map.Entry::getValue)).get();

        ArrayNode supportAmount = Json.newArray();
        supportAmount.add(
                Json.newObject()
                        .put("year", max.getKey())
                        .put("amount", BigDecimal.valueOf(max.getValue()).divide(BigDecimal.valueOf(12), 0, BigDecimal.ROUND_HALF_UP))
        );
        supportAmount.add(
                Json.newObject()
                        .put("year", min.getKey())
                        .put("amount", BigDecimal.valueOf(min.getValue()).divide(BigDecimal.valueOf(12), 0, BigDecimal.ROUND_HALF_UP))
        );

        resultNode.set("support_amount", supportAmount);

        return resultNode;
    }
}
