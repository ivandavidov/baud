package com.idzona.baud;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BaudEntryDay implements Comparable<BaudEntryDay> {
    private LocalDate date;
    private double totalNav;
    private double sharePrice;

    public static final String CSV_HEADER = "Дата,НСА (общо),НСА (дял),Дялове";

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getTotalNav() {
        return totalNav;
    }

    public void setTotalNav(double totalNav) {
        this.totalNav = totalNav;
    }

    public double getSharePrice() {

        return sharePrice;
    }

    public void setSharePrice(double sharePrice) {
        this.sharePrice = sharePrice;
    }

    public double getTotalShares() {
        double totalShares = 0.0d;

        if(totalNav > 0.0d && sharePrice > 0.0d) {
            totalShares = totalNav / sharePrice;
            totalShares *= 10000;
            totalShares = Math.floor(totalShares);
            totalShares /= 10000;
        }

        return totalShares;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaudEntryDay baudEntryDay = (BaudEntryDay) o;

        if (Double.compare(baudEntryDay.totalNav, totalNav) != 0) return false;
        if (Double.compare(baudEntryDay.sharePrice, sharePrice) != 0) return false;
        return date != null ? date.equals(baudEntryDay.date) : baudEntryDay.date == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = date != null ? date.hashCode() : 0;
        temp = Double.doubleToLongBits(totalNav);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(sharePrice);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }


    @Override
    public int compareTo(BaudEntryDay baudEntryDay) {
        if(baudEntryDay == null || this.date == null || baudEntryDay.date == null) {
            return -1;
        }

        return date.compareTo(baudEntryDay.date);
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(4);
        nf.setGroupingUsed(false);

        StringBuilder sb = new StringBuilder();

        sb.append("Date: ").append(date.format(formatter)).append(", ");
        sb.append("Total NAV: ").append(nf.format(totalNav)).append(", ");
        sb.append("Share price: ").append(nf.format(sharePrice)).append(", ");
        sb.append("Total shares: ").append(nf.format(getTotalShares()));

        return sb.toString();
    }

    public String toCsvString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(4);
        nf.setGroupingUsed(false);

        StringBuilder sb = new StringBuilder();

        sb.append(date.format(formatter)).append(",");
        sb.append(nf.format(totalNav)).append(",");
        sb.append(nf.format(sharePrice)).append(",");
        sb.append(nf.format(getTotalShares()));

        return sb.toString();
    }
}
