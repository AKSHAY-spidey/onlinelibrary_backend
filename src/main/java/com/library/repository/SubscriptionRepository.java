package com.library.repository;

import com.library.model.Subscription;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    List<Subscription> findByUser(User user);
    
    @Query("SELECT s FROM Subscription s WHERE s.user = ?1 AND s.status = 'ACTIVE' AND s.endDate > ?2")
    Optional<Subscription> findActiveSubscriptionByUser(User user, LocalDateTime now);
    
    List<Subscription> findByStatus(String status);
    
    List<Subscription> findByEndDateBeforeAndStatus(LocalDateTime endDate, String status);
    
    List<Subscription> findByAutoRenewAndEndDateBetween(Boolean autoRenew, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.planType = ?1 AND s.status = 'ACTIVE'")
    Long countActiveSubscriptionsByPlanType(String planType);
    
    @Query("SELECT s.planType, COUNT(s) FROM Subscription s WHERE s.status = 'ACTIVE' GROUP BY s.planType")
    List<Object[]> countActiveSubscriptionsByPlanTypeGrouped();
}
