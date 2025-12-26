package com.antonov.is2.repos;

import com.antonov.is2.entities.MagicCity;
import com.antonov.is2.entities.BookCreatureType;
import lombok.NoArgsConstructor;

import javax.ejb.Stateless;
import java.util.List;

@Stateless
@NoArgsConstructor
public class MagicCityRepository extends BasicRepository<MagicCity> {
    @Override
    protected Class<MagicCity> getEntityClass() {
        return MagicCity.class;
    }

    /**
     * Находит города, где правителем является эльф
     */
    public List<MagicCity> findCitiesWithElfGovernor() {
        return em.createQuery(
                "SELECT mc FROM MagicCity mc WHERE mc.governor = :governor", MagicCity.class)
                .setParameter("governor", BookCreatureType.ELF)
                .getResultList();
    }
}