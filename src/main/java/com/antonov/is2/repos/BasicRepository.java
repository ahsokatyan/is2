package com.antonov.is2.repos;

import lombok.NoArgsConstructor;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public abstract class BasicRepository<T> implements BasicRepo<T> {

    @PersistenceContext(unitName = "default")
    protected EntityManager em;

    // Методы остаются такими же, но без конструктора
    public T save(T entity) {
        em.persist(entity);
        return entity;
    }

    public Optional<T> findById(Long id) {
        T entity = em.find(getEntityClass(), id);
        return Optional.ofNullable(entity);
    }

    public List<T> findAll() {
        return em.createQuery("FROM " + getEntityClass().getSimpleName(), getEntityClass())
                .getResultList();
    }

    public List<T> findAll(int offset, int limit) {
        return em.createQuery("FROM " + getEntityClass().getSimpleName(), getEntityClass())
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public long count() {
        return em.createQuery("SELECT COUNT(*) FROM " + getEntityClass().getSimpleName(), Long.class)
                .getSingleResult();
    }

    public void delete(T entity) {
        if (em.contains(entity)) {
            em.remove(entity); // managed - удаляем сразу
        } else {
            // detached - сначала merge потом remove
            T managedEntity = em.merge(entity);
            em.remove(managedEntity);
        }
    }
    public T update(T entity) {
        return em.merge(entity);
    }

    public T merge(T entity) {
        return em.merge(entity);
    }

    public void deleteById(Long id) {
        T entity = em.find(getEntityClass(), id); // Получаем свежий MANAGED
        if (entity != null) {
            em.remove(entity);
        }
    }

    // Абстрактный метод, который должны реализовать потомки
    protected abstract Class<T> getEntityClass();

}