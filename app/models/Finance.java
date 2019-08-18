package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Functions;
import com.opencsv.CSVReader;
import io.ebean.Finder;
import io.ebean.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Table(name = "finance")
@Entity
public class Finance extends Model {
    final static Logger logger = LoggerFactory.getLogger("application");

    public static final Finder<Long, Finance> find = new Finder<>(Finance.class);

    @Id
    public Long id;

    public Integer year;
    public Integer month;

    public Long amount;

    @ManyToOne
    public Bank bank;

    public static void init() {
        logger.debug("Finance: init()");

        try(FileReader fileReader = new FileReader("app/resources/data.csv")) {
            CSVReader csvReader = new CSVReader(fileReader);

            String[] header = csvReader.readNext();

            List<Bank> bankList = new ArrayList<>();

            for (int i = 2; i < header.length; i++) {
                String bankName = header[i].split("[\\(,[0-9]]")[0].trim();
                if(!bankName.isEmpty()) {
                    Bank bank = Bank.find.query().where().eq("name", bankName).findOne();

                    if(bank == null) {
                        bank = new Bank();
                        bank.name = bankName;
                        bank.save();
                    }

                    bankList.add(bank);
                }
            }

            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                for (int i = 2; i < nextRecord.length; i++) {
                    Finance finance = new Finance();
                    finance.year = Integer.valueOf(nextRecord[0]);
                    finance.month = Integer.valueOf(nextRecord[1]);

                    nextRecord[i] = nextRecord[i].replace(",", "").trim();
                    if(!nextRecord[i].isEmpty()) {
                        finance.amount = Long.parseLong(nextRecord[i]);
                        finance.bank = bankList.get(i-2);
                        finance.save();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ObjectNode getSummaryByYearly() {
        ObjectNode resultNode = Json.newObject();
        resultNode.put("name", "주택금융 공급현황");

        ArrayNode dataArrayNode = Json.newArray();
        resultNode.set("data", dataArrayNode);

        Map<Integer, Map<String, Long>> collect = find.all()
                .stream()
                .collect(Collectors.groupingBy(
                        x -> x.year,
                        Collectors.groupingBy(
                                x -> x.bank.name,
                                Collectors.summingLong(
                                        x -> x.amount
                                )
                        )
                        )
                );

        for (Map.Entry<Integer, Map<String, Long>> entry : collect.entrySet()) {
            ObjectNode yearNode = Json.newObject();
            ObjectNode detailAmount = Json.newObject();

            yearNode.put("year", entry.getKey()+"년");

            Map<String, Long> yearMap = entry.getValue();

            long sum = yearMap.values().stream().mapToLong(x->x).sum();

            yearNode.put("total_amount", sum);
            yearMap.forEach(detailAmount::put);
            yearNode.set("detail_amount", detailAmount);

            dataArrayNode.add(yearNode);
        }


        return resultNode;
    }
}
