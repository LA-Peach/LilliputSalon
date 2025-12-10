package com.LilliputSalon.SalonApp.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Appointment_Reward", schema = "dbo")
public class AppointmentReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AppointmentRewardID")
    private Integer appointmentRewardId;

    @Column(name = "AppointmentID", nullable = false)
    private Integer appointmentId;

    @Column(name = "RewardID", nullable = false)
    private Integer rewardId;

    @Column(name = "PointsRedeemed", nullable = false)
    private Integer pointsRedeemed;

    @Column(name = "DiscountValue", precision = 18, scale = 2)
    private BigDecimal discountValue;
}
