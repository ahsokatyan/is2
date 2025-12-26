package com.antonov.is2.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "coordinates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Coordinates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DecimalMin(value = "-999.0", inclusive = false, message = "X должен быть больше -999")
    private double x; //Значение поля должно быть больше -999

    @NotNull(message = "Y не может быть null")
    @Min(value = -934, message = "Y должен быть больше -934")
    private Long y; //Значение поля должно быть больше -934, Поле не может быть null

    @Override
    public String toString() {
        return "Coordinates{" +
                "id=" + id +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}