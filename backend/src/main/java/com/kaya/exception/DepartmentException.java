package com.kaya.exception;

public class DepartmentException extends RuntimeException {

    public DepartmentException(String message) {
        super(message);
    }

    public static DepartmentException alreadyExists(String name, String code) {
        return new DepartmentException("Department already exists: " + name + " " + code);
    }

    public static DepartmentException notFound() {
        return new DepartmentException("Department was not found!");
    }
}
