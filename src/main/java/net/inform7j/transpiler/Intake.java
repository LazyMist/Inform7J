package net.inform7j.transpiler;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.swing.ProgressMonitor;
import net.inform7j.Logging;
import net.inform7j.Logging.Severity;
import net.inform7j.transpiler.Source.Extension;
import net.inform7j.transpiler.language.IStatement;
import net.inform7j.transpiler.language.IStatement.StatementSupplier;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory;
import net.inform7j.transpiler.language.impl.deferring.DeferringStory.CombinedParser;
import net.inform7j.transpiler.language.impl.deferring.RawBlockStatement;
import net.inform7j.transpiler.language.impl.deferring.RawLineStatement;
import net.inform7j.transpiler.tokenizer.Token;
import net.inform7j.transpiler.tokenizer.Token.SourcedToken;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenPattern.Result;
import net.inform7j.transpiler.tokenizer.TokenPredicate;
import net.inform7j.transpiler.tokenizer.TokenString;

import static net.inform7j.transpiler.language.impl.deferring.DeferringImpl.NOT_ENDMARKER_LOOP;
import static net.inform7j.transpiler.language.impl.deferring.DeferringImpl.WORD_LOOP;
import static net.inform7j.transpiler.language.impl.deferring.DeferringImpl.ENDMARKER;
import static net.inform7j.transpiler.language.impl.deferring.DeferringImpl.ENDLINE;
import static net.inform7j.transpiler.language.impl.deferring.DeferringImpl.AN;

public class Intake {
	private DeferringStory deferred;
	private Path extensions,src;

