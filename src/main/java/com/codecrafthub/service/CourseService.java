package com.codecrafthub.service;

import com.codecrafthub.model.Course;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CourseService {
    // The file is created in the directory from which the app is launched.
    private static final String DATA_FILE = "courses.json";
    private final ObjectMapper objectMapper;

    public CourseService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Load courses from JSON, creating an empty file on first run. */
    public List<Course> loadCourses() throws IOException {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            saveCourses(new ArrayList<>());
            return new ArrayList<>();
        }

        if (file.length() == 0) {
            return new ArrayList<>();
        }

        return objectMapper.readValue(file, new TypeReference<List<Course>>() {});
    }

    /** Persist all courses to the JSON file. */
    public void saveCourses(List<Course> courses) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(DATA_FILE), courses);
    }

    /** Return the next ID, beginning at 1. */
    public Integer getNextId(List<Course> courses) {
        return courses.stream()
                .map(Course::getId)
                .filter(id -> id != null)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0) + 1;
    }
}
