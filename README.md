# citizen-sci-io
The *citizen-sci-io* repository hosts a Spring Boot–based platform for citizen-science data collection. Its Maven configuration establishes the project as “Citisci,” using Java 17 and Spring Boot 3.5.3 with web, security, JPA, PostgreSQL, local storage, and optional S3 dependencies.
The application models field records that capture unique IDs, geographic coordinates and user/project associations; each record can store images and other metadata for later verification.
A REST API endpoint lets authenticated clients upload a record’s metadata, images, and other data. Uploaded files are stored through a configurable storage service and linked to the metadata, enabling rich, geotagged submissions.
Security is enforced through JWT: all `/api/**` routes require tokens, with a custom filter ensuring stateless authentication.
Deployment settings allow files to be stored locally or on AWS S3, and credentials and database properties are pulled from environment variables via configuration via `application.yml`.

URL Demo: https://citizen-sci-io-c296af702ec9.herokuapp.com

## Docker installation

The easiest local installation is Docker Compose. It builds the Spring Boot app, starts PostgreSQL, creates the `citizen-sci-io` database on first run, and seeds the first users and menu entries from `src/main/resources/data.sql`.

```bash
docker compose up --build
```

Open the app at http://localhost:80/login

## Login for admin
- username: admin
- password: citizen

## Login for user
- username: citizen
- password: scientist

The Docker database credentials are:

```text
database: citizen-sci-io
username: citisci
password: citisci
host: localhost
port: 5432
```

For production, change the PostgreSQL password and replace `JWT_SECRET` in `docker-compose.yml` with your own Base64-encoded secret of at least 32 bytes.

## Manual PostgreSQL installation

Install Java 17, Maven, and PostgreSQL. Create the first database and application user:

```sql
CREATE DATABASE "citizen-sci-io";
CREATE USER citisci WITH ENCRYPTED PASSWORD 'change-me';
GRANT ALL PRIVILEGES ON DATABASE "citizen-sci-io" TO citisci;
\c "citizen-sci-io"
GRANT ALL ON SCHEMA public TO citisci;
```

Set the application environment variables, then start the app:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/citizen-sci-io"
$env:SPRING_DATASOURCE_USERNAME = "citisci"
$env:SPRING_DATASOURCE_PASSWORD = "change-me"
$env:JWT_SECRET = "Y2l0aXNjaS1sb2NhbC1kZXZlbG9wbWVudC1qd3Qtc2VjcmV0LTEyMzQ1Ng=="
$env:APP_STORAGE_TYPE = "local"
$env:APP_STORAGE_LOCAL_BASE_PATH = "D:\citizen-sci-io\uploads"
$env:APP_STORAGE_LOCAL_BASE_URL = "http://localhost:80/file/"
$env:PORT = "80"
mvn spring-boot:run
```

Hibernate creates or updates the PostgreSQL schema with `spring.jpa.hibernate.ddl-auto=update`. After the schema exists, Spring Boot runs `src/main/resources/data.sql`, which inserts the initial `app_user`, `user_roles`, `menu`, and `menu_roles` rows.

## Exposed API endpoints:
`/api/auth/check-credential-id`
`/api/auth/sign-in`
`/api/auth/sign-up`
`/api/user/change-pwd`
`/api/user/update-profile`
`/api/projects`
`/api/record/upload`
`/api/record/list-by-project`
`/api/record/list-by-user`
`/api/record/project-summary`
`/api/record/user-summary`