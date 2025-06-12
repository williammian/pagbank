package br.com.wm.pagbankapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.wm.pagbankapi.model.Order;
import br.com.wm.pagbankapi.model.Order.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByPagbankOrderId(String pagbankOrderId);
    
    Optional<Order> findByReferenceId(String referenceId);
    
    @Query("SELECT o.status FROM Order o WHERE o.referenceId = :referenceId")
    Optional<OrderStatus> findStatusByReferenceId(String referenceId);
}