	public static record IntakeReader(
			DeferringStory trg,
			Path src,
			Source source,
			Path extensions,
			Map<String,Set<String>> explored,
			boolean stopOnError
			) implements Runnable {

		public IntakeReader(DeferringStory trg, Path src, Source source, Path extensions, boolean stopOnError) {
			this(trg, src, source, extensions, new HashMap<>(), stopOnError);
		}

		@Override
		public void run() {
			RawBlockStatement.Builder rootBuilder = new RawBlockStatement.Builder();
			Logging.log(Severity.DEBUG, "Reading: %s", src.toString());
			try(var buff = Files.newBufferedReader(src)) {
				List<SourcedToken> tokens = new LinkedList<>();
				new Token.Generator(src, source, new Token.CommentRemover(tokens::add)).absorbStream(buff.lines().map(s -> s+"\n").flatMapToInt(String::codePoints));
				List<List<SourcedToken>> lines = new LinkedList<>();
				{
					List<SourcedToken> line = new LinkedList<>();
					long lastLine = 0;
					for(SourcedToken t:tokens) {
						lastLine = t.line();
						line.add(t);
						if(t.tok().type() == Token.Type.NEWLINE) {
							lastLine++;
							lines.add(line);
							line = new LinkedList<>();
						}
					}
					if(line.isEmpty()) line.add(new SourcedToken(new Token(Token.Type.NEWLINE, "\n"), lastLine, src, source));
					lines.add(line);
				}
				Stack<RawBlockStatement.Builder> builder = new Stack<>();
				builder.push(rootBuilder);
				for(List<SourcedToken> line:lines) {
					int aind = 1;
					for(SourcedToken t:line) {
						if(t.tok().type() != Token.Type.INDENT) break;
						aind++;
					}
					while(builder.size()<aind) {
						RawBlockStatement.Builder n = new RawBlockStatement.Builder();
						builder.peek().accept(n);
						builder.push(n);
					}
					while(builder.size()>aind) {
						builder.pop();
					}
					builder.peek().accept(new RawLineStatement(line.subList(aind-1, line.size())));
				}
			} catch(IOException ex) {
				Logging.log(Statistics.ERROR_EXCEPTIONS, ex);
			}
			RawBlockStatement root = rootBuilder.get();
			List<? extends IStatement> cnt = root.blockContents();
			ProgressMonitor prog = new ProgressMonitor(null, "Parsing "+src, "0/"+cnt.size(), 0, cnt.size());
			ListIterator<? extends IStatement> iter = cnt.listIterator();
			StatementSupplier sup = new StatementSupplier(iter);
			boolean success = true;
			try {
				while(iter.hasNext() && !prog.isCanceled()) {
					sup.commit();
					IStatement s = iter.next();
					if(s instanceof RawLineStatement line) {
						Logging.log(Statistics.LINES, "Parsing line: %s", line.raw());
						success = processRootLine(line, sup);
					} else if(!s.isBlank() && success) {
						Logging.log(Statistics.UNPROCESSED_BLOCK, "Unprocessed indentation block in %s @%d:\n%s", s.src(), s.line(), s.toString(""));
						throw new UnknownLineException(String.format("Unprocessed indentation block in %s @%d:\n%s", s.src(), s.line(), s.toString("")));
					}
					prog.setProgress(iter.nextIndex());
					prog.setNote(iter.nextIndex()+"/"+cnt.size());
				}
			} finally {
				prog.close();
			}
		}

		public static final String CAPTURE_VERSION = "version",
				CAPTURE_EXT = "extension",
				CAPTURE_AUTHOR = "author",
				CAPTURE_SECTION_TYPE = "sectionType",
				CAPTURE_TITLE = "title",
				CAPTURE_INPUT = "input",
				CAPTURE_LIKELYHOOD = "likelyhood";
		public static final TokenPattern INCLUDE = TokenPattern.quoteIgnoreCase("Include")
				.concatOptionalIgnoreCase("the")
				.concat(TokenPattern.quoteIgnoreCase("version").concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_VERSION)).concatIgnoreCase("of").omittable())
				.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_EXT))
				.concatIgnoreCase("by")
				.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_AUTHOR))
				.concat(".").concat(ENDLINE),
				//Pattern.compile("^Include(?: the| Version (?<version>.+?) of)? (?<ext>.+?) by (?<author>.+?)\\.\\s*$", Pattern.CASE_INSENSITIVE)
				INCLUDE_RAW = TokenPattern.quoteIgnoreCase("Include (-").concat(ENDLINE),
				//Pattern.compile("^Include \\(-\\s*+$", Pattern.CASE_INSENSITIVE)
				INCLUDE_RAW_END = TokenPattern.quote("-)")
				.concat(TokenPattern.quoteIgnoreCase("instead of").concat(NOT_ENDMARKER_LOOP).omittable())
				.concat(ENDMARKER.omittable())
				.concat(ENDLINE),
				//Pattern.compile("-\\)(?: instead of .+?)?\\.?\\s*+$", Pattern.CASE_INSENSITIVE)
				RAW_END = TokenPattern.quote("-)").concat(ENDLINE),
				SECTION = TokenPattern.quoteIgnoreCase("Section").orIgnoreCase("Chapter").orIgnoreCase("Book").orIgnoreCase("Part").capture(CAPTURE_SECTION_TYPE)
				.concat(new TokenPattern.Single(TokenPredicate.NEWLINE.negate()).loop().capture(CAPTURE_TITLE))
				.concat(ENDLINE),
				//Pattern.compile("^(?<stype>Section|Chapter|Book|Part) (?<title>.+?)\\s*+$", Pattern.CASE_INSENSITIVE)
				VERSION = TokenPattern.quoteIgnoreCase("Version")
				.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_VERSION))
				.concatIgnoreCase("of").omittable()
				.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_EXT))
				.concat(TokenPattern.quote("(").concat(NOT_ENDMARKER_LOOP).concat(")").omittable())
				.concatIgnoreCase("by")
				.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_AUTHOR))
				.concatIgnoreCase("begins here.").concat(ENDLINE),
				//Pattern.compile("^(?:Version (?<version>.+?) of )?(?<ext>.+?)(?: \\([^\\)]++\\))? by (?<author>.+?) begins here\\.\\s*$", Pattern.CASE_INSENSITIVE)
				BOOK_START = TokenPattern.Single.STRING.capture(CAPTURE_TITLE)
				.concatIgnoreCase("by")
				.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_AUTHOR))
				.concat(ENDLINE),
				//Pattern.compile("^\"(?<title>[^\"]++)\" by (?<author>.+?)\\s*$", Pattern.CASE_INSENSITIVE)
				IGNORE = TokenPattern.quoteIgnoreCase("Release along with an interpreter.")
				.or(TokenPattern.quoteIgnoreCase("Use").concat(NOT_ENDMARKER_LOOP).concat(ENDMARKER))
				.or(TokenPattern.quoteIgnoreCase("Understand the command").concat(TokenPattern.Single.STRING.concatOptionalIgnoreCase("and").loop()).concatIgnoreCase("as something new."))
				.concat(ENDLINE),
				//Pattern.compile("^(?:Release along with an interpreter\\.|Use .+?\\.|understand the command \"[^\"\\.]++\"(?: and \"[^\"]++\")*+ as something new\\.)\\s*$", Pattern.CASE_INSENSITIVE)
				IGNORE_BLOCK = AN.concat(WORD_LOOP).concatIgnoreCase("hyperlink rule (this is the default inline hyperlink handling rule):")
				.or(AN.concatIgnoreCase("hyperlink processing rule (this is the default command replacement by hyperlinks rule):"))
				.concat(ENDLINE),
				COMMENTSTRING = TokenPattern.Single.STRING.concat(ENDLINE),
				END = NOT_ENDMARKER_LOOP.capture(CAPTURE_EXT)
				.concat(TokenPattern.quoteIgnoreCase("end").orIgnoreCase("ends"))
				.concat("here.")
				.concat(ENDLINE),
				//Pattern.compile("^(?<ext>.+?) ends? here\\.\\s*$", Pattern.CASE_INSENSITIVE)
				INPUT_LIKELYHOOD = TokenPattern.quoteIgnoreCase("Does the player mean")
				.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_INPUT))
				.concatIgnoreCase(": it is")
				.concat(NOT_ENDMARKER_LOOP.capture(CAPTURE_LIKELYHOOD))
				.concat(".").concat(ENDLINE);
		//Pattern.compile("^Does the player mean (?<input>.+?): it is (?<likelyhood>.+?)\\.\\s*$", Pattern.CASE_INSENSITIVE)

		public static boolean tailMatch(IStatement st, TokenPattern pat, boolean contained) {
			if(st instanceof RawLineStatement l) {
				return !pat.matches(l.raw()).findFirst().isEmpty();
			} else if(st instanceof RawBlockStatement b) {
				boolean ret = false;
				for(IStatement s:b.blockContents()) {
					if(contained) Logging.log_assert(!ret, Severity.WARN, "Potential Raw block end in the middle of an indentation block");
					ret = tailMatch(s, pat, true);
				}
			}
			return false;
		}

		public boolean processRootLine(final RawLineStatement line, final StatementSupplier sup) {
			TokenString lstr = new TokenString(line.raw().stream().filter(TokenPredicate.IS_WHITESPACE.negate()));
			Optional<Result> r = INCLUDE_RAW.matches(lstr).findFirst();
			if(r.isPresent()) {
				while(true) {
					Optional<? extends IStatement> opt = sup.getNextOptional(IStatement.class);
					if(opt.isEmpty()) throw new UnknownLineException("Unresolvable Raw include statement: "+line.src()+"@"+line.line());
					if(tailMatch(opt.get(), INCLUDE_RAW_END, true)) return true;
				}
			}
			r = INCLUDE.matches(lstr).findFirst();
			if(r.isPresent()) {
				final Result res = r.get();
				final TokenString auth = res.cap(CAPTURE_AUTHOR), ex = res.cap(CAPTURE_EXT);
				Predicate<String> author = Pattern.compile(Token.toPattern(auth), Pattern.CASE_INSENSITIVE).asMatchPredicate(), ext = Pattern.compile(Token.toPattern(ex)+"\\.i7x", Pattern.CASE_INSENSITIVE).asMatchPredicate();
				if(explored.computeIfAbsent(auth.toString(), s -> new HashSet<>()).add(ex.toString())) {
					try(Stream<Path> str = Files.find(extensions, 2, (Path p, BasicFileAttributes a) -> {
						return ext.test(p.getFileName().toString()) && author.test(p.getParent().getFileName().toString());
					}, FileVisitOption.FOLLOW_LINKS)) {
						Path p = str.findAny().get();
						IntakeReader rdr = new IntakeReader(trg,p,new Extension(p.getParent().getFileName().toString(), p.getFileName().toString()),extensions,explored,stopOnError);
						try {
							rdr.run();
						} catch(RuntimeException exc) {
							if(stopOnError) throw new RuntimeException(exc);
							Logging.log(Statistics.INCOMPLETE_INCLUDE, exc);
						}
					} catch(IOException exc) {
						Logging.log(Statistics.MISSING_FILES, exc);
					} catch(NoSuchElementException exc) {
						Logging.log(Statistics.MISSING_FILES, "Cannot locate %s by %s in the extensions.", ex, auth);
					}
				}
				return true;
			}
			r = SECTION.matches(lstr).findFirst();
			if(r.isPresent()) {
				final Result res = r.get();
				Logging.log(Severity.INFO, "Parsing %s \"%s\" of %s @%d", res.cap(CAPTURE_SECTION_TYPE), res.cap(CAPTURE_TITLE), line.src(), line.line());
				return true;
			}
			r = VERSION.matches(lstr).findFirst();
			if(r.isPresent()) {
				final Result res = r.get();
				TokenString ext = res.cap(CAPTURE_EXT);
				if(source instanceof Extension e) e.tokenName().set(ext);
				Logging.log(Severity.INFO, "Parsing Version %s of %s by %s @%d", res.capOpt(CAPTURE_VERSION).map(TokenString::toString).orElse("N/A"), ext, res.cap(CAPTURE_AUTHOR), line.line());
				while(true) {
					Optional<? extends RawLineStatement> opt = sup.getNextOptional(RawLineStatement.class);
					if(opt.isEmpty()) break;
					RawLineStatement rline = opt.get();
					if(!rline.isBlank()) {
						if(COMMENTSTRING.matches(rline.raw()).findFirst().isPresent()) {
							Logging.log(Severity.INFO, "%s", rline.raw());
						} else {
							sup.reverse();
							break;
						}
					}
				}
				return true;
			}
			r = BOOK_START.matches(lstr).findFirst();
			if(r.isPresent()) {
				final Result res = r.get();
				Logging.log(Severity.INFO, "Parsing %s by %s @%d", res.cap(CAPTURE_TITLE), res.cap(CAPTURE_AUTHOR), line.line());
				while(true) {
					Optional<? extends RawLineStatement> opt = sup.getNextOptional(RawLineStatement.class);
					if(opt.isEmpty()) break;
					RawLineStatement rline = opt.get();
					if(!rline.isBlank()) {
						if(COMMENTSTRING.matches(rline.raw()).findFirst().isPresent()) {
							Logging.log(Severity.INFO, "%s", rline.raw());
						} else {
							sup.reverse();
							break;
						}
					}
				}
				return true;
			}
			r = IGNORE.matches(lstr).findFirst();
			if(r.isPresent()) {
				Logging.log(Severity.INFO, "Ignoring irrelevant technical line %d in %s: %s", line.line(), line.src(), lstr);
				return true;
			}
			r = IGNORE_BLOCK.matches(lstr).findFirst();
			if(r.isPresent()) {
				Logging.log(Severity.INFO, "Ignoring irrelevant technical block in %s @ %d: %s\n%s", line.src(), line.line(), lstr, sup.getNext(IStatement.class).toString(""));
				return true;
			}
			r = END.matches(lstr).findFirst();
			if(r.isPresent()) {
				TokenString ext = r.get().cap(CAPTURE_EXT);
				Logging.log(Severity.INFO, "Finished parsing %s", ext);
				if(source instanceof Extension ex) {
					if(ex.tokenName().get().stream().anyMatch(ext::equals)) {
						while(sup.getNextOptional(IStatement.class).isPresent()) {}
					} else Logging.log(Severity.WARN, "Unrecognized end within extension: %s", ext);
				}
				return true;
			}
			r = INPUT_LIKELYHOOD.matches(lstr).findFirst();
			if(r.isPresent()) {
				final Result res = r.get();
				Logging.log(Severity.INFO, "Likelyhood assertion: %s is %s", res.cap(CAPTURE_INPUT), res.cap(CAPTURE_LIKELYHOOD));
				return true;
			}
			if(lstr.stream().anyMatch(TokenPredicate.IS_WHITESPACE.negate())) {
				boolean change;
				do {
					change = false;
					for(CombinedParser<?> p:DeferringStory.CPARSERS) {
						TokenString n = lstr;
						try{
							n = p.cparse(trg, line, sup, lstr);
						} catch(RuntimeException ex) {
							Logging.log(Statistics.ERROR_EXCEPTIONS, ex);
						}
						if(!n.equals(lstr)) {
							lstr = n;
							change = true;
							break;
						}
					}
				} while(change && !lstr.stream().allMatch(TokenPredicate.IS_WHITESPACE));
			}
			if(lstr.stream().anyMatch(TokenPredicate.IS_WHITESPACE.negate())) {
				Logging.log(Statistics.UNKNOWN_LINES, "Unable to process line %d in %s: %s", line.line(), line.src(), lstr);
				throw new UnknownLineException("Unknown line "+line.line()+" in "+line.src()+": "+lstr);
			}
			return true;
		}
	}

	public Intake(Path extensions, Path src) {
		this.extensions = extensions;
		this.src = src;
		this.deferred = new DeferringStory();
	}

	public IntakeReader createReader(boolean stopOnError) {
		return new IntakeReader(deferred, src, Source.Story.MAIN, extensions, stopOnError);
	}
}
