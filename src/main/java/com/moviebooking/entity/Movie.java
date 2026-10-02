package com.moviebooking.entity;

import com.moviebooking.entity.enums.Genre;
import com.moviebooking.entity.enums.Language;
import com.moviebooking.enums.MovieStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Language language;

    @Column(nullable = false)
    private LocalDate releaseDate;

    @ElementCollection(targetClass = Genre.class)
    @CollectionTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "genre", nullable = false, length = 30)
    private Set<Genre> genres = new HashSet<>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MovieStatus movieStatus = MovieStatus.ACTIVE;

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