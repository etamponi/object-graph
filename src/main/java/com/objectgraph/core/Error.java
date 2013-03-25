package com.objectgraph.core;

public class Error {
	
	public enum ErrorLevel {
		WARNING, SEVERE
	}
	
	public final ErrorLevel level;
	
	public final String message;
	
	public Error(ErrorLevel level, String message) {
		this.level = level;
		this.message = message;
	}

	public ErrorLevel getLevel() {
		return level;
	}

	public String getMessage() {
		return message;
	}
	
	@Override
	public String toString() {
		return message + (level == ErrorLevel.WARNING ? " (warning)" : "");
	}

}
