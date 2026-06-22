package com.bankingassistant.repository;

import com.bankingassistant.entity.Loan;
import com.bankingassistant.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {
    List<Loan> findByUserId(UUID userId);
    List<Loan> findByStatus(RequestStatus status);
}
