package com.antonov.is2.repos;

import com.antonov.is2.entities.UserAccount;
import lombok.NoArgsConstructor;

import javax.ejb.Stateless;
import java.util.List;
import java.util.Optional;

@Stateless
@NoArgsConstructor
public class UserAccountRepository extends BasicRepository<UserAccount> {
    @Override
    protected Class<UserAccount> getEntityClass() {
        return UserAccount.class;
    }

    public Optional<UserAccount> findByLogin(String login) {
        List<UserAccount> result = em.createQuery(
                        "SELECT u FROM UserAccount u WHERE u.login = :login", UserAccount.class)
                .setParameter("login", login)
                .getResultList();
        return result.stream().findFirst();
    }

    public List<UserAccount> findPending() {
        return em.createQuery(
                        "SELECT u FROM UserAccount u WHERE u.approved = false ORDER BY u.createdAt ASC",
                        UserAccount.class)
                .getResultList();
    }

    public List<UserAccount> findAllOrdered() {
        return em.createQuery(
                        "SELECT u FROM UserAccount u ORDER BY u.createdAt DESC",
                        UserAccount.class)
                .getResultList();
    }
}
