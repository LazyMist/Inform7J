package net.inform7j;

import lombok.extern.slf4j.Slf4j;
import net.inform7j.transpiler.Intake;
import net.inform7j.transpiler.IntakeReader;
import net.inform7j.transpiler.Statistics;
import org.slf4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

@Slf4j
public class Main {
    public enum ParseState {
        CMD,
        EXT_PATH
    }
    
    public static void main(String[] args) {
        Path src = null;
        Path ext = null;
        ParseState state = ParseState.CMD;
        boolean stopOnError = false;
        for(String s : args) {
            switch(state) {
            case CMD:
                switch(s) {
                case "--help":
                    System.out.println("""
                                       Usage: [options...] [input-file]
                                       Options:
                                       --help				Show this message.
                                       --ext [path]			Set the path to the inform7 extensions folder.
                                       Default: /usr/share/gnome-inform7/Extensions/
                                       --stopOnError		Stops the compiler when an error occurs.
                                       """);
                    return;
                case "--ext":
                    state = ParseState.EXT_PATH;
                    break;
                case "--stopOnError":
                    stopOnError = true;
                    break;
                default:
                    if(src != null) log.warn("Multiple inputs specified.");
                    Path n = Paths.get(s);
					if(Files.exists(n)) {
						src = n;
					} else {
						log.error("{} doesn't exist", s);
					}
                    break;
                }
                break;
            case EXT_PATH:
                if(ext != null) log.warn("Extension folder path specified multiple times.");
                ext = Paths.get(s);
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
            if(!Files.exists(src)) log.error("Selected file does not exist");
        }
        Intake in = new Intake(ext, src);
        IntakeReader rdr = in.createReader(stopOnError);
        rdr.explored().computeIfAbsent("Emily Short", s -> new HashSet<>()).addAll(
            Arrays.asList(
                "Basic Screen Effects",
                "Menus", "Glulx Entry Points",
                "Glulx Text Effects",
                "Simple Graphical Window",
                "Basic Help Menu"
            )
        );
        rdr.explored().computeIfAbsent("Eric Eve", s -> new HashSet<>()).add("Text Capture");
        rdr.explored().computeIfAbsent("Nuku Valente", s -> new HashSet<>()).add("Inline Hyperlinks");
        rdr.explored().computeIfAbsent("Core Mechanics", s -> new HashSet<>()).addAll(
            Arrays.asList(
                "Graphics Director",
                "Game UI"
            )
        );
        try {
            rdr.run();
            log.info("Kinds");
            rdr.trg().streamKinds().forEachOrdered(Main::logInfoObject);
            log.info("Properties:");
            rdr.trg().streamProperties().forEachOrdered(Main::logInfoObject);
            log.info("Objects:");
            rdr.trg().streamObjects().forEachOrdered(Main::logInfoObject);
            log.info("Values:");
            rdr.trg().streamValues().forEachOrdered(Main::logInfoObject);
        } catch(RuntimeException ex) {
            Statistics.ERROR_EXCEPTIONS.prepareLog(log)
                .setCause(ex)
                .log();
        } finally {
            Statistics.printStats(Logger::atInfo);
        }
    }
    
    private static void logInfoObject(Object o) {
        log.info("{}", o);
    }
}
