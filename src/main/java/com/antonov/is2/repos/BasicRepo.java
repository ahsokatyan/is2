package com.antonov.is2.repos;

import java.util.List;
import java.util.Optional;

public interface BasicRepo<T> {
    T save(T entity);
    Optional<T> findById(Long id);
    List<T> findAll();
    void delete(T entity);
    T update(T entity);
    List<T> findAll(int offset, int limit);
    long count();
}