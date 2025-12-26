package com.antonov.is2.entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Objects;

@Entity
@Table(name = "magic_cities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MagicCity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "City name не может быть пустым")
    @NotNull(message = "City name не может быть null")
    @Column(nullable = false)
    private String name; //Поле не может быть null, Строка не может быть пустой

    @NotNull(message = "Площадь не может быть null")
    @Positive(message = "Площадь должна быть больше 0")
    @Column(nullable = false)
    private Float area; //Значение поля должно быть больше 0, Поле не может быть null

    @Positive(message = "Значение поля должно быть больше 0")
    private long population; //Значение поля должно быть больше 0

    @Column(name = "establishment_date")
    private java.time.LocalDateTime establishmentDate;

    @Enumerated(EnumType.STRING)
    private BookCreatureType governor; //Поле может быть null

    private Boolean capital; //Поле может быть null

    @Positive(message = "Значение поля должно быть больше 0")
    @Column(name = "population_density")
    private float populationDensity; //Значение поля должно быть больше 0

    @Override
    public String toString() {
        return "MagicCity {\n" +
                "\tid = " + id + ",\n" +
                "\tname = '" + name + "',\n" +
                "\tarea = " + area + ",\n" +
                "\tpopulation = " + population + ",\n" +
                "\testablishmentDate = " + establishmentDate + ",\n" +
                "\tgovernor = " + governor + ",\n" +
                "\tcapital = " + capital + ",\n" +
                "\tpopulationDensity = " + populationDensity + "\n" +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MagicCity)) return false;
        MagicCity magicCity = (MagicCity) o;
        // Сравниваем по ID, а не по всей объекту
        return Objects.equals(getId(), magicCity.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}