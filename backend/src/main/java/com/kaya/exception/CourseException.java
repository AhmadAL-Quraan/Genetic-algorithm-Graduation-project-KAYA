package com.kaya.exception;

public class CourseException extends RuntimeException {

    public CourseException(String message) {
        super(message);
    }

    public static CourseException alreadyExists(String symbol, String number) {
        return new CourseException("Course already exists: " + symbol + " " + number);
    }

    public static CourseException notFound() {
        return new CourseException("Course was not found!");
    }
}