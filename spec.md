# AI-Powered Intelligent Banking Assistant — Project Spec

## Project Overview

Build a multi-agent AI banking assistant using Spring Boot (or JVM equivalent), React/Angular frontend, PostgreSQL with pgvector, and OpenAI LLM integration. All development is conducted via Claude Code with GitHub and Brave Search MCP servers configured.

---

## Task 1: Project Scaffolding and Configuration

### 1.1 Initialize Repository
- Create a new GitHub repository for the project
- Set up `.gitignore` for Java/Spring Boot, Node.js, and Docker artifacts
- Create a base `README.md` with project overview and setup instructions

### 1.2 Backend Scaffolding (Spring Boot)
- Generate a Spring Boot project using Spring Initializr with dependencies:
  - Spring Web, Spring Security, Spring Data JPA, Spring Data JDBC
  - JWT (jjwt), Lombok, MapStruct, Validation
  - PostgreSQL Driver, Flyway (DB migrations)
  - OpenAPI/Swagger (springdoc-openapi)
- Set up Maven or Gradle build configuration
- Configure `application.yml` with profiles: `dev`, `test`, `prod`
- Set up package structure:
  - `controller`, `service`, `repository`, `domain/entity`, `domain/dto`, `config`, `agent`, `orchestrator`, `rag`, `security`, `exception`

### 1.3 Frontend Scaffolding
- Initialize Angular or React project
- Set up project folder structure: `components`, `pages`, `services`, `guards`, `models`
- Install and configure HTTP client, routing, and auth interceptors

### 1.4 Docker and Infrastructure
- Write `Dockerfile` for Spring Boot backend
- Write `Dockerfile` for the frontend (Nginx-served build)
- Write `docker-compose.yml` orchestrating:
  - Backend service
  - Frontend service
  - PostgreSQL (with pgvector extension enabled)
- Add environment variable support via `.env` file

### 1.5 CI/CD Pipeline
- Create `.github/workflows/ci.yml` with GitHub Actions:
  - Steps: checkout → build → test → Docker build
- Add a basic lint/format check step

---

## Task 2: MCP Tool Setup (Claude Code)

### 2.1 GitHub MCP Server
- Add GitHub MCP server entry to `.mcp.json` in Claude Code
- Authenticate with a GitHub Personal Access Token (PAT) scoped to `repo` and `pull_requests`
- Verify connectivity: ask Claude Code to list open issues on the project repository
- Use Claude Code + GitHub MCP to:
  - Create feature branches for each module
  - Raise pull requests without leaving the editor
  - Fetch and summarise open issues before starting each sprint task
  - Review diffs and apply suggestions before merging

### 2.2 Brave Search MCP Server
- Add Brave Search MCP server entry to `.mcp.json`
- Supply the Brave Search API key in MCP server environment config
- Verify connectivity: ask Claude Code to search for the latest Spring Boot release notes
- Use Brave Search MCP during development for:
  - Current RBI/banking regulatory guidelines for compliance features
  - CVE and security advisories when adding third-party dependencies
  - Up-to-date API documentation for LLM providers and vector databases
  - Fintech standards: ISO 20022, SWIFT, PCI-DSS

---

## Task 3: Database Schema and Migrations

### 3.1 Enable pgvector
- Run SQL to enable the pgvector extension in PostgreSQL
- Confirm the extension is available in the Docker compose setup

### 3.2 Core Schema Migrations (Flyway)
- `V1__create_users_table.sql` — users with roles, KYC fields, account tier
- `V2__create_accounts_table.sql` — bank accounts linked to users
- `V3__create_transactions_table.sql` — transaction history per account
- `V4__create_loans_table.sql` — loan records with status, type, EMI fields
- `V5__create_service_requests_table.sql` — loan/service request workflow with status enum
- `V6__create_chat_sessions_table.sql` — chat sessions per customer with expiry
- `V7__create_chat_messages_table.sql` — messages linked to sessions
- `V8__create_policy_documents_table.sql` — policy document metadata
- `V9__create_policy_embeddings_table.sql` — pgvector embeddings for policy docs
- `V10__create_financial_plans_table.sql` — stored financial plan outputs per customer
- `V11__seed_roles_and_admin.sql` — seed default roles and admin user

---

## Task 4: Module 1 — User Authentication and Authorization

