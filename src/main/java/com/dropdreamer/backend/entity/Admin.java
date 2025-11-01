package com.dropdreamer.backend.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "admins", schema = "drop_dreamer")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    public Admin() {}

    public Admin(String email, String name) {
        this.email = email;
        this.name = name;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
