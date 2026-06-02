package com.kaya.exception;

public class InstructorException extends RuntimeException {

    public InstructorException(String message) {
        super(message);
    }

    public static InstructorException alreadyExists(String instructor) {
        return new InstructorException("Instructor already exists: " + instructor);
    }

    public static InstructorException notFound() {
        return new InstructorException("Instructor was not found!");
    }
}
