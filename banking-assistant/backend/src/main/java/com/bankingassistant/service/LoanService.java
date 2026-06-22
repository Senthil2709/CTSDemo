package com.bankingassistant.service;

import com.bankingassistant.agent.LoanAgent;
import com.bankingassistant.dto.loan.LoanApplicationRequest;
import com.bankingassistant.dto.loan.LoanDecisionRequest;
import com.bankingassistant.dto.loan.LoanResponse;
import com.bankingassistant.entity.Loan;
import com.bankingassistant.entity.LoanType;
import com.bankingassistant.entity.RequestStatus;
import com.bankingassistant.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Module 7 - Loan and Service Request Approval Workflow (loan-specific side).
 * Wraps the Loan entity's status lifecycle: PENDING_APPROVAL -> APPROVED /
 * REJECTED -> DISBURSED. Distinct from LoanAgent, which only provides
 * advisory eligibility / EMI calculations without persisting state.
 */
@Service
@RequiredArgsConstructor
public class LoanService {

    private static final Map<LoanType, BigDecimal> BASE_RATES = Map.of(
            LoanType.HOME, new BigDecimal("8.50"),
            LoanType.PERSONAL, new BigDecimal("12.00"),
            LoanType.AUTO, new BigDecimal("9.75")
    );

    private final LoanRepository loanRepository;
    private final LoanAgent loanAgent;

    @Transactional
    public LoanResponse apply(UUID userId, LoanApplicationRequest request) {
        BigDecimal rate = BASE_RATES.getOrDefault(request.getLoanType(), new BigDecimal("11.00"));
        BigDecimal emi = loanAgent.calculateEmi(request.getPrincipalAmount(), rate, request.getTenureMonths()).getEmi();

        Loan loan = Loan.builder()
                .userId(userId)
                .loanType(request.getLoanType())
                .principalAmount(request.getPrincipalAmount())
                .interestRate(rate)
                .tenureMonths(request.getTenureMonths())
                .emiAmount(emi)
                .status(RequestStatus.PENDING_APPROVAL)
                .purpose(request.getPurpose())
                .build();

        loan = loanRepository.save(loan);
        return toResponse(loan);
    }

    public List<LoanResponse> listForUser(UUID userId) {
        return loanRepository.findByUserId(userId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<LoanResponse> listByStatus(RequestStatus status) {
        return loanRepository.findByStatus(status).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<LoanResponse> listAll() {
        return loanRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public LoanResponse getById(UUID loanId) {
        return toResponse(loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found")));
    }

    @Transactional
    public LoanResponse decide(UUID loanId, UUID deciderId, LoanDecisionRequest request) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        validateTransition(loan.getStatus(), request.getStatus());

        loan.setStatus(request.getStatus());
        loan.setDecidedBy(deciderId);
        loan.setDecidedAt(LocalDateTime.now());
        loan.setDecisionNotes(request.getNotes());

        loan = loanRepository.save(loan);
        return toResponse(loan);
    }

    private void validateTransition(RequestStatus current, RequestStatus target) {
        boolean valid = switch (current) {
            case PENDING_APPROVAL -> target == RequestStatus.APPROVED || target == RequestStatus.REJECTED;
            case APPROVED -> target == RequestStatus.DISBURSED;
            default -> false;
        };
        if (!valid) {
            throw new IllegalStateException("Cannot transition loan from " + current + " to " + target);
        }
    }

    private LoanResponse toResponse(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .userId(loan.getUserId())
                .loanType(loan.getLoanType().name())
                .principalAmount(loan.getPrincipalAmount())
                .interestRate(loan.getInterestRate())
                .tenureMonths(loan.getTenureMonths())
                .emiAmount(loan.getEmiAmount())
                .status(loan.getStatus().name())
                .purpose(loan.getPurpose())
                .createdAt(loan.getCreatedAt())
                .decisionNotes(loan.getDecisionNotes())
                .build();
    }
}
