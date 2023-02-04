package net.inform7j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.inform7j.Logging.Severity;
import net.inform7j.transpiler.Intake;
import net.inform7j.transpiler.Intake.IntakeReader;
import net.inform7j.transpiler.Statistics;

public class Main {
	public static enum ParseState {
		CMD,
		EXT_PATH,
		LOG_SEVERITY;
	}

	public static void main(String[] args) {
		Path src = null, ext = null;
		ParseState state = ParseState.CMD;
		boolean stopOnError = false;
		for(String s:args) {
			switch(state) {
			case CMD:
				switch(s) {
				case "--help":
					System.out.println("""
							Usage: [options...] [input-file]
							Options:
							--help				Show this message.
							--ext [path]		Set the path to the inform7 extensions folder.
							Default: /usr/share/gnome-inform7/Extensions/
							--log [severity]	Set the minimum severity of logged messages.
							Severities are DEBUG, INFO, WARN, ERROR, FATAL
							Default: WARN
							--stopOnError		Stops the compiler when an error occurs.
							""");
					return;
				case "--ext":
					state = ParseState.EXT_PATH;
					break;
				case "--log":
					state = ParseState.LOG_SEVERITY;
					break;
				case "--stopOnError":
					stopOnError = true;
					break;
				default:
					Logging.log_assert(src == null, Severity.WARN, "Multiple inputs specified.");
					Path n = Paths.get(s);
					if(Logging.log_assert(Files.exists(n), Severity.ERROR, "%s doesn't exist", s)) src = n;
					break;
				}
				break;
			case EXT_PATH:
				Logging.log_assert(ext == null, Severity.WARN, "Extension folder path specified multiple times.");
				ext = Paths.get(s);
				state = ParseState.CMD;
				break;
			case LOG_SEVERITY:
				Logging.MINIMUM_SEVERITY = Severity.valueOf(s);
				state = ParseState.CMD;
				break;
			}
		}
		if(ext == null) ext = Paths.get("/usr/share/gnome-inform7/Extensions/");
		if(src == null) {
			JFileChooser jfc = new JFileChooser();
			jfc.setFileFilter(new FileNameExtensionFilter("Inform 7", "ni", "i7x"));
			if(jfc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;
			src = jfc.getSelectedFile().toPath();
		}
		Intake in = new Intake(ext, src);
		IntakeReader rdr = in.createReader(stopOnError);
		rdr.explored().computeIfAbsent("Emily Short", s -> new HashSet<>()).addAll(
				Arrays.asList(
						"Basic Screen Effects",
						"Menus", "Glulx Entry Points",
						"Glulx Text Effects",
						"Simple Graphical Window",
						"Basic Help Menu")
				);
		rdr.explored().computeIfAbsent("Eric Eve", s -> new HashSet<>()).add("Text Capture");
		rdr.explored().computeIfAbsent("Nuku Valente", s -> new HashSet<>()).add("Inline Hyperlinks");
		rdr.explored().computeIfAbsent("Core Mechanics", s -> new HashSet<>()).addAll(
				Arrays.asList(
						"Graphics Director",
						"Game UI")
				);
		try{
			rdr.run();
			System.out.println("Kinds:");
			rdr.trg().streamKinds().forEachOrdered(System.out::println);
			System.out.println("\nProperties:");
			rdr.trg().streamProperties().forEachOrdered(System.out::println);
			System.out.println("\nObjects:");
			rdr.trg().streamObjects().forEachOrdered(System.out::println);
			System.out.println("\nValues:");
			rdr.trg().streamValues().forEachOrdered(System.out::println);
		} catch(RuntimeException ex) {
			Logging.log(Statistics.ERROR_EXCEPTIONS, ex);
		} finally {
			Statistics.printStats(Logging.OUT);
		}
	}

}
