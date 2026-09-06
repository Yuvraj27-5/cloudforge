# backend

Spring Boot API and deployment orchestration.

```powershell
.\mvnw.cmd spring-boot:run   # http://localhost:8080
.\mvnw.cmd test
```

Requires PostgreSQL from the root `docker-compose.yml`.

Packages are organised by feature (`system/`, then `project/`, `deployment/`,
`risk/`), each holding its own controller, service, repository, and DTOs. Shared
concerns live in `common/`.

Schema is owned by Flyway (`src/main/resources/db/migration`). Hibernate runs with
`ddl-auto: validate` and never modifies the database.
