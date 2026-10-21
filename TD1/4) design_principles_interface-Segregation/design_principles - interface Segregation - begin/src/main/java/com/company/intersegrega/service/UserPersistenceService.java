package com.company.intersegrega.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.company.intersegrega.entity.User;

//Stores User entities. Users do have a name, so this service can expose findByName.
public class UserPersistenceService implements NameSearchablePersistenceService<User> {

  private static final Map<Long, User> USERS = new HashMap<>();

  @Override
  public void save(User entity) {
    synchronized (USERS) {
      USERS.put(entity.getId(), entity);
    }
  }

  @Override
  public void delete(User entity) {
    synchronized (USERS) {
      USERS.remove(entity.getId());
    }
  }

  @Override
  public User findById(Long id) {
    synchronized (USERS) {
      return USERS.get(id);
    }
  }

  @Override
  public List<User> findByName(String name) {
    synchronized (USERS) {
      return USERS.values().stream()
          .filter(user -> user.getName() != null && user.getName().equalsIgnoreCase(name))
          .collect(Collectors.toList());
    }
  }
}
