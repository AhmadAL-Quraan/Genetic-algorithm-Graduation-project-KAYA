package com.kaya.exception;

public class RoomException extends RuntimeException {

    public RoomException(String message) {
        super(message);
    }

    public static RoomException alreadyExists(String building, String number) {
        return new RoomException("Room already exists: " + building + " " + number);
    }

    public static RoomException notFound() {
        return new RoomException("Room was not found!");
    }
}
