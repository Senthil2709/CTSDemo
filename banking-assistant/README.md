# AI Multi-Agent Banking Assistant

A full-stack reference implementation of the banking assistant functional
specification: Spring Boot backend, Angular frontend, PostgreSQL +
pgvector, all Dockerized.

## Stack #####

- **Backend**: Java 17, Spring Boot 3.2.5, Spring Security (JWT, RBAC),
  Spring Data JPA, Flyway, PostgreSQL + pgvector.
- **Frontend**: Angular 17 (NgModule-based), Bootstrap 5, plain CSS design
  system (no Angular Material).
- **AI**: OpenAI chat completions + embeddings via a thin wrapper client.
  The app runs fully **without** an API key — every agent has a
  deterministic, rule-based fallback — so you can demo it offline and
  layer in a real LLM later.
- **Deployment**: Docker Compose (db, backend, frontend).

## Quick start

```bash
cp .env.example .env
# optionally edit .env to add OPENAI_API_KEY for real LLM-backed answers
docker compose --env-file .env up --build
```

- Frontend: http://localhost:8081
- Backend API: http://localhost:8080/api
- Postgres: localhost:5432

The backend runs Flyway migrations automatically on startup, seeding three
demo users and nine bank policy documents.

## Demo credentials

| Role | Username | Password |
|---|---|---|
| Customer | `customer1` | `Customer@123` |
| Relationship Manager | `relmanager` | `Manager@123` |
| Branch Admin | `branchadmin` | `Admin@123` |

The login screen has one-click buttons to fill these in.

## Module-to-code mapping

**Module 1 — Auth, KYC, RBAC**
`security/`, `controller/AuthController.java`, `service/UserService.java`,
`entity/User.java`, `entity/KycDetail.java`. Roles: `CUSTOMER`,
`RELATIONSHIP_MANAGER`, `BRANCH_ADMIN`. Tiers: `STANDARD` → `SILVER` →
`GOLD` → `PLATINUM`.

**Module 2 — AI Multi-Agent Architecture**
`agent/AccountAgent.java`, `agent/LoanAgent.java`, `agent/PolicyAgent.java`,
`agent/FinancialPlanningAgent.java`. Each implements the shared `Agent`
interface (`handle(AgentRequest) -> AgentResponse`) and also exposes typed
methods consumed directly by REST controllers.

**Module 3 — Orchestration**
`orchestrator/OrchestratorService.java` classifies intent (LLM with
keyword fallback), runs the `SafetyComplianceChecker`, routes to one agent
or — for financial-planning requests — runs Account + Loan + Financial
Planning agents **in parallel** via the `agentExecutor` thread pool
(`config/AsyncConfig.java`), then merges the results into one reply.

**Module 4 — Conversational Memory**
`service/ChatMemoryService.java` persists every chat turn and reuses the
customer's active session. `scheduler/ChatSessionCleanupScheduler.java`
auto-expires sessions idle for longer than `CHAT_SESSION_TIMEOUT_MIN`.

**Module 5 — Banking Policy Knowledge Base (Semantic RAG)**
`rag/PolicyDocumentDao.java` talks to Postgres/pgvector directly via
`JdbcTemplate` (the vector column is bound as a `"[v1,v2,...]"::vector`
text literal — no extra pgvector JDBC driver needed).
`rag/PolicyRagService.java` embeds the question, retrieves the top-K
documents by cosine distance, and asks the chat model to answer using only
that context. `scheduler/EmbeddingBackfillRunner.java` embeds the seeded
policy documents on startup if `OPENAI_API_KEY` is set; otherwise the
service falls back to keyword search.

**Module 6 — AI Financial Plan Generation**
`agent/FinancialPlanningAgent.java`: risk-based expected return, a
goal-based required-savings calculation (annuity formula), a month-wise
savings/growth schedule, investment product suggestions, loan options
where relevant, and regulatory/tax notes.

**Module 7 — Loan / Service Request Approval Workflow**
`service/LoanService.java` and `service/ServiceRequestService.java`
implement the `DRAFT → PENDING_APPROVAL → APPROVED/REJECTED → DISBURSED/ACTIVATED`
lifecycle. Customers apply via `/api/loans/apply` or
`/api/service-requests`; RMs/Branch Admins decide via
`/api/loans/{id}/decision` or `/api/service-requests/{id}/decision`, or
review everything at once on the **Approval Queue** page
(`/api/admin/approval-queue`).

## Running without Docker (local dev)

**Backend**
```bash
cd backend
# requires a local Postgres with the pgvector extension available
mvn spring-boot:run
```

**Frontend**
```bash
cd frontend
npm install
npm start   # serves on http://localhost:4200, proxies nothing by default —
            # point environment.ts apiBaseUrl at your backend if not 8080
```

## Notes

- All monetary values are in INR.
- This is a reference / demo build: validation, error handling, and the
  RAG pipeline are production-shaped but simplified (e.g. EMI/interest
  rates are illustrative, not real product rates).
- If `OPENAI_API_KEY` is left blank, intent classification, policy
  answers, and multi-agent response merging all use deterministic
  fallbacks so the whole app still works end-to-end offline.
