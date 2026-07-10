# JatiStore

This codebase is managed as a monorepo, containing both our Java Spring Boot backend and React frontend, alongside a containerized PostgreSQL local environment.

## Repository Structure

```text
JatiStore/
├── backend/                  # Java Spring Boot App (API)
├── frontend/                 # React.js + TypeScript App (UI)
├── docker-compose.yml        # Local PostgreSQL infrastructure
└── README.md                 # This documentation file

```

---

## Prerequisites

Before getting started, ensure every team member has the following software installed locally on their machine:

1. Docker and Docker Compose v2 (Docker Desktop)
2. Java 21 JDK
3. Node.js (LTS Version)

---

## Quick Start Guide

Follow these steps in order to get the entire environment running locally.

### 1. Stand Up the Database (Docker)

Open your terminal in the root directory of this project and spin up the database container:

```bash
docker compose up -d

```

This downloads PostgreSQL 18 and launches an isolated instance on port 5432 in the background.

### 2. Run the Backend (Spring Boot + Flyway)

1. Open the backend/ folder in your preferred Java IDE.
2. Let the IDE import the Maven dependencies (pom.xml).
3. Run the main Application class.

On startup, Flyway will connect to your Docker database, initialize the flyway_schema_history table, and automatically execute our baseline database structure from `src/main/resources/db/migration/V1__initial_schema.sql`.

### 3. Run the Frontend (React + Vite)

Open a new terminal window, navigate to the frontend directory, install dependencies, and start the development server:

```bash
cd frontend
npm install
npm run dev

```

Open http://localhost:5173 in your browser to view the application.

---

## Flyway Migration Guide

To avoid breaking each other's code or local environments, follow these strict database rules using Flyway:

1. Never edit an existing SQL migration file (e.g., `V1__...`) once it has been pushed to Git. Flyway tracks file checksums; changing an existing file will crash the app for everyone else.
2. To make a change (such as adding a column or creating a new table), create a new incremental file inside `backend/src/main/resources/db/migration/`.
3. Ensure the filename uses the strict naming convention: `V2__add_description_here.sql` (Note the double underscore).
4. Pair your SQL migration file with the corresponding updates to your Java @Entity classes in the same commit.

---

## How to Switch Branches Safely

Because our local Postgres database uses a persistent data volume (pgdata), switching between feature branches with different migration histories can cause Flyway schema mismatches or validation errors.

If your backend throws a Flyway validation error after you change Git branches, follow this sequence in the project root to reset your environment:

```bash
# 1. Stop the database container and wipe out its persistent data volume
docker compose down -v

# 2. Re-create a clean, empty database container
docker compose up -d

```

Once you restart your Spring Boot application, Flyway will look at the fresh database and instantly replay only the migration files available on your current Git branch.

---

## Credentials Reference

For local development, the following credentials are pre-configured between our docker-compose.yml and application.properties:

| Parameter | Value |
| --- | --- |
| DB Host | localhost:5432 |
| DB Name | jatistore |
| Username | developer |
| Password | rahasia |

