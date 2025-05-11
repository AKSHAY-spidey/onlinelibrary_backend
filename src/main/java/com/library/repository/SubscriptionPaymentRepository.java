package com.library.repository;

import com.library.model.Payment;
import com.library.model.Subscription;
import com.library.model.SubscriptionPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, Long> {
    
    List<SubscriptionPayment> findBySubscription(Subscription subscription);
    
    List<SubscriptionPayment> findByPayment(Payment payment);
    
    Optional<SubscriptionPayment> findBySubscriptionAndPayment(Subscription subscription, Payment payment);
}
