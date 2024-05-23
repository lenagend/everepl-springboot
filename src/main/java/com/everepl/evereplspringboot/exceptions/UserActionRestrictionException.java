package com.everepl.evereplspringboot.exceptions;

public class UserActionRestrictionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UserActionRestrictionException(String message) {
        super(message);
    }
}
