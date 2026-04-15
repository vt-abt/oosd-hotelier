package com.example.demo;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Room {
    public enum Type {
        DELUXE(2500, 2300, "#008080"),
        SUPER_DELUXE(3600, 3400, "#4169E1"),
        ALSACE_SUITE(6500, 6500, "#800080");

        public final double base, pre;
        public final String heatColor;
        Type(double b, double p, String color) { this.base = b; this.pre = p; this.heatColor = color; }
    }

    public enum Status { VACANT, OCCUPIED, PREBOOKED, MAINTENANCE }

    private final IntegerProperty number;
    private final ObjectProperty<Type> type;
    private final ObjectProperty<Status> status;
    private final BooleanProperty needsCleaning = new SimpleBooleanProperty(false);
    private final List<Booking> bookings = new ArrayList<>();

    public Room(int num, Type t) {
        this.number = new SimpleIntegerProperty(num);
        this.type = new SimpleObjectProperty<>(t);
        this.status = new SimpleObjectProperty<>(Status.VACANT);
    }

    public boolean isAvailable(LocalDate start, LocalDate end) {
        if (status.get() == Status.MAINTENANCE) return false;
        if (start.equals(LocalDate.now()) && needsCleaning.get()) return false; // Prevent booking dirty rooms

        for (Booking b : bookings) {
            if (!b.isCheckoutCompleted()) {
                if (start.isBefore(b.getCheckOut()) && end.isAfter(b.getCheckIn())) return false;
            }
        }
        return true;
    }
    public Booking getActiveBooking() {
        return bookings.stream()
                .filter(b -> !b.isCheckoutCompleted() && !LocalDate.now().isBefore(b.getCheckIn()))
                .findFirst()
                .orElse(null);
    }
    public boolean isOverstaying() {
        Booking active = getActiveBooking();
        return active != null && LocalDate.now().isAfter(active.getCheckOut()) && status.get() == Status.OCCUPIED;
    }
    public void processCheckout() {
        Booking active = getActiveBooking();
        if (active != null) {
            active.setCheckoutCompleted(true);
            active.setCheckOut(LocalDate.now());
        }
        this.setStatus(Status.VACANT);
        this.setNeedsCleaning(true);
    }

    public void addBooking(Booking b) { bookings.add(b); }
    public List<Booking> getBookings() { return bookings; }
    public int getNumber() { return number.get(); }
    public Type getType() { return type.get(); }
    public Status getStatus() { return status.get(); }
    public void setStatus(Status s) { this.status.set(s); }
    public boolean isNeedsCleaning() { return needsCleaning.get(); }
    public void setNeedsCleaning(boolean v) { needsCleaning.set(v); }
    public IntegerProperty numberProperty() { return number; }
    public ObjectProperty<Type> typeProperty() { return type; }
    public ObjectProperty<Status> statusProperty() { return status; }
    public BooleanProperty needsCleaningProperty() { return needsCleaning; }
}

class Booking {
    private final String name, idCard, phone;
    private final LocalDate checkIn;
    private LocalDate checkOut;
    private final double totalAmt;
    private final int guests;
    private boolean checkoutCompleted;

    public Booking(String name, String idCard, String phone, LocalDate in, LocalDate out, int guests, double rate) {
        this.name = name;
        this.idCard = idCard;
        this.phone = phone;
        this.checkIn = in;
        this.checkOut = out;
        this.guests = guests;
        this.checkoutCompleted = false;

        long nights = Math.max(1, ChronoUnit.DAYS.between(in, out));
        this.totalAmt = nights * rate;
    }

    public String getName() { return name; }
    public String getIdCard() { return idCard; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate out) { this.checkOut = out; }
    public double getTotalAmt() { return totalAmt; }
    public boolean isCheckoutCompleted() { return checkoutCompleted; }
    public void setCheckoutCompleted(boolean v) { this.checkoutCompleted = v; }
}