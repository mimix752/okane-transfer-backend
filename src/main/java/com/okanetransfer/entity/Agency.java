package com.okanetransfer.entity;

import lombok.Data;

import jakarta.persistence.*;

@Data
@Entity
@Table(name = "agencies")
public class Agency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;
    private String city;
    private String country;
    private String phone;
    private boolean active = true;
}
