package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.signaflo.timeseries.TimeSeries;
import com.github.signaflo.timeseries.Ts;
import com.github.signaflo.timeseries.forecast.Forecast;
import com.github.signaflo.timeseries.model.arima.Arima;
import com.github.signaflo.timeseries.model.arima.ArimaOrder;
import com.opencsv.CSVReader;
import exceptions.FinanceRuntimeException;
import io.ebean.Finder;
import io.ebean.Model;
import play.libs.Json;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public static boolean init() {
        List<Finance> finances = Finance.find.all();
        if(finances.size() > 0)
            return false;

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
            throw new FinanceRuntimeException(FinanceRuntimeException.ErrorCode.CSV_IO_EXCEPTION);
        }

        return true;
    }

    public static JsonNode getForecast(String bankName, int month) {
        if(bankName == null || bankName.isEmpty() || month < 1 || month > 12)
            throw new FinanceRuntimeException(FinanceRuntimeException.ErrorCode.INVALID_PARAMETER);

        Bank bank = Bank.find.query().where().eq("name", bankName).findOne();

        if(bank==null)
            throw new FinanceRuntimeException(FinanceRuntimeException.ErrorCode.BANK_NOT_FOUND);

        List<Finance> finances = Finance.find.query().where().eq("bank_id", bank.id).findList();

        double[] financeArray = finances.stream().mapToDouble(x -> Double.valueOf(x.amount)).toArray();

        TimeSeries timeSeries = Ts.newMonthlySeries(2005,1, financeArray);

        ArimaOrder modelOrder = ArimaOrder.order(0, 1, 1, 0, 1, 1);

        Arima model = Arima.model(timeSeries, modelOrder);

        Forecast forecast = model.forecast(14);
        List<Double> forecastList = forecast.pointEstimates().asList();

        return Json.newObject()
                .put("bank", bank.id)
                .put("year", 2018)
                .put("month", month)
                .put("amount", forecastList.get(1+month).intValue());
    }
}
