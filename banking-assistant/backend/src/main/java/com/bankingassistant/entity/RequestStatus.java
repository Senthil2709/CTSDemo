package com.bankingassistant.entity;

/** Shared status lifecycle for loans and generic service requests. */
public enum RequestStatus {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    DISBURSED,
    ACTIVATED
}
