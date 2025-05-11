package com.library.repository;

import com.library.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    
    Optional<PaymentMethod> findByName(String name);
    
    List<PaymentMethod> findByIsActive(Boolean isActive);
    
    List<PaymentMethod> findByIsActiveOrderByProcessingFeeAsc(Boolean isActive);
}
