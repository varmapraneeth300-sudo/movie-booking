package com.moviebooking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "booking_seats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_booking_show_seat",
                        columnNames = {"booking_id", "show_seat_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "booking_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_booking_seat_booking")
    )
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "show_seat_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_booking_seat_show_seat")
    )
    private ShowSeat showSeat;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}