package com.dropdreamer.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users", schema = "drop_dreamer") // ✅ important: schema name matches Neon
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq")
    @SequenceGenerator(name = "users_seq", sequenceName = "users_sr_no_seq", allocationSize = 1)
    private Long sr_no;

    @Column(name = "user_name")
    private String userName;

    public User() {}

    public User(String userName) {
        this.userName = userName;
    }

    public Long getSr_no() {
        return sr_no;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
