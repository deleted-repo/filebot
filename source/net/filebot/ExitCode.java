package net.filebot;

public interface ExitCode {

	public static final int SUCCESS = 0;

	public static final int ERROR = 1;

	public static final int BAD_LICENSE = 2;

	public static final int FAILURE = 3;

	public static final int DIE = 4;

	public static final int NOOP = 100;

	public static String getErrorMessage(int code) {
		switch (code) {
		case SUCCESS:
			return "Done";
		case ERROR:
			return "Error";
		case BAD_LICENSE:
			return "Bad License";
		case FAILURE:
			return "Failure";
		case DIE:
			return "Abort";
		case NOOP:
			return "Done";
		default:
			return String.format("Error (%d)", code);
		}
	}

	public static String getErrorKaomoji(int code) {
		switch (code) {
		case SUCCESS:
			return "ヾ(＠⌒ー⌒＠)ノ";
		case ERROR:
			return "(o_O)";
		case BAD_LICENSE:
			return "(>_<)";
		case FAILURE:
			return "(×_×)⌒☆";
		case DIE:
			return "(×_×)";
		case NOOP:
			return "¯\\_(ツ)_/¯";
		default:
			return "/╲/\\╭[☉﹏☉]╮/\\╱\\";
		}
	}

}
