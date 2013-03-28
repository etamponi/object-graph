package com.objectgraph.jobsystem.exceptions;

public class JobNotExistsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public JobNotExistsException(String message) {
        super(message);
    }

}
