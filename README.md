# DevPilot

AI-powered developer growth and career preparation platform.

DevPilot integrates scattered developer data — GitHub, blogs, study logs, job postings, and resumes — into one place and provides personalized career coaching with AI.

## Tech Stack

| Layer | Technologies |
|-------|----------------|
| Backend | Java 21, Spring Boot, Spring Security, JPA, MySQL, Redis |
| Frontend | React, TypeScript, Vite, Tailwind CSS |
| Infra | Docker, GitHub Actions, AWS |
| External APIs | GitHub API, OpenAI API |

## Project Structure

```
devpilot/
├── backend/            # Spring Boot API
├── frontend/           # React + Vite web app
├── docker/             # Local MySQL, Redis (docker-compose)
├── .github/workflows/  # CI/CD pipelines
├── .env.example        # Environment variable template
└── README.md
```

## Prerequisites

- Java 21
- Node.js 20+
- Docker Desktop
- Git

## Getting Started

> Setup instructions will be added as each layer is initialized.

1. Copy environment variables: `cp .env.example .env`
2. Start local infrastructure: `docker compose -f docker/docker-compose.yml up -d`
3. Run backend: TBD
4. Run frontend: TBD

## Development Rules

- Backend-first development
- Layered architecture: Controller → Service → Repository → Domain
- RESTful API with common response format and global exception handling
- Frontend: simple, mobile-friendly dashboard layout
- Server state: TanStack Query
- Global client state: Zustand (only when necessary)

## License

Private project.