### 4.1 Security Configuration
- Configure Spring Security filter chain
- Set up JWT utility class: generate, validate, extract claims
- Implement `JwtAuthenticationFilter` (OncePerRequestFilter)
- Configure CORS and CSRF settings
- Set up `UserDetailsService` backed by the user repository

### 4.2 User Registration and Login
- `POST /api/auth/register` — register a new customer with password hashing (BCrypt)
- `POST /api/auth/login` — authenticate and return JWT access token

### 4.3 Role-Based Access Control (RBAC)
- Define roles: `CUSTOMER`, `RELATIONSHIP_MANAGER`, `BRANCH_ADMIN`
- Annotate controllers with `@PreAuthorize` role checks
- Implement method-level security for sensitive endpoints

### 4.4 Customer Profile and KYC
- `GET /api/customers/{id}/profile` — fetch customer profile
- `PUT /api/customers/{id}/profile` — update profile fields
- `POST /api/customers/{id}/kyc` — submit KYC documents (store metadata)
- `GET /api/customers/{id}/kyc/status` — KYC verification status

### 4.5 Account Tier and Product Eligibility
- Define account tiers: Basic, Silver, Gold, Platinum
- Implement eligibility rules engine for product access by tier
- `GET /api/customers/{id}/eligibility` — return eligible products for a customer

---

## Task 5: Module 2 — AI Multi-Agent Architecture

### 5.1 Agent Interface and Base Class
- Define `BankingAgent` interface with `handle(AgentRequest)` → `AgentResponse`
- Implement `BaseAgent` abstract class with LLM client injection, prompt rendering, and error handling
- Define `AgentRequest` and `AgentResponse` DTOs

### 5.2 Account Agent
- Retrieve account balance and transaction history for a customer
- Recommend suitable account types based on customer profile
- Check eligibility for account upgrades
- Summarize account activity and spending patterns
- Expose as `AccountAgent` Spring service bean

### 5.3 Loan Agent
- Evaluate loan eligibility based on credit profile (income, existing EMIs, credit score)
- Recommend suitable loan products: personal, home, auto
- Calculate EMI and total interest given principal, rate, and tenure
- Check policy compliance for loan disbursement conditions
- Expose as `LoanAgent` Spring service bean

### 5.4 Policy Agent
- Answer banking policy and regulatory questions using RAG (see Module 5)
- Retrieve relevant policy documents semantically
- Explain terms and conditions, interest rates, and fee structures
- Expose as `PolicyAgent` Spring service bean

### 5.5 Financial Planning Agent
- Accept customer financial profile: income, expenses, liabilities, goal, timeline, risk appetite
- Generate personalized savings and investment plans
- Suggest suitable financial products: FDs, mutual funds, insurance
- Provide budget breakdowns and goal-based projections
- Ensure recommendations are policy-compliant and risk-appropriate
- Expose as `FinancialPlanningAgent` Spring service bean

---

## Task 6: Module 3 — Agent Orchestration

### 6.1 Orchestrator Service
- Implement `OrchestratorService` as the central routing layer
- Receive customer requests from the API layer
- Perform safety and compliance pre-checks before routing

### 6.2 Intent Classification
- Implement `IntentClassifier` using LLM prompt to classify input into:
  - `ACCOUNT_QUERY`, `LOAN_QUERY`, `POLICY_QUERY`, `FINANCIAL_PLANNING`, `GENERAL`
- Return classified intent and confidence score

### 6.3 Request Routing
- Route single-intent requests to the appropriate agent
- For `FINANCIAL_PLANNING` intent, invoke Account Agent + Loan Agent + Financial Planning Agent in parallel

### 6.4 Parallel Agent Execution
- Use `CompletableFuture` or virtual threads to run multiple agents concurrently
- Implement timeout handling for each parallel agent call

### 6.5 Response Merging
- Implement `ResponseMerger` to combine outputs from multiple agents
- Generate a single customer-friendly natural language response using an LLM summarization call

### 6.6 Orchestration API Endpoint
- `POST /api/assistant/chat` — accepts customer message and session ID, returns merged response

---

## Task 7: Module 4 — Conversational Memory

### 7.1 Chat Session Management
- `POST /api/sessions` — create a new chat session for a customer
- `GET /api/sessions/{sessionId}` — retrieve session metadata
- `DELETE /api/sessions/{sessionId}` — terminate a session

