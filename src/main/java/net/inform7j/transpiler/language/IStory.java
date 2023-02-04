package net.inform7j.transpiler.language;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.IFunction.SignatureElement;
import net.inform7j.transpiler.language.rules.IActionRule;
import net.inform7j.transpiler.language.rules.IRule;
import net.inform7j.transpiler.language.rules.ISimpleRule;
import net.inform7j.transpiler.tokenizer.TokenString;

public interface IStory {
	interface Element {
		IStory story();
		Source source();
	}
	enum BaseKind {
		VOID(new TokenString("nothing"), null),
		VALUE(new TokenString("value"), null),
		TRUTH_STATE(new TokenString("truth state"),
			List.of(new TokenString("truth value")),
				VALUE),
		NUMBER(new TokenString("number"), VALUE),
		TEXT(new TokenString("text"), VALUE),
		INDEXED_TEXT(new TokenString("indexed text"), TEXT),
		SNIPPET(new TokenString("snippet"), TEXT),
		RULEBOOK(new TokenString("rulebook"), null),
		THING(new TokenString("thing"), null),
		FIGURE(new TokenString("figure"), THING),
		OBJECT(new TokenString("object"), THING),
		GRABOBJECT(new TokenString("grabobject"),
			List.of(new TokenString("grab object")),
				OBJECT),
		ROOM(new TokenString("room"), THING),
		CREATURE(new TokenString("creature"), THING),
		PERSON(new TokenString("person"), CREATURE),
		MAN(new TokenString("man"), PERSON),
		WOMAN(new TokenString("woman"), PERSON);
		public final TokenString writtenName;
		public final Set<TokenString> aliases;
		public final BaseKind parentKind;
		BaseKind(TokenString writtenName, Collection<TokenString> aliases, BaseKind parentKind) {
			this.aliases = Collections.unmodifiableSet(new HashSet<>(aliases));
			this.writtenName = writtenName;
			this.parentKind = parentKind;
		}
		BaseKind(TokenString writtenName, BaseKind parentKind) {
			this(writtenName, Collections.emptySet(), parentKind);
		}
	}
	IKind getKind(TokenString name);
	Stream<? extends IKind> streamKinds();
	default Predicate<IKind> kindEquals(IKind k) {
		return k2 -> k.name().equals(k2.name());
	}
	IKind getBaseKind(BaseKind bk);
	IEnum getEnum(IEnum.Category category, TokenString name);
	Stream<? extends IEnum> streamEnums(IEnum.Category category);
	default Stream<? extends IEnum> streamEnums() {
		return Stream.of(IEnum.Category.values()).flatMap(this::streamEnums);
	}
	IProperty getProperty(IKind owner, TokenString name);
	Stream<? extends IProperty> streamProperties();
	IObject getObject(TokenString name);
	Stream<? extends IObject> streamObjects();
	default IFunction getFunction(IKind returnType, Stream<? extends SignatureElement> name) {
		IFunction f = getFunction(name);
		if(f.returnType().canAssignTo(returnType)) return f;
		return null;
	}
	IFunction getFunction(Stream<? extends SignatureElement> name);
	Stream<? extends IFunction> streamFunctions();
	IValue getValue(IObject owner, IProperty property);
	IValue getValue(IObject var);
	Stream<? extends IValue> streamValues();
	IDefault getDefault(IObject label);
	IDefault getDefault(IProperty property);
	Stream<? extends IDefault> streamDefaults();
	IAction getAction(TokenString name);
	Stream<? extends IAction> streamActions();
	ITable<?> getTable(TokenString name, boolean number);
	Stream<? extends ITable<?>> streamTables();
	<T extends Element> Stream<? extends IAlias<T>> getAliases(Class<T> clazz, Predicate<? super T> selector);
	Stream<? extends IAlias<?>> streamAliases();
	Stream<? extends IActionRule> getActionRules(IAction action);
	default Stream<? extends IActionRule> getActionRules(IAction action, IActionRule.ActionTrigger trigger) {
		return getActionRules(action).filter(r -> r.trigger() == trigger);
	}
	IRule getNamedRule(TokenString name);
	Stream<? extends ISimpleRule> getSimpleRules(ISimpleRule.SimpleTrigger trigger);
	Stream<? extends IRule> streamRules();
}
