package com.bankingassistant.repository;

import com.bankingassistant.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByAccountIdOrderByTransactedAtDesc(UUID accountId);
    List<Transaction> findByAccountIdAndTransactedAtAfterOrderByTransactedAtDesc(UUID accountId, LocalDateTime since);
}
