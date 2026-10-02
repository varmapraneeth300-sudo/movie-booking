package com.moviebooking.entity;

import com.moviebooking.entity.enums.ScreenStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "screens",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_screen_theatre_name",
                        columnNames = {"theatre_id", "name"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "theatre_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_screen_theatre")
    )
    private Theatre theatre;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ScreenStatus screenStatus = ScreenStatus.ACTIVE;


    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}