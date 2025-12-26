package com.antonov.is2.repos;


import com.antonov.is2.entities.Coordinates;
import lombok.NoArgsConstructor;

import javax.ejb.Stateless;

@Stateless
@NoArgsConstructor
public class CoordinatesRepository extends BasicRepository<Coordinates> {
    @Override
    protected Class<Coordinates> getEntityClass() {
        return Coordinates.class;

    }

}