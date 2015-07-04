package com.xafero.dynjc.core;

import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogCompilationProgress extends CompilationProgress {

	private static final Logger log = LoggerFactory.getLogger("ecj");

	@Override
	public void begin(int remainingWork) {
		log.info("begin = {}", remainingWork);
	}

	@Override
	public void done() {
		log.info("done");
	}

	@Override
	public boolean isCanceled() {
		// No cancellation!
		return false;
	}

	@Override
	public void setTaskName(String name) {
		log.info("task = {}", name);
	}

	@Override
	public void worked(int workIncrement, int remainingWork) {
		log.info("worked = {} / {}", workIncrement, remainingWork);
	}
}