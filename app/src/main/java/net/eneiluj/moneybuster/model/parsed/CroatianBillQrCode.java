package net.eneiluj.moneybuster.model.parsed;

import java.time.LocalDateTime;
import java.util.Objects;

public class CroatianBillQrCode {
    private LocalDateTime date;
    private double amount;

    public CroatianBillQrCode(LocalDateTime date, double amount) {
        this.date = date;
        this.amount = amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CroatianBillQrCode that = (CroatianBillQrCode) o;
        return Double.compare(that.amount, amount) == 0 && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, amount);
    }

    @Override
    public String toString() {
        return "CroatianBillQrCode{" +
                "date=" + date +
                ", amount=" + amount +
                '}';
    }
}
