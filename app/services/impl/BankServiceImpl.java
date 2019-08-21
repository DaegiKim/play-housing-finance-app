package services.impl;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.FinanceRuntimeException;
import models.Bank;
import models.Finance;
import org.apache.commons.collections.map.CompositeMap;
import play.libs.Json;
import services.BankService;

import java.util.*;
import java.util.stream.Collectors;

public class BankServiceImpl implements BankService {
    @Override
    public ArrayNode getBankList() {
        List<Bank> banks = Bank.find.all();

        ArrayNode arrayNode = Json.newArray();
        for (Bank bank : banks) {
            ObjectNode objectNode = Json.newObject();

            objectNode.put("id", bank.id);
            objectNode.put("bank", bank.name);

            arrayNode.add(objectNode);
        }

        return arrayNode;
    }

    @Override
    public ObjectNode getMaximumByYearly() {
        List<Bank> banks = Bank.find.all();

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

    @Override
    public ObjectNode getSummaryByYearly() {
        ObjectNode resultNode = Json.newObject();
        resultNode.put("name", "주택금융 공급현황");

        ArrayNode dataArrayNode = Json.newArray();
        resultNode.set("data", dataArrayNode);

        List<Bank> banks = Bank.find.all();

        Map<Integer, Map<String, Long>> map = new TreeMap<>(Integer::compareTo);

        for (Bank bank : banks) {
            Map<Integer, Long> collect = bank.finances.stream().collect(Collectors.groupingBy(x -> x.year, Collectors.summingLong(x -> x.amount)));
            collect.forEach((k,v)-> map.merge(k, new HashMap<String, Long>(){{put(bank.name, v);}}, CompositeMap::new));
        }

        for (Map.Entry<Integer, Map<String, Long>> entry : map.entrySet()) {
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

    @Override
    public ObjectNode getMaxMinByYearly(String name) {
        ObjectNode resultNode = Json.newObject();
        resultNode.put("bank", name);

        Bank bank = Bank.find.query().where().eq("name", name).findOne();

        if(bank==null)
            throw new FinanceRuntimeException(FinanceRuntimeException.ErrorCode.BANK_NOT_FOUND);

        List<Finance> finances = Finance.find.query().where().eq("bank_id", bank.id).findList();

        Map<Integer, LongSummaryStatistics> collect = finances.stream().collect(
                Collectors.groupingBy(
                        x -> x.year,
                        Collectors.summarizingLong(x -> x.amount)
                )
        );

        List<Map.Entry<Integer, LongSummaryStatistics>> sorted = collect.entrySet().stream().filter(x->x.getKey()<2017).sorted(Comparator.comparingDouble(x -> x.getValue().getAverage())).collect(Collectors.toList());

        Map.Entry<Integer, LongSummaryStatistics> min = sorted.get(0);
        Map.Entry<Integer, LongSummaryStatistics> max = sorted.get(sorted.size() - 1);

        ArrayNode supportAmount = Json.newArray();

        supportAmount.add(
                Json.newObject()
                        .put("year", min.getKey())
                        .put("amount", Math.round(min.getValue().getAverage()))
        );

        supportAmount.add(
                Json.newObject()
                        .put("year", max.getKey())
                        .put("amount", Math.round(max.getValue().getAverage()))
        );

        resultNode.set("support_amount", supportAmount);

        return resultNode;
    }
}
