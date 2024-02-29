package io.proj3ect.accontantbot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.sql.Timestamp;

@Entity(name="all_earnsss")
public class Earn {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private int Id;

    private Long Chatid;
    private double spend_money;
    private Timestamp time;

    public void setId(int id) {
        this.Id = id;
    }

    public int getId() {
        return Id;
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


    public Long getChatid() {
        return Chatid;
    }

    public void setChatid(Long chatid) {
        Chatid = chatid;
    }
}
