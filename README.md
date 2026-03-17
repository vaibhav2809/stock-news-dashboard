# Stock News Dashboard

> A real-time stock news aggregation platform with sentiment analysis, watchlists, custom alerts, JWT authentication, and rate limiting. Supports US and Indian (NSE/BSE) stock markets. Deployed on Railway.

## Quick Start

```bash
# 1. Clone
git clone https://github.com/vaibhav2809/stock-news-dashboard.git
cd stock-news-dashboard

# 2. Set up environment
cp .env.example .env
# Edit .env — add your Finnhub and NewsData.io API keys

# 3. Start infrastructure
docker compose up -d

# 4. Start backend (Terminal 1)
cd backend && ./gradlew bootRun

# 5. Start frontend (Terminal 2)
cd frontend && npm install && npm run dev

# 6. Open http://localhost:5173
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React 19, Vite 8, TypeScript, Tailwind CSS v4, TanStack Query v5, Zustand, Recharts |
| Backend | Spring Boot 3.4.3, Java 21, Spring Security, Spring Data JPA |
| Database | PostgreSQL 16 |
| Cache | Redis 7 |
| Auth | JWT (jjwt) |
| Real-time | WebSocket (STOMP over SockJS) |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Migrations | Flyway |
| PDF Export | OpenHTMLToPDF |
| Infrastructure | Docker Compose |

## Documentation

- [Full Project Documentation](docs/PROJECT.md) — architecture, decisions, how it works
- [Development Plan](docs/DEVELOPMENT_PLAN.md) — 9-phase roadmap with detailed specs
- [Build Steps](docs/BUILD_STEPS.md) — step-by-step recreation guide
- [Onboarding Guide](docs/ONBOARDING.md) — new developer setup guide
- [Change Log](docs/CHANGES.md) — history of all changes
- [Deployment Guide](docs/DEPLOYMENT.md) — production Docker deployment instructions
- [Troubleshooting](docs/TROUBLESHOOTING.md) — issues encountered and how they were solved

## Environment Setup

Copy `.env.example` to `.env` and fill in your values:
- `FINNHUB_API_KEY` — free at [finnhub.io](https://finnhub.io/register)
- `NEWSDATA_API_KEY` — free at [newsdata.io](https://newsdata.io/register)
- `JWT_SECRET` — any random 32+ character string

See `.env.example` for all available variables with descriptions.

## Scripts

### Backend
| Command | Description |
|---------|-------------|
| `./gradlew bootRun` | Start the backend dev server (port 8080) |
| `./gradlew test` | Run all backend tests |
| `./gradlew build` | Build the backend JAR |

### Frontend
| Command | Description |
|---------|-------------|
| `npm run dev` | Start the frontend dev server (port 5173) |
| `npm run build` | Build for production |
| `npm run preview` | Preview production build |
| `npm test` | Run frontend tests |
| `npm run lint` | Lint TypeScript code |
| `npm run typecheck` | Run TypeScript type checking |

### Infrastructure
| Command | Description |
|---------|-------------|
| `docker compose up -d` | Start PostgreSQL + Redis |
| `docker compose down` | Stop all containers |
| `docker compose logs -f` | Tail container logs |

## Production Deployment

### Railway (Recommended)
The app is deployed on [Railway](https://railway.app) with two services (backend + frontend) and a managed PostgreSQL database. Railway handles builds, deployments, and SSL automatically.

See [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) for Railway setup instructions and environment variable configuration.

### Self-Hosted Docker
```bash
cp .env.example .env                                        # Fill in real values
docker compose -f docker-compose.prod.yml up -d --build     # Build and start all services
```

This starts PostgreSQL, Redis, the Spring Boot backend, and an Nginx frontend container on port 80. See [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) for the full guide including environment variables, updating, monitoring, backups, and SSL setup.

## API Documentation

- Swagger UI: http://localhost:8080/swagger-ui.html (when backend is running)
- OpenAPI JSON: http://localhost:8080/api-docs

## Contributing

1. Create a feature branch: `git checkout -b feat/your-feature`
2. Follow the coding standards in `CLAUDE.md`
3. Commit with conventional format: `feat(scope): description`
4. Push and create a pull request
5. Ensure all tests pass before requesting review

### Branch Naming
- `feat/` — new features
- `fix/` — bug fixes
- `refactor/` — code improvements
- `docs/` — documentation changes
- `test/` — test additions or fixes