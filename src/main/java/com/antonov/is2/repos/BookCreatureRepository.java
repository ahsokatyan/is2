package com.antonov.is2.repos;

import com.antonov.is2.entities.BookCreature;
import com.antonov.is2.entities.BookCreatureType;
import com.antonov.is2.entities.MagicCity;
import lombok.NoArgsConstructor;

import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Stateless
@NoArgsConstructor
public class BookCreatureRepository extends BasicRepository<BookCreature> {
    @Override
    protected Class<BookCreature> getEntityClass() {
        return BookCreature.class;
    }

    /**
     * Находит существо по ID кольца
     */
    public Optional<BookCreature> findByRingId(Long ringId) {
        List<BookCreature> creatures = findAll();
        return creatures.stream()
                .filter(creature -> creature.getRing() != null &&
                        creature.getRing().getId().equals(ringId))
                .findFirst();
    }

    /**
     * Находит всех существ в указанном городе
     */
    public List<BookCreature> findByCityId(Long cityId) {
        return em.createQuery("SELECT b FROM BookCreature b WHERE b.creatureLocation.id = :cityId", BookCreature.class)
                .setParameter("cityId", cityId)
                .getResultList();
    }

    /**
     * Находит существ без колец
     */
    public List<BookCreature> findCreaturesWithoutRings() {
        List<BookCreature> allCreatures = findAll();
        return allCreatures.stream()
                .filter(creature -> creature.getRing() == null)
                .collect(Collectors.toList());
    }

    /**
     * Находит существ без колец, исключая указанное существо
     */
    public List<BookCreature> findCreaturesWithoutRings(Long excludeCreatureId) {
        List<BookCreature> allCreatures = findAll();
        return allCreatures.stream()
                .filter(creature -> creature.getRing() == null &&
                        !creature.getId().equals(excludeCreatureId))
                .collect(Collectors.toList());
    }

    public List<BookCreature> findByType(BookCreatureType type) {
        if (type == null) {
            // Если тип не указан, возвращаем пустой список
            return new ArrayList<>();
        }

        try {
            return em.createQuery(
                            "SELECT b FROM BookCreature b WHERE b.creatureType = :type", BookCreature.class)
                    .setParameter("type", type)
                    .getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Находит всех существ указанного типа с кольцами
     */
    public List<BookCreature> findByTypeWithRings(BookCreatureType type) {
        if (type == null) {
            return new ArrayList<>();
        }

        try {
            return em.createQuery(
                            "SELECT b FROM BookCreature b WHERE b.creatureType = :type AND b.ring IS NOT NULL", BookCreature.class)
                    .setParameter("type", type)
                    .getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Находит всех существ, проживающих в указанном городе
     */
    public List<BookCreature> findByCity(MagicCity city) {
        if (city == null) {
            return new ArrayList<>();
        }

        try {
            return em.createQuery(
                            "SELECT b FROM BookCreature b WHERE b.creatureLocation = :city", BookCreature.class)
                    .setParameter("city", city)
                    .getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public boolean existsByNameAndType(String name, BookCreatureType type, Long excludeId) {
        String base = "SELECT COUNT(b) FROM BookCreature b WHERE b.name = :name";
        String typeClause = type == null ? " AND b.creatureType IS NULL" : " AND b.creatureType = :type";
        String excludeClause = excludeId == null ? "" : " AND b.id <> :excludeId";
        String jpql = base + typeClause + excludeClause;

        var query = em.createQuery(jpql, Long.class)
                .setParameter("name", name);
        if (type != null) {
            query.setParameter("type", type);
        }
        if (excludeId != null) {
            query.setParameter("excludeId", excludeId);
        }
        return query.getSingleResult() > 0;
    }

    public boolean existsByNameAndCoordinates(String name, double x, Long y, Long excludeId) {
        String base = "SELECT COUNT(b) FROM BookCreature b WHERE b.name = :name"
                + " AND b.coordinates.x = :x AND b.coordinates.y = :y";
        String excludeClause = excludeId == null ? "" : " AND b.id <> :excludeId";
        String jpql = base + excludeClause;

        var query = em.createQuery(jpql, Long.class)
                .setParameter("name", name)
                .setParameter("x", x)
                .setParameter("y", y);
        if (excludeId != null) {
            query.setParameter("excludeId", excludeId);
        }
        return query.getSingleResult() > 0;
    }
}
