package com.antonov.is2.repos;

import com.antonov.is2.entities.ImportOperation;
import lombok.NoArgsConstructor;

import javax.ejb.Stateless;
import java.util.List;

@Stateless
@NoArgsConstructor
public class ImportOperationRepository extends BasicRepository<ImportOperation> {
    @Override
    protected Class<ImportOperation> getEntityClass() {
        return ImportOperation.class;
    }

    public List<ImportOperation> findAllOrdered() {
        return em.createQuery("SELECT i FROM ImportOperation i ORDER BY i.createdAt DESC", ImportOperation.class)
                .getResultList();
    }

    public List<ImportOperation> findByUsernameOrdered(String username) {
        return em.createQuery(
                        "SELECT i FROM ImportOperation i WHERE i.username = :username ORDER BY i.createdAt DESC",
                        ImportOperation.class)
                .setParameter("username", username)
                .getResultList();
    }
}