### 7.2 Message Persistence
- Save every incoming customer message and outgoing assistant response to the `chat_messages` table
- Link messages to session and customer IDs with timestamps

### 7.3 Session Expiry
- Implement a scheduled job (`@Scheduled`) to expire sessions inactive for a configurable duration
- Mark expired sessions with `status = EXPIRED`

### 7.4 History Retrieval for Agent Context
- Implement `ConversationHistoryService` to fetch the last N messages of a session
- Pass conversation history as context in each agent LLM call

---

## Task 8: Module 5 — Banking Policy Knowledge Base (Semantic RAG)

### 8.1 Policy Document Ingestion
- Implement `PolicyIngestionService`:
  - Accept policy document text and metadata
  - Chunk documents into passages (configurable chunk size with overlap)
  - Generate embeddings using OpenAI Embedding API (or configured provider)
  - Store text chunks and embeddings in the `policy_embeddings` table via pgvector

### 8.2 Ingestion API
- `POST /api/admin/policies` — upload a new policy document (admin only)
- `GET /api/admin/policies` — list all stored policy documents
- `DELETE /api/admin/policies/{id}` — remove a policy document and its embeddings

### 8.3 Seed Policy Documents
- Write a seeder or migration script to load the following policies into the knowledge base:
  - Savings and current account terms
  - Home and personal loan eligibility criteria
  - Credit card fee and reward structures
  - KYC and AML compliance guidelines
  - Grievance redressal and escalation process
  - Digital banking and OTP security policy

### 8.4 Semantic Retrieval
- Implement `PolicyRetrievalService`:
  - Accept a query string
  - Generate query embedding
  - Perform cosine similarity search against `policy_embeddings` using pgvector (`<=>` operator)
  - Return top-K relevant policy chunks

### 8.5 RAG-Augmented Response Generation
- In `PolicyAgent`, combine retrieved policy chunks as context with the customer query
- Build the prompt: `[System Instructions] + [Policy Context] + [Customer Question]`
- Call LLM and return the grounded policy answer

---

## Task 9: Module 6 — AI Financial Plan Generation

### 9.1 Financial Plan Request DTO
- Define `FinancialPlanRequest` with fields:
  - `monthlyIncome`, `monthlyExpenses`, `existingEMIs`
  - `financialGoal` (enum: HOME_PURCHASE, RETIREMENT, EDUCATION, EMERGENCY_FUND, OTHER)
  - `targetTimelineMonths`, `riskAppetite` (CONSERVATIVE, MODERATE, AGGRESSIVE)

### 9.2 Financial Plan Generation
- Implement `FinancialPlanService` to build a structured LLM prompt from the request
- Call LLM to generate:
  - Recommended monthly savings amount
  - Suitable investment products (FDs, mutual funds, insurance)
  - Loan options if applicable
  - Month-wise financial schedule (milestone table)
  - Estimated goal achievement timeline
  - Regulatory and tax notes relevant to the plan

### 9.3 Plan Persistence
- Store generated plan JSON in `financial_plans` table linked to the customer
- `GET /api/customers/{id}/financial-plans` — list a customer's saved plans
- `GET /api/customers/{id}/financial-plans/{planId}` — retrieve a specific plan

### 9.4 Financial Plan API Endpoint
- `POST /api/assistant/financial-plan` — submit financial profile, receive generated plan
- Validate input (required fields, positive numeric values)

---

## Task 10: Module 7 — Loan and Service Request Approval Workflow

### 10.1 Service Request Submission
- `POST /api/service-requests` — customer submits a loan or service request
- Supported request types: `PERSONAL_LOAN`, `HOME_LOAN`, `AUTO_LOAN`, `ACCOUNT_UPGRADE`, `CARD_REQUEST`
- Initial status set to `DRAFT`

### 10.2 Status Workflow
- Implement status transitions:
  - `DRAFT` → `PENDING_APPROVAL` (customer submits)
  - `PENDING_APPROVAL` → `APPROVED` or `REJECTED` (Relationship Manager / Branch Admin acts)
  - `APPROVED` → `DISBURSED` or `ACTIVATED` (post-processing)
- Validate that only authorized roles can perform each transition

