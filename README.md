# 💳 CogniQ Billing Service

The **CogniQ Billing Service** is the **Subscription, Usage, and Invoicing** component for the CogniQ platform.  
It manages plans, user subscriptions, usage tracking (compute hours, team seats, datasets, training jobs), and billing history.

---

## 🧩 Role

**Subscription & Billing Provider**

---

## 📌 Responsibilities

### 📋 Plans
- List active subscription plans (FREE, PRO, ENTERPRISE)
- Plan limits (compute hours, team seats, dataset count, training jobs) and pricing
- Seed default plans on first run

### 📤 Subscriptions
- Current user subscription (plan, status, period start/end)
- Subscribe to a plan (creates subscription with current period)
- Cancel at period end
- Subscription history

### 📊 Usage
- Current period usage summary (used vs limits per usage type)
- Usage types: COMPUTE_HOURS, TEAM_SEATS, DATASET_COUNT, TRAINING_JOBS, API_CALLS
- Record usage (for internal/webhook from Data or Orchestrator services)

### 🧾 Invoices
- List invoices (billing history) for the current user
- Invoice detail (amount, status, period, due date, paid at)

### 🛂 Authorization
- User identity from **X-User-Id** (Gateway) or JWT
- Ownership check on all subscription, usage, and invoice access
- Plans list is public; subscription/usage/invoices require authentication

---

## 🗄️ Database

**PostgreSQL**

### Core Tables
- **plans** — subscription plans (name, slug, price, limits JSONB, features JSONB)
- **subscriptions** — user subscription (user_id, plan_id, status, current_period_start/end)
- **usage_records** — usage events (user_id, usage_type, quantity, period)
- **invoices** — billing period invoices (user_id, amount, status, period, due_date)

---

## 🏗️ Architecture Context

This service is a core component of the **CogniQ microservices architecture** and integrates with:

- API Gateway (routes **/api/billing/**; forwards **X-User-Id** after JWT validation)
- Service Discovery (Eureka)
- Data Service / Orchestrator (optional: report usage via internal API or events)

---

## 🚀 Tech Stack

- Java 21 / Spring Boot 4
- Spring Security (JWT filter, ownership checks)
- Spring Validation (@Valid on request bodies)
- PostgreSQL (JPA, JSONB for limits/features)
- Actuator, Prometheus (**billing_subscriptions_active_count**), Swagger (OpenAPI 3)
- Eureka Client

---

## 📄 License

This project is part of the **CogniQ Platform** and follows the platform’s licensing model.
