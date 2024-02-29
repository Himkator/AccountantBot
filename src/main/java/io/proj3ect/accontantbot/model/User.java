package io.proj3ect.accontantbot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.sql.Timestamp;

@Entity(name="UserRep")
public class User {
    @Id
    private Long Chatid;
    private String name;
    private Timestamp time;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public void setId(Long id) {
        this.Chatid = id;
    }

    public Long getId() {
        return Chatid;
    }

}
