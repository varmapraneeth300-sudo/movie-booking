package com.moviebooking.entity;

import com.moviebooking.entity.enums.ShowSeatStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "show_seats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_show_seat",
                        columnNames = {"show_id", "seat_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "show_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_show_seat_show")
    )
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "seat_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_show_seat_seat")
    )
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShowSeatStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private long price;

    @Column
    private LocalDateTime holdExpiresAt;
}