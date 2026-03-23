package com.ecommerce.user_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity // marks class as JPA entity which should be mapped to DB
@Table(name = "users") // specify table in DB
@Getter
@Setter
public class User {

    @Id //primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) //auto increment
    private Long id;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role; // store enum as string in DB(instead of numbers)


}

