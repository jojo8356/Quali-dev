package com.company.intersegrega.service;

import java.util.List;

import com.company.intersegrega.entity.Entity;

/**
 * Persistence contract for entities that carry a name (e.g. {@code User}).
 *
 * <p>This capability is kept out of {@link PersistenceService} on purpose:
 * an {@code Order} has no name, so {@code OrderPersistenceService} must not be
 * forced to implement {@code findByName}.</p>
 */
public interface NameSearchablePersistenceService<T extends Entity> extends PersistenceService<T> {

  List<T> findByName(String name);
}
