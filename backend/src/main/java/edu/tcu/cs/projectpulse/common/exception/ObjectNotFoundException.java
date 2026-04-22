package edu.tcu.cs.projectpulse.common.exception;

public class ObjectNotFoundException extends RuntimeException {

    public ObjectNotFoundException(String objectType, Object id) {
        super(objectType + " not found with id: " + id);
    }

    public ObjectNotFoundException(String message) {
        super(message);
    }
}
