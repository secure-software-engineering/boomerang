package boomerang;

import boomerang.ifdssolver.ILogger;

public class EmptyLogger implements ILogger {

	@Override
	public void log(String format, Object... args) {
	}

}
