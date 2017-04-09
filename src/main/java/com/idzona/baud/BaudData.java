package com.idzona.baud;

import java.util.Set;

public class BaudData {
    private String fundId;
    private Set<BaudEntryDay> baudEntries;

    public String getFundId() {
        return fundId;
    }

    public void setFundId(String fundId) {
        this.fundId = fundId;
    }

    public Set<BaudEntryDay> getBaudEntries() {
        return baudEntries;
    }

    public void setBaudEntries(Set<BaudEntryDay> baudEntries) {
        this.baudEntries = baudEntries;
    }
}
