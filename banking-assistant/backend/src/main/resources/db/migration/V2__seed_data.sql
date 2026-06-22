-- ============================================================
-- V2: Seed demo users, demo accounts and policy documents
-- Demo passwords (bcrypt hashed below):
--   branchadmin / Admin@123
--   relmanager  / Manager@123
--   customer1   / Customer@123
-- ============================================================

INSERT INTO users (id, username, email, password_hash, full_name, phone_number, role, account_tier)
VALUES
 (gen_random_uuid(), 'branchadmin', 'branchadmin@bankingassistant.com',
  '$2b$10$ii6KrzYGEu0wMTnz8KxgUepnkWhtyRO9NJbfWncMzC/hV0FKXEIVe',
  'Branch Admin', '9000000001', 'BRANCH_ADMIN', 'STANDARD'),
 (gen_random_uuid(), 'relmanager', 'relmanager@bankingassistant.com',
  '$2b$10$4gjmhN93GEEZzczYLaVVcexNXdVhVW4LUhKehRx1qtWPcZOlRdApS',
  'Relationship Manager', '9000000002', 'RELATIONSHIP_MANAGER', 'STANDARD'),
 (gen_random_uuid(), 'customer1', 'customer1@bankingassistant.com',
  '$2b$10$wGpZYuzly.7lUMTsrkkz1OLAvhCxo4XmNP5xiLsKlhWOHpSjukvFm',
  'Asha Verma', '9000000003', 'CUSTOMER', 'GOLD');

INSERT INTO kyc_details (user_id, id_proof_type, id_proof_number, address, date_of_birth, annual_income, employment_status, credit_score, kyc_status, verified_at)
SELECT id, 'PASSPORT', 'P1234567', 'Coimbatore, Tamil Nadu, IN', '1992-04-12', 1200000.00, 'SALARIED', 745, 'VERIFIED', now()
FROM users WHERE username = 'customer1';

INSERT INTO accounts (id, user_id, account_number, account_type, balance, currency, status)
SELECT gen_random_uuid(), id, 'IN00BANK0001234567', 'SAVINGS', 245000.00, 'INR', 'ACTIVE'
FROM users WHERE username = 'customer1';

INSERT INTO accounts (id, user_id, account_number, account_type, balance, currency, status)
SELECT gen_random_uuid(), id, 'IN00BANK0007654321', 'CURRENT', 50000.00, 'INR', 'ACTIVE'
FROM users WHERE username = 'customer1';

-- A handful of starter transactions for the savings account
INSERT INTO transactions (account_id, type, category, amount, description, balance_after, transacted_at)
SELECT a.id, 'CREDIT', 'SALARY', 95000.00, 'Monthly salary credit', 245000.00, now() - interval '2 days'
FROM accounts a WHERE a.account_number = 'IN00BANK0001234567';

INSERT INTO transactions (account_id, type, category, amount, description, balance_after, transacted_at)
SELECT a.id, 'DEBIT', 'RENT', 22000.00, 'Rent payment', 150000.00, now() - interval '5 days'
FROM accounts a WHERE a.account_number = 'IN00BANK0001234567';

INSERT INTO transactions (account_id, type, category, amount, description, balance_after, transacted_at)
SELECT a.id, 'DEBIT', 'GROCERY', 6500.00, 'Supermarket purchase', 172000.00, now() - interval '7 days'
FROM accounts a WHERE a.account_number = 'IN00BANK0001234567';

-- ---------------------------------------------------------------
-- Policy documents (embeddings are populated at application startup
-- by EmbeddingBackfillRunner using the OpenAI embeddings API)
-- ---------------------------------------------------------------
INSERT INTO policy_documents (id, title, category, content) VALUES
(gen_random_uuid(), 'Savings Account Terms', 'ACCOUNTS',
 'Savings accounts require a minimum average monthly balance of INR 2,500 for metro branches and INR 1,000 for rural branches. Interest is calculated daily on the closing balance and credited quarterly at 3.5% per annum. Non-maintenance of minimum balance attracts a penalty of up to INR 100 per month. Account holders get a complimentary debit card and unlimited UPI transactions.'),
(gen_random_uuid(), 'Current Account Terms', 'ACCOUNTS',
 'Current accounts are designed for businesses and require a minimum balance of INR 10,000. No interest is paid on current account balances. Account holders are eligible for overdraft facilities subject to credit assessment and collateral requirements. Cheque book and RTGS/NEFT services are provided free of charge.'),
(gen_random_uuid(), 'Home Loan Eligibility Criteria', 'LOANS',
 'Home loans are available to salaried and self-employed applicants aged 21 to 60 years with a minimum credit score of 700. Maximum loan amount is up to 80% of property value (loan-to-value ratio) subject to income multiple of up to 60 times net monthly salary. Interest rates range from 8.5% to 10.5% per annum based on credit profile. Maximum tenure is 30 years. Required documents include income proof, property documents, and KYC.'),
(gen_random_uuid(), 'Personal Loan Eligibility Criteria', 'LOANS',
 'Personal loans require a minimum credit score of 650 and minimum monthly income of INR 25,000. Loan amounts range from INR 50,000 to INR 25,00,000 with tenure from 12 to 60 months. Interest rates range from 10.5% to 18% per annum depending on credit profile and employer category. No collateral is required. Processing fee of up to 2% of loan amount applies.'),
(gen_random_uuid(), 'Auto Loan Eligibility Criteria', 'LOANS',
 'Auto loans finance up to 90% of the on-road price of new vehicles and up to 80% for used vehicles. Minimum credit score required is 680. Tenure ranges from 12 to 84 months. Interest rates range from 8.75% to 13% per annum. The vehicle is hypothecated to the bank until the loan is fully repaid.'),
(gen_random_uuid(), 'Credit Card Fee and Reward Structure', 'CARDS',
 'Standard credit cards carry an annual fee of INR 500, waived if annual spend exceeds INR 1,00,000. Reward points are earned at 1 point per INR 100 spent, with 5x points on dining and travel categories. Late payment fees range from INR 100 to INR 1,300 depending on outstanding balance. Interest on revolving credit is charged at 3.5% per month if the full balance is not paid by the due date.'),
(gen_random_uuid(), 'KYC and AML Compliance Guidelines', 'COMPLIANCE',
 'All customers must complete Know Your Customer verification using a valid government-issued photo ID and address proof before account activation. Periodic KYC re-verification is required every 2 years for low-risk customers and annually for high-risk customers. Transactions above INR 10,00,000 in cash are reported under Anti-Money-Laundering regulations. Politically Exposed Persons require enhanced due diligence.'),
(gen_random_uuid(), 'Grievance Redressal and Escalation Process', 'COMPLIANCE',
 'Customers can raise complaints through the mobile app, branch, or contact centre. The bank aims to resolve complaints within 7 working days. If unresolved, customers may escalate to the Branch Manager and subsequently to the Nodal Officer for grievance redressal. If still unsatisfied after 30 days, customers may approach the Banking Ombudsman appointed by the regulator.'),
(gen_random_uuid(), 'Digital Banking and OTP Security Policy', 'SECURITY',
 'One Time Passwords are valid for 5 minutes and can be used only once. Customers must never share OTPs, PINs, or CVVs with anyone, including bank staff. Mobile banking sessions automatically time out after 5 minutes of inactivity. Customers should enable biometric or app-lock security on devices used for digital banking and report lost devices immediately to block access.');
