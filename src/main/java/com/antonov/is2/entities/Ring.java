package com.antonov.is2.entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Objects;

@Entity
@Table(name = "rings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ring {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "ring name не может быть пустой")
    @Column(nullable = false)
    @NotNull(message = "ring name не может быть null")
    private String name; //Поле не может быть null, Строка не может быть пустой

    @Positive(message = "Значение поля должно быть больше 0")
    private Long power; //Значение поля должно быть больше 0, Поле может быть null

    @Positive(message = "Значение поля должно быть больше 0")
    private double weight; //Значение поля должно быть больше 0

    @Override
    public String toString() {
        return "Ring {\n" +
                "\tid = " + id + ",\n" +
                "\tname = '" + name + "',\n" +
                "\tpower = " + power + ",\n" +
                "\tweight = " + weight + "\n" +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ring)) return false;
        Ring rin = (Ring) o;
        // Сравниваем по ID, а не по всей объекту
        return Objects.equals(getId(), rin.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}