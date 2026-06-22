package com.bankingassistant.controller;

import com.bankingassistant.agent.FinancialPlanningAgent;
import com.bankingassistant.dto.financialplan.FinancialPlanRequest;
import com.bankingassistant.dto.financialplan.FinancialPlanResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/financial-plan")
@RequiredArgsConstructor
public class FinancialPlanController {

    private final FinancialPlanningAgent financialPlanningAgent;

    @PostMapping("/generate")
    public ResponseEntity<FinancialPlanResponse> generate(@Valid @RequestBody FinancialPlanRequest request) {
        return ResponseEntity.ok(financialPlanningAgent.generateFinancialPlan(request));
    }
}
