# SentinelForensic – Deployment Guide

This guide covers deployment strategies for SentinelForensic:
1. **Containerized Deployment (Docker & Docker Compose)**
2. **Cloud Deployment (Render / Railway / Fly.io)**
3. **Standalone Production JAR & Static Frontend Hosting**

---

## 1. Docker & Docker Compose (Recommended)

The easiest and most robust way to deploy SentinelForensic is via Docker Compose.

### Architecture in Containers
- `sentinelforensic-db`: PostgreSQL 16 Alpine container with persistent named volume.
- `sentinelforensic-backend`: Multi-stage Java 21 build running Spring Boot on port 8081.
- `sentinelforensic-frontend`: Multi-stage Node 20 build with Nginx reverse proxy on port 80, routing `/api/` requests internally to the backend container.

### Running with Docker Compose
```bash
# Build images and start services in background
docker compose up -d --build

# View container status
docker compose ps

# View live logs
docker compose logs -f
```

Access the application at `http://localhost`.

---

## 2. Free / Low-Cost Cloud Deployment

### Option A: Railway (Unified Full-Stack)
1. Link your GitHub repository to [Railway.app](https://railway.app).
2. Add a **PostgreSQL** plugin (provides `DATABASE_URL`).
3. Deploy the **Backend**:
   - Set Root Directory: `/backend`
   - Set Environment Variables:
     - `SPRING_DATASOURCE_URL`: `${{Postgres.DATABASE_URL}}`
     - `SPRING_DATASOURCE_USERNAME`: `${{Postgres.PGUSER}}`
     - `SPRING_DATASOURCE_PASSWORD`: `${{Postgres.PGPASSWORD}}`
     - `SERVER_PORT`: `8081`
4. Deploy the **Frontend**:
   - Set Root Directory: `/frontend`
   - Set Environment Variable: `VITE_API_URL` pointing to the backend's public URL.

### Option B: Render.com
1. Create a **Free PostgreSQL Database** on Render.
2. Deploy the backend as a **Web Service** using Docker (points to `backend/Dockerfile`).
3. Deploy the frontend as a **Static Site** (Build Command: `npm install && npm run build`, Publish Directory: `dist`).

---

## 3. GitHub Actions CI/CD Pipeline

A production CI pipeline is pre-configured at `.github/workflows/ci.yml`:
- **Backend Job**: Spins up a PostgreSQL test container, sets up JDK 21, and executes all 14 JUnit 5 tests.
- **Frontend Job**: Sets up Node.js 20, installs dependencies, and runs `npm run build` to verify type-checking and bundling.
- Every commit pushed to GitHub will automatically be verified.
