package com.company.intersegrega.service;

import java.util.HashMap;
import java.util.Map;

import com.company.intersegrega.entity.Order;

/**
 * Stores Order entities.
 *
 * <p>An order has no name, therefore this service only implements the generic
 * persistence contract: {@code findByName} is not part of it anymore.
 * {@link NameSearchablePersistenceService} is the interface to use when name
 * lookups are actually needed.</p>
 */
public class OrderPersistenceService implements PersistenceService<Order> {

  private static final Map<Long, Order> ORDERS = new HashMap<>();

  @Override
  public void save(Order entity) {
    synchronized (ORDERS) {
      ORDERS.put(entity.getId(), entity);
    }
  }

  @Override
  public void delete(Order entity) {
    synchronized (ORDERS) {
      ORDERS.remove(entity.getId());
    }
  }

  @Override
  public Order findById(Long id) {
    synchronized (ORDERS) {
      return ORDERS.get(id);
    }
  }
}
