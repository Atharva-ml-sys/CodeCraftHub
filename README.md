# CodeCraftHub Learning Management System

A beginner-friendly REST API for tracking learning goals and courses, built with Java and Spring Boot. Course data is stored in `courses.json`; no database is required.

## Features
- Full CRUD operations for courses
- JSON file storage
- Automatically generated course IDs and creation timestamps
- Validation for required fields, dates, and statuses
- Helpful HTTP error responses

## Requirements
- Java JDK 17 or higher
- Apache Maven 3.6.3 or higher

Check your setup:
```bash
java -version
mvn -version
```

## Build and run
Run these commands from the directory containing `pom.xml`:
```bash
mvn clean install
mvn spring-boot:run
```

The API runs at `http://localhost:8080`. The `courses.json` file is automatically created in the working directory on first use.

## API endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/courses` | Add a course |
| GET | `/api/courses` | List all courses |
| GET | `/api/courses/{id}` | Get one course |
| PUT | `/api/courses/{id}` | Update supplied fields |
| DELETE | `/api/courses/{id}` | Delete a course |

Valid status values are exactly `Not Started`, `In Progress`, and `Completed`.

### Add a course
```bash
curl -X POST http://localhost:8080/api/courses \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Python Basics",
    "description": "Learn Python fundamentals",
    "target_date": "2026-12-31",
    "status": "Not Started"
  }'
```

### List courses
```bash
curl http://localhost:8080/api/courses
```

### Get course ID 1
```bash
curl http://localhost:8080/api/courses/1
```

### Update course ID 1
```bash
curl -X PUT http://localhost:8080/api/courses/1 \
  -H "Content-Type: application/json" \
  -d '{"status":"In Progress"}'
```

### Delete course ID 1
```bash
curl -X DELETE http://localhost:8080/api/courses/1
```

## Notes
- Start the app from the project root so `courses.json` is created there.
- Keep the server running in one terminal while testing requests from another.
- Stop the server with `Ctrl+C`.
