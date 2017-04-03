package com.idzona.baud;

import java.util.Set;

public class BaudData {
    private String fond;
    private Set<BaudEntryDay> baudEntries;

    public String getFond() {
        return fond;
    }

    public void setFond(String fond) {
        this.fond = fond;
    }

    public Set<BaudEntryDay> getBaudEntries() {
        return baudEntries;
    }

    public void setBaudEntries(Set<BaudEntryDay> baudEntries) {
        this.baudEntries = baudEntries;
    }
}
