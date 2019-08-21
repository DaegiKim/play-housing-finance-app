package services;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface FinanceService {
    boolean init();
    ObjectNode getForecast(String bankName, int month);
}
