package com.company.intersegrega;

import java.time.LocalDateTime;

import com.company.intersegrega.entity.Order;
import com.company.intersegrega.entity.User;
import com.company.intersegrega.service.NameSearchablePersistenceService;
import com.company.intersegrega.service.OrderPersistenceService;
import com.company.intersegrega.service.PersistenceService;
import com.company.intersegrega.service.UserPersistenceService;

/**
 * Small demo of the Interface Segregation Principle.
 *
 * <p>{@code userService} exposes {@code findByName} because a user has a name,
 * whereas {@code orderService} only exposes the generic persistence
 * operations. Calling {@code orderService.findByName(...)} does not even
 * compile, which is exactly the goal: no class depends on methods it does not
 * use.</p>
 */
public class Main {

  public static void main(String[] args) {
    User user = new User();
    user.setId(1L);
    user.setName("Ada");
    user.setLastLogin(LocalDateTime.now());

    NameSearchablePersistenceService<User> userService = new UserPersistenceService();
    userService.save(user);

    System.out.println("User found by id   : " + userService.findById(1L).getName());
    System.out.println("Users named 'ada'  : " + userService.findByName("ada").size());

    Order order = new Order();
    order.setId(1L);
    order.setOrderPlacedOn(LocalDateTime.now());
    order.setTotalValue(149.90);

    PersistenceService<Order> orderService = new OrderPersistenceService();
    orderService.save(order);

    System.out.println("Order found by id  : " + orderService.findById(1L).getTotalValue());
    // orderService.findByName("Ada"); // <- does not compile: Order has no name.
  }
}
