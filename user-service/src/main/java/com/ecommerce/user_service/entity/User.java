package com.ecommerce.user_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity // marks class as JPA entity  mapped to DB
@Table(name = "users") // specify table in DB
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role; // store enum as string in DB(instead of numbers)


}

