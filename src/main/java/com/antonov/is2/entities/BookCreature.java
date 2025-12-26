package com.antonov.is2.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

@Entity
@Table(name = "book_creatures")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookCreature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически

    @NotBlank(message = "name не может быть пустым")
    @Column(nullable = false)
    @NotNull(message = "name не может быть null ")
    private String name; //Поле не может быть null, Строка не может быть пустой

    @NotNull(message = "coordinates не могут быть null")
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "coordinates_id")
    private Coordinates coordinates; //Поле не может быть null

    @NotNull(message = "creationDate не может быть null")
    @Column(name = "creation_date", nullable = false, updatable = false)
    private java.time.LocalDate creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически

    @Column(nullable = false)
    @NotNull(message = "age не может быть null")
    @Positive(message = "age должен быть больше 0")
    private Long age; //Значение поля должно быть больше 0, Поле не может быть null

    @Enumerated(EnumType.STRING)
    @Column(name = "creature_type")
    private BookCreatureType creatureType; //Поле может быть null

//    @ManyToOne(cascade = CascadeType.ALL)
    @ManyToOne
    @JoinColumn(name = "magic_city_id")
    private MagicCity creatureLocation; //Поле может быть null

    @Positive(message = "attackLevel должен быть больше 0")
    @Column(name = "attack_level")
    private long attackLevel; //Значение поля должно быть больше 0

    @OneToOne
    @JoinColumn(name = "ring_id")
    private Ring ring; //Поле может быть null

    @PrePersist
    protected void onCreate() {
        creationDate = LocalDate.now();
    }

    @Override
    public String toString() {
        return "BookCreature{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates.toString() +
                ", creationDate=" + creationDate +
                ", age=" + age +
                ", creatureType=" + creatureType +
                ", creatureLocation=" + creatureLocation.toString() +
                ", attackLevel=" + attackLevel +
                ", ring=" + ring.toString() +
                '}';
    }
}