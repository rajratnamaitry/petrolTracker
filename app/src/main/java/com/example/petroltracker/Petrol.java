package com.example.petroltracker;


import android.icu.text.SimpleDateFormat;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;

public class Petrol {
    private String id;
    public String totalKm,liter,amount,date,meter,petrolPoint,rate,preRate,avg,days;

    public Petrol(QueryDocumentSnapshot document){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy");
        Date endDate = document.getTimestamp("date").toDate();
        this.id = document.getId();
        this.meter = document.get("meter").toString();
        this.liter = document.get("liter").toString();
        this.amount = document.get("amount").toString();
        this.date = simpleDateFormat.format(endDate);
        this.totalKm = document.get("totalKm").toString();

        this.petrolPoint = document.get("petrolPoint").toString();
        this.rate       = String.format("%.2f",document.get("rate"));
        this.preRate    = String.format("%.2f",document.get("preRate"));
        this.avg        = String.format("%.2f",document.get("avg"));
        this.days       = document.get("days").toString();
    }

    public String getId() {
        return id;
    }

    public String getMeter() {   return meter; }

    public String getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getLiter() {
        return liter;
    }

    public String getPetrolPoint() {
        return petrolPoint;
    }

    public String getPreRate() {   return preRate; }

    public String getAvg() {   return avg; }

    public String getDays() {   return days; }

    public String getTotalKm() {   return totalKm; }

    public String getRate() {   return rate; }

    public void setId(String id) { this.id = id; }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setLiter(String liter) {
        this.liter = liter;
    }

    public void setTotalKm(String totalKm) {
        this.totalKm = totalKm;
    }

    public void setMeter(String meter) {
        this.meter = meter;
    }

    public void setPetrolPoint(String param) {
        this.petrolPoint = param;
    }

    public void setRate(String param) {
        this.rate = param;
    }

    public void setPreRate(String param) {
        this.preRate = param;
    }

    public void setDays(String param) {
        this.days = param;
    }

    public void setAvg(String param) {
        this.avg = param;
    }}
