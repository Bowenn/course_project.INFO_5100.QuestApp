# QuestApp (INFO 5100 Course Project)

A Spring Boot REST API application built with Java 25 and Maven.

## Prerequisites

- Java 25
- Maven 3.9+ (or use the Maven wrapper)

## Run now (no database setup)

The app uses **H2** by default—no MySQL required. Data is stored in `./data/questapp.mv.db`.

```bash
mvn spring-boot:run
```

- **API**: http://localhost:8080/api/health  
- **H2 Console** (optional): http://localhost:8080/h2-console  
  - JDBC URL: `jdbc:h2:file:./data/questapp`  
  - Username: `sa`  
  - Password: (leave empty)

## Switch to MySQL later

When you have MySQL installed:

1. **Update credentials** in `src/main/resources/application-mysql.properties`:
   - `spring.datasource.username`
   - `spring.datasource.password`

2. **Run with MySQL profile**:
   ```bash
   mvn spring-boot:run -Dspring.profiles.active=mysql
   ```

   Or when running the JAR:
   ```bash
   java -jar target/questapp-1.0.0-SNAPSHOT.jar --spring.profiles.active=mysql
   ```

The app will create the `questapp` database automatically if it doesn't exist.

## Build

```bash
mvn clean package
java -jar target/questapp-1.0.0-SNAPSHOT.jar
```

## Test

```bash
mvn test
```

## API

- `GET /api/health` – Health check endpoint
- See [API.md](API.md) for full task system API reference
