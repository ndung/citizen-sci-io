# citizen-sci-io
The *citizen-sci-io* repository hosts a Spring Boot–based platform for citizen‑science data collection. Its Maven configuration establishes the project as “Citizen Science,” using Java 17 and Spring Boot 3.5.3 with web, security, JPA, and S3 dependencies.
The application models field records that capture unique IDs, geographic coordinates, accuracy, and user/project associations; each record can store images and other metadata for later verification.
A REST API endpoint lets authenticated clients upload a record’s metadata, images, and other data. Uploaded files are stored through a configurable storage service and linked to the metadata, enabling rich, geotagged submissions.
Security is enforced through JWT: all `/api/**` routes require tokens, with a custom filter ensuring stateless authentication.
Deployment settings allow files to be stored locally or on AWS S3, and credentials and database properties are pulled from environment variables via configuration via `application.yml`.

URL Demo: https://citizen-sci-io-c296af702ec9.herokuapp.com

## Login for admin
- username: admin
- password: citizen

## Login for user
- username: citizen
- password: scientist

Exposed API endpoints:
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