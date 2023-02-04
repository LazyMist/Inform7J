package net.inform7j.transpiler.language;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IFunction.SignatureElement;
import net.inform7j.transpiler.language.rules.IActionRule;
import net.inform7j.transpiler.language.rules.IRule;
import net.inform7j.transpiler.language.rules.ISimpleRule;
import net.inform7j.transpiler.tokenizer.TokenString;

public interface IStory {
	public static interface Element {
		public IStory story();
		public Source source();
	}
	public static enum BaseKind {
		VOID(new TokenString("nothing"), null),
		VALUE(new TokenString("value"), null),
		TRUTH_STATE(new TokenString("truth state"),
				Arrays.asList(new TokenString("truth value")),
				VALUE),
		NUMBER(new TokenString("number"), VALUE),
		TEXT(new TokenString("text"), VALUE),
		INDEXED_TEXT(new TokenString("indexed text"), TEXT),
		SNIPPET(new TokenString("snippet"), TEXT),
		THING(new TokenString("thing"), null),
		FIGURE(new TokenString("figure"), THING),
		OBJECT(new TokenString("object"), THING),
		GRABOBJECT(new TokenString("grabobject"),
				Arrays.asList(new TokenString("grab object")),
				OBJECT),
		ROOM(new TokenString("room"), THING),
		CREATURE(new TokenString("creature"), THING),
		PERSON(new TokenString("person"), CREATURE),
		MAN(new TokenString("man"), PERSON),
		WOMAN(new TokenString("woman"), PERSON);
		public final TokenString writtenName;
		public final Set<TokenString> aliases;
		public final BaseKind parentKind;
		private BaseKind(TokenString writtenName, Collection<TokenString> aliases, BaseKind parentKind) {
			this.aliases = Collections.unmodifiableSet(new HashSet<>(aliases));
			this.writtenName = writtenName;
			this.parentKind = parentKind;
		}
		private BaseKind(TokenString writtenName, BaseKind parentKind) {
			this(writtenName, Collections.emptySet(), parentKind);
		}
	}
	public IKind getKind(TokenString name);
	public Stream<? extends IKind> streamKinds();
	public default Predicate<IKind> kindEquals(IKind k) {
		return k2 -> k.name().equals(k2.name());
	}
	public IKind getBaseKind(BaseKind bk);
	public IEnum getEnum(IEnum.Category category, TokenString name);
	public Stream<? extends IEnum> streamEnums(IEnum.Category category);
	public default Stream<? extends IEnum> streamEnums() {
		return Stream.of(IEnum.Category.values()).flatMap(this::streamEnums);
	}
	public IProperty getProperty(IKind owner, TokenString name);
	public Stream<? extends IProperty> streamProperties();
	public IObject getObject(TokenString name);
	public Stream<? extends IObject> streamObjects();
	public default IFunction getFunction(IKind returnType, Stream<? extends SignatureElement> name) {
		IFunction f = getFunction(name);
		if(f.returnType().canAssignTo(returnType)) return f;
		return null;
	}
	public IFunction getFunction(Stream<? extends SignatureElement> name);
	public Stream<? extends IFunction> streamFunctions();
	public IValue getValue(IObject owner, IProperty property);
	public IValue getValue(IObject var);
	public Stream<? extends IValue> streamValues();
	public IDefault getDefault(IObject label);
	public IDefault getDefault(IProperty property);
	public Stream<? extends IDefault> streamDefaults();
	public IAction getAction(TokenString name);
	public Stream<? extends IAction> streamActions();
	public ITable<?> getTable(TokenString name, boolean number);
	public Stream<? extends ITable<?>> streamTables();
	public <T extends Element> Stream<? extends IAlias<T>> getAliases(Class<T> clazz, Predicate<? super T> selector);
	public Stream<? extends IAlias<?>> streamAliases();
	public Stream<? extends IActionRule> getActionRules(IAction action);
	public default Stream<? extends IActionRule> getActionRules(IAction action, IActionRule.ActionTrigger trigger) {
		return getActionRules(action).filter(r -> r.trigger() == trigger);
	}
	public IRule getNamedRule(TokenString name);
	public Stream<? extends ISimpleRule> getSimpleRules(ISimpleRule.SimpleTrigger trigger);
	public Stream<? extends IRule> streamRules();
}
