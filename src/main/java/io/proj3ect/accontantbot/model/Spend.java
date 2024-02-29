package io.proj3ect.accontantbot.model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity(name="all_spend")
public class Spend {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private Long Chatid;


    private double spend_money;
    private Timestamp time;



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setChatId(Long id) {
        this.Chatid = id;
    }

    public Long getChatId() {
        return Chatid;
    }



    public double getSpend_money() {
        return spend_money;
    }

    public void setSpend_money(double spend_money) {
        this.spend_money = spend_money;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }
}
