package net.filebot.format;

import static net.filebot.format.ExpressionFormat.*;

import java.security.AccessController;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

public class ExpressionMapper {

	private final String expression;
	private final CompiledScript compiledExpression;

	public ExpressionMapper(String expression) throws ScriptException {
		this.expression = expression;
		this.compiledExpression = new SecureCompiledScript(compileScriptlet(asExpression(expression)));
	}

	public String getExpression() {
		return expression;
	}

	public <T> T map(Object value, Class<T> type) throws ScriptException {
		return map(new ExpressionBindings(value), type);
	}

	public <T> T map(Bindings bindings, Class<T> type) throws ScriptException {
		// use privileged bindings so we are not restricted by the script sandbox
		Bindings priviledgedBindings = PrivilegedInvocation.newProxy(Bindings.class, bindings, AccessController.getContext());

		// initialize script context with the privileged bindings
		ScriptContext context = new SimpleScriptContext();
		context.setBindings(priviledgedBindings, ScriptContext.GLOBAL_SCOPE);

		// evaluate user script
		Object value = compiledExpression.eval(context);

		// value as target type
		return (T) DefaultTypeTransformation.castToType(value, type);
	}

}
