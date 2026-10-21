package com.company.intersegrega.service;

import com.company.intersegrega.entity.Entity;

/**
 * Common persistence contract shared by every entity.
 *
 * <p>Only the operations that make sense for <em>all</em> entities are declared
 * here: save, delete and find by identifier. Anything that is specific to a
 * given entity (e.g. searching a user by name) belongs to a dedicated
 * interface, so that other entities are not forced to implement it.</p>
 */
public interface PersistenceService<T extends Entity> {

  void save(T entity);

  void delete(T entity);

  T findById(Long id);
}
