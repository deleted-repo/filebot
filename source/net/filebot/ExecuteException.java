package net.filebot;

import java.io.IOException;
import java.util.List;

public class ExecuteException extends IOException {

	private int exitCode;

	public ExecuteException(String message, int exitCode) {
		super(message);
		this.exitCode = exitCode;
	}

	public ExecuteException(List<String> command, int exitCode) {
		this(String.format("%s failed (%d)", command, exitCode), exitCode);
	}

	public int getExitCode() {
		return exitCode;
	}

}
