package com.LilliputSalon.SalonApp.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "[User_Type]")
@Getter @Setter
public class UserType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserTypeID")
    private Long id;

    @Column(name = "TypeName", nullable = false, length = 50)
    private String typeName;
}