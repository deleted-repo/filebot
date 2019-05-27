package net.filebot.cli;

import static java.util.stream.Collectors.*;
import static net.filebot.Execute.*;
import static net.filebot.Logging.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.script.ScriptException;

import net.filebot.ExecuteException;
import net.filebot.ExitCode;
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

	public IntStream execute(Stream<MediaBindingBean> group) {
		if (parallel) {
			return executeParallel(group);
		} else {
			return executeSequence(group);
		}
	}

	private IntStream executeSequence(Stream<MediaBindingBean> group) {
		return group.map(v -> {
			return template.stream().map(t -> getArgumentValue(t, v)).filter(Objects::nonNull).collect(toList());
		}).distinct().mapToInt(this::execute);
	}

	private IntStream executeParallel(Stream<MediaBindingBean> group) {
		// collect all bindings and combine them into a single command
		List<MediaBindingBean> bindings = group.collect(toList());

		if (bindings.isEmpty()) {
			return IntStream.empty();
		}

		// collect single command
		List<String> command = template.stream().flatMap(t -> {
			return bindings.stream().map(v -> getArgumentValue(t, v)).filter(Objects::nonNull).distinct();
		}).collect(toList());

		// execute single command
		return Stream.of(command).mapToInt(this::execute);
	}

	private int execute(List<String> command) {
		try {
			system(command, directory);
			return ExitCode.SUCCESS;
		} catch (ExecuteException e) {
			log.warning(e::getMessage);
			return e.getExitCode();
		} catch (Exception e) {
			log.warning(e::getMessage);
			return ExitCode.ERROR;
		}
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
