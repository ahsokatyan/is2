package com.antonov.is2.repos;

import com.antonov.is2.entities.Ring;
import lombok.NoArgsConstructor;

import javax.ejb.Stateless;
import java.util.List;

@Stateless
@NoArgsConstructor
public class RingRepository extends BasicRepository<Ring> {
    @Override
    protected Class<Ring> getEntityClass() {
        return Ring.class;
    }

    // Проверить, используется ли кольцо каким-либо существом
    public boolean isRingUsed(Long ringId) {
        String query = "SELECT COUNT(bc) FROM BookCreature bc WHERE bc.ring.id = :ringId";
        Long count = em.createQuery(query, Long.class)
                .setParameter("ringId", ringId)
                .getSingleResult();
        return count > 0;
    }

    // Получить список свободных колец
    public List<Ring> getFreeRings() {
        String query = "SELECT r FROM Ring r WHERE r.id NOT IN " +
                "(SELECT bc.ring.id FROM BookCreature bc WHERE bc.ring IS NOT NULL)";
        return em.createQuery(query, Ring.class).getResultList();
    }

    // Получить список использованных колец
    public List<Ring> getUsedRings() {
        String query = "SELECT DISTINCT bc.ring FROM BookCreature bc WHERE bc.ring IS NOT NULL";
        return em.createQuery(query, Ring.class).getResultList();
    }

}