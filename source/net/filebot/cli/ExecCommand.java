package net.filebot.cli;

import static java.util.stream.Collectors.*;
import static net.filebot.Execute.*;
import static net.filebot.Logging.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.script.ScriptException;

import net.filebot.format.ExpressionFormat;
import net.filebot.format.MediaBindingBean;

public class ExecCommand {

	private List<ExpressionFormat> template;
	private boolean parallel;

	private File directory;

	public ExecCommand(List<ExpressionFormat> template, boolean parallel, File directory) {
		this.template = template;
		this.parallel = parallel;
		this.directory = directory;
	}

	public void execute(MediaBindingBean... group) throws IOException {
		if (parallel) {
			executeParallel(group);
		} else {
			executeSequence(group);
		}
	}

	private void executeSequence(MediaBindingBean... group) throws IOException {
		// collect unique commands
		List<List<String>> commands = Stream.of(group).map(v -> {
			return template.stream().map(t -> getArgumentValue(t, v)).filter(Objects::nonNull).collect(toList());
		}).distinct().collect(toList());

		// execute unique commands
		for (List<String> command : commands) {
			system(command, directory);
		}
	}

	private void executeParallel(MediaBindingBean... group) throws IOException {
		// collect single command
		List<String> command = template.stream().flatMap(t -> {
			return Stream.of(group).map(v -> getArgumentValue(t, v)).filter(Objects::nonNull).distinct();
		}).collect(toList());

		// execute single command
		system(command, directory);
	}

	private String getArgumentValue(ExpressionFormat template, MediaBindingBean variables) {
		try {
			return template.format(variables);
		} catch (Exception e) {
			debug.warning(cause(template.getExpression(), e));
		}
		return null;
	}

	public static ExecCommand parse(List<String> args, File directory) throws ScriptException {
		// execute one command per file or one command with many file arguments
		boolean parallel = args.lastIndexOf("+") == args.size() - 1;

		if (parallel) {
			args = args.subList(0, args.size() - 1);
		}

		List<ExpressionFormat> template = new ArrayList<ExpressionFormat>();
		for (String argument : args) {
			template.add(new ExpressionFormat(argument));
		}

		return new ExecCommand(template, parallel, directory);
	}

}
