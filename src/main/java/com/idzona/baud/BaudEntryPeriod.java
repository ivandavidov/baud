package com.idzona.baud;

public class BaudEntryPeriod implements Comparable<BaudEntryPeriod> {
    private BaudEntryDay baudBegin;
    private BaudEntryDay baudEnd;

    public static final String CSV_HEADER = "Дата - начало,НСА (общо) - начало,НСА (дял) - начало,Дялове - начало,Дата - край,НСА (общо) - край,НСА (дял) - край,Дялове - край";

    public BaudEntryDay getBaudBegin() {
        return baudBegin;
    }

    public void setBaudBegin(BaudEntryDay baudBegin) {
        this.baudBegin = baudBegin;
    }

    public BaudEntryDay getBaudEnd() {
        return baudEnd;
    }

    public void setBaudEnd(BaudEntryDay baudEnd) {
        this.baudEnd = baudEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaudEntryPeriod that = (BaudEntryPeriod) o;

        if (!baudBegin.equals(that.baudBegin)) return false;
        return baudEnd.equals(that.baudEnd);
    }

    @Override
    public int hashCode() {
        int result = baudBegin.hashCode();
        result = 31 * result + baudEnd.hashCode();
        return result;
    }

    @Override
    public int compareTo(BaudEntryPeriod baudEntryPeriod) {
        if(baudEntryPeriod == null || this.baudBegin == null || baudEntryPeriod.baudBegin.getDate() == null) {
            return -1;
        }

        return baudBegin.compareTo(baudEntryPeriod.baudBegin);
    }

    public String toCsvString() {
        String begin = baudBegin.toCsvString();
        String end = baudEnd.toCsvString();

        return begin + ',' + end;
    }
}
