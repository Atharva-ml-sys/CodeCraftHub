package com.codecrafthub.controller;

import com.codecrafthub.model.Course;
import com.codecrafthub.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class CourseController {
    private static final List<String> VALID_STATUSES =
            Arrays.asList("Not Started", "In Progress", "Completed");

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/courses")
    public ResponseEntity<Map<String, Object>> getAllCourses() {
        try {
            List<Course> courses = courseService.loadCourses();
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("count", courses.size());
            response.put("courses", courses);
            return ResponseEntity.ok(response);
        } catch (IOException | RuntimeException e) {
            return error("Failed to retrieve courses. Check courses.json: " + safeMessage(e),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/courses/{id}")
    public ResponseEntity<Map<String, Object>> getCourse(@PathVariable Integer id) {
        try {
            Optional<Course> course = courseService.loadCourses().stream()
                    .filter(c -> Objects.equals(c.getId(), id))
                    .findFirst();

            if (course.isEmpty()) {
                return error("Course with ID " + id + " not found", HttpStatus.NOT_FOUND);
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("course", course.get());
            return ResponseEntity.ok(response);
        } catch (IOException | RuntimeException e) {
            return error("Failed to retrieve course: " + safeMessage(e),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/courses")
    public ResponseEntity<Map<String, Object>> addCourse(@RequestBody(required = false) Course course) {
        if (course == null) {
            return error("Request body is required", HttpStatus.BAD_REQUEST);
        }

        String validationError = validateRequiredCourse(course);
        if (validationError != null) {
            return error(validationError, HttpStatus.BAD_REQUEST);
        }

        try {
            List<Course> courses = courseService.loadCourses();
            course.setId(courseService.getNextId(courses));
            course.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            courses.add(course);
            courseService.saveCourses(courses);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "Course added successfully");
            response.put("course", course);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException | RuntimeException e) {
            return error("Failed to add course: " + safeMessage(e),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<Map<String, Object>> updateCourse(
            @PathVariable Integer id,
            @RequestBody(required = false) Course updatedCourse) {
        if (updatedCourse == null) {
            return error("Request body is required", HttpStatus.BAD_REQUEST);
        }

        String validationError = validateOptionalCourse(updatedCourse);
        if (validationError != null) {
            return error(validationError, HttpStatus.BAD_REQUEST);
        }

        try {
            List<Course> courses = courseService.loadCourses();
            Optional<Course> match = courses.stream()
                    .filter(c -> Objects.equals(c.getId(), id))
                    .findFirst();

            if (match.isEmpty()) {
                return error("Course with ID " + id + " not found", HttpStatus.NOT_FOUND);
            }

            Course course = match.get();
            if (updatedCourse.getName() != null) course.setName(updatedCourse.getName());
            if (updatedCourse.getDescription() != null) course.setDescription(updatedCourse.getDescription());
            if (updatedCourse.getTargetDate() != null) course.setTargetDate(updatedCourse.getTargetDate());
            if (updatedCourse.getStatus() != null) course.setStatus(updatedCourse.getStatus());

            courseService.saveCourses(courses);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "Course updated successfully");
            response.put("course", course);
            return ResponseEntity.ok(response);
        } catch (IOException | RuntimeException e) {
            return error("Failed to update course: " + safeMessage(e),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<Map<String, Object>> deleteCourse(@PathVariable Integer id) {
        try {
            List<Course> courses = courseService.loadCourses();
            Optional<Course> match = courses.stream()
                    .filter(c -> Objects.equals(c.getId(), id))
                    .findFirst();

            if (match.isEmpty()) {
                return error("Course with ID " + id + " not found", HttpStatus.NOT_FOUND);
            }

            Course deleted = match.get();
            courses.remove(deleted);
            courseService.saveCourses(courses);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "Course deleted successfully");
            response.put("deleted_course", deleted);
            return ResponseEntity.ok(response);
        } catch (IOException | RuntimeException e) {
            return error("Failed to delete course: " + safeMessage(e),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String validateRequiredCourse(Course course) {
        if (isBlank(course.getName())) return "Missing required field: name";
        if (isBlank(course.getDescription())) return "Missing required field: description";
        if (isBlank(course.getTargetDate())) return "Missing required field: target_date";
        if (isBlank(course.getStatus())) return "Missing required field: status";
        return validateOptionalCourse(course);
    }

    private String validateOptionalCourse(Course course) {
        if (course.getName() != null && course.getName().trim().isEmpty())
            return "Field name cannot be blank";
        if (course.getDescription() != null && course.getDescription().trim().isEmpty())
            return "Field description cannot be blank";

        if (course.getTargetDate() != null) {
            try {
                LocalDate.parse(course.getTargetDate());
            } catch (DateTimeParseException e) {
                return "target_date must use YYYY-MM-DD format";
            }
        }

        if (course.getStatus() != null && !VALID_STATUSES.contains(course.getStatus())) {
            return "Status must be one of: " + String.join(", ", VALID_STATUSES);
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private ResponseEntity<Map<String, Object>> error(String message, HttpStatus status) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("error", message);
        return ResponseEntity.status(status).body(response);
    }

    private String safeMessage(Exception e) {
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }
}
