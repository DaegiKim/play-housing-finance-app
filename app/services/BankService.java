package services;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface BankService {
    ArrayNode getBankList();
    ObjectNode getMaximumByYearly();
    ObjectNode getSummaryByYearly();
    ObjectNode getMaxMinByYearly(String name);
}
