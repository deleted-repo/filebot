package net.filebot.cli;

import static net.filebot.util.FileUtilities.*;

import java.io.File;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.kohsuke.args4j.spi.StringOptionHandler;

public class GroovyExpressionHandler extends StringOptionHandler {

	public GroovyExpressionHandler(CmdLineParser parser, OptionDef option, Setter<? super String> setter) {
		super(parser, option, setter);
	}

	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		setter.addValue(getStringValue(params.getParameter(0)));
		return 1;
	}

	private String getStringValue(String s) throws CmdLineException {
		// try as file path
		if (s.endsWith(".groovy")) {
			File f = new File(s);
			if (f.isFile()) {
				try {
					return readTextFile(f);
				} catch (Exception e) {
					throw new CmdLineException(owner, "Failed to read text file: " + f, e);
				}
			}
		}

		// or default to literal value
		return s;
	}

	@Override
	public String getDefaultMetaVariable() {
		return "{expression}";
	}

}