### 10.3 Workflow API Endpoints
- `GET /api/service-requests` — list requests (filtered by role: own requests for customers, all pending for managers)
- `GET /api/service-requests/{id}` — get request details
- `PUT /api/service-requests/{id}/submit` — customer submits draft
- `PUT /api/service-requests/{id}/approve` — manager approves
- `PUT /api/service-requests/{id}/reject` — manager rejects (with reason)
- `PUT /api/service-requests/{id}/disburse` — admin marks as disbursed/activated

### 10.4 Notifications (Basic)
- On status change, log an event and store a notification record for the customer
- `GET /api/customers/{id}/notifications` — retrieve unread notifications

---

## Task 11: Frontend Implementation

### 11.1 Authentication Pages
- Login page with JWT token storage (HTTP-only cookie or memory)
- Registration page for new customer signup
- Route guards to protect authenticated routes by role

### 11.2 Customer Dashboard
- Account summary cards (balance, account type, tier)
- Recent transaction list
- Quick action buttons: Chat Assistant, Apply for Loan, Generate Financial Plan

### 11.3 Chat Assistant Interface
- Conversational chat UI with message bubbles (customer / assistant)
- Session management: start new session, view session history
- Stream or poll for assistant responses
- Display agent attribution in responses (e.g., "Answered by Policy Agent")

### 11.4 Financial Plan Generator
- Form to input: income, expenses, EMIs, goal, timeline, risk appetite
- Submit to backend and render the generated plan in a structured view
- Show month-wise schedule as a table

### 11.5 Loan / Service Request Pages
- Apply for loan form (loan type, amount, tenure)
- Track existing requests and their status
- Relationship Manager view: list pending requests with approve/reject actions

### 11.6 Admin Pages
- Policy document upload form (admin only)
- List and delete policy documents
- User management: list users, update roles

---

## Task 12: Testing

### 12.1 Unit Tests
- Service layer tests for: `OrchestratorService`, `IntentClassifier`, `PolicyRetrievalService`, `FinancialPlanService`, `LoanEligibilityService`
- Use Mockito to mock LLM client, repositories, and external APIs
- Target ≥ 80% coverage on service classes

### 12.2 Integration Tests
- Use `@SpringBootTest` with Testcontainers (PostgreSQL + pgvector)
- Test API endpoints end-to-end: auth flow, chat flow, financial plan flow
- Test pgvector similarity search with sample embeddings

### 12.3 Security Tests
- Verify JWT expiry and tamper rejection
- Verify RBAC: confirm customers cannot access manager/admin endpoints
- Verify SQL injection protection on query parameters

---

## Task 13: Documentation and Confluence Artifacts

### 13.1 API Documentation
- Ensure all REST endpoints are annotated with springdoc-openapi annotations
- Export and publish Swagger/OpenAPI spec to Confluence

### 13.2 Confluence Artifact 1 — MCP Setup Guide
- Document step-by-step setup for GitHub MCP Server and Brave Search MCP Server
- Include `.mcp.json` configuration snippets (credentials redacted)
- Add screenshots of Claude Code successfully connecting to each MCP tool
- Include one example prompt and result for each MCP tool

### 13.3 Confluence Artifact 2 — Sample Query and API Call Log
- Log at least three MySQL/PostgreSQL queries executed via Claude Code with prompts and results
- Log at least three REST API calls made via Claude Code with prompts and response summaries
- Add a brief note on how each interaction helped during development

### 13.4 Architecture Decision Records (ADRs)
- ADR-001: Choice of LLM provider and embedding model
- ADR-002: Choice of vector database (pgvector vs alternatives)
- ADR-003: Multi-agent framework approach (custom vs LangChain/LangGraph)
- ADR-004: Session storage strategy for conversational memory

---

## Task 14: Deployment

### 14.1 Docker Build and Verification
- Build Docker images for backend and frontend
- Run `docker-compose up` and verify all services start cleanly
- Confirm database migrations execute on first startup

### 14.2 Environment Configuration
- Ensure all secrets (DB password, JWT secret, OpenAI API key) are injected via environment variables
- Confirm no secrets are committed to the repository

### 14.3 Health Checks
- Add `/actuator/health` endpoint to the backend
- Add health check entries in `docker-compose.yml` for backend and database services

### 14.4 GitHub Actions CI Verification
- Push to `main` branch and confirm CI pipeline runs: build → test → Docker build
- Fix any pipeline failures before final submission
