package net.inform7j.transpiler;

import java.nio.file.Path;

import lombok.extern.slf4j.Slf4j;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;

@Slf4j
public class Intake {
	private final DeferringStory deferred;
	private final Path extensions;
	private final Path src;
	
	public Intake(Path extensions, Path src) {
		this.extensions = extensions;
		this.src = src;
		this.deferred = new DeferringStory();
	}

	public IntakeReader createReader(boolean stopOnError) {
		return new IntakeReader(deferred, src, Source.Story.MAIN, extensions, stopOnError);
	}
}
