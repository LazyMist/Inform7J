package net.inform7j.transpiler.language.impl.deferring;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import net.inform7j.transpiler.Statistics;
import net.inform7j.transpiler.language.*;
import net.inform7j.transpiler.language.IStory;
import net.inform7j.transpiler.language.ITable;
import net.inform7j.transpiler.language.impl.deferring.DeferringTable.DeferringContinuation;
import net.inform7j.transpiler.language.impl.deferring.rules.DeferringActionRule;
import net.inform7j.transpiler.language.impl.deferring.rules.DeferringRule;
import net.inform7j.transpiler.language.impl.deferring.rules.DeferringSimpleRule;
import net.inform7j.transpiler.language.rules.ISimpleRule;
import net.inform7j.transpiler.tokenizer.Token;
import net.inform7j.transpiler.tokenizer.TokenPattern;
import net.inform7j.transpiler.tokenizer.TokenString;
import net.inform7j.transpiler.tokenizer.pattern.CaptureReplacement;
import net.inform7j.transpiler.tokenizer.pattern.Conjunction;
import net.inform7j.transpiler.util.CombinedCollection;
import net.inform7j.transpiler.util.LazyLookup;
import net.inform7j.transpiler.util.MappedCollection;
import net.inform7j.transpiler.util.MappedList;

@Slf4j
public class DeferringStory implements IStory {
    private final Map<TokenString, DeferringKind> kinds = new HashMap<>();
    public static final String KIND_NAME_REPLACEMENT = "kind_names";
    private final EnumMap<BaseKind, DeferringKind> basekinds = new EnumMap<>(BaseKind.class);
    {
        for(BaseKind b : BaseKind.values()) {
            DeferringKind d = b.parentKind == null ? new DeferringKind(
                this,
                null,
                b.writtenName
            ) : new DeferringKind(
                this,
                null,
                b.writtenName,
                basekinds.get(b.parentKind)
            );
            basekinds.put(b, d);
            addKind(d);
            for(TokenString alias : b.aliases) kinds.putIfAbsent(alias, d);
        }
        
    }
    private final EnumMap<IEnum.Category, Map<TokenString, DeferringEnum>> enums = new EnumMap<>(IEnum.Category.class);
    {
        for(IEnum.Category c : IEnum.Category.values()) {
            enums.put(c, new HashMap<>());
        }
    }
    
    private final TokenPattern singleKindNames = new Conjunction(
        new MappedCollection<>(
            new CombinedCollection<>(kinds.keySet(), enums.get(IEnum.Category.KIND).keySet()),
            TokenPattern::quoteIgnoreCase
        )
    );
    private final TokenPattern pluralKindNames = new Conjunction(
        new MappedCollection<>(
            new CombinedCollection<>(kinds.keySet(), enums.get(IEnum.Category.KIND).keySet()),
            ((UnaryOperator<TokenString>) TokenString::pluralize).andThen(TokenPattern::quoteIgnoreCase)
        )
    );
    private final TokenPattern kindNames = TokenPattern.quoteIgnoreCase("list of").loop().omittable()
        .concat(new Conjunction(Set.of(singleKindNames, pluralKindNames)));
    private final Map<TokenString, Map<TokenString, DeferringProperty>> properties = new HashMap<>();
    {
        addBuiltinProperty(new DeferringProperty(this, null, BaseKind.FIGURE, new TokenString("file"), BaseKind.TEXT));
        addBuiltinProperty(
            new DeferringProperty(this, null, BaseKind.ROOM, new TokenString("fast travel"), BaseKind.TRUTH_STATE),
            new TokenString(Token.Generator.parseLiteral("fasttravel"))
        );
        addBuiltinProperty(new DeferringProperty(
            this,
            null,
            BaseKind.ROOM,
            new TokenString("private"),
            BaseKind.TRUTH_STATE
        ));
        addBuiltinProperty(new DeferringProperty(
            this,
            null,
            BaseKind.ROOM,
            new TokenString("sleepsafe"),
            BaseKind.TRUTH_STATE
        ));
        addBuiltinProperty(new DeferringProperty(
            this,
            null,
            BaseKind.THING,
            new TokenString("printed name"),
            BaseKind.TEXT
        ));
        addBuiltinProperty(new DeferringProperty(
            this,
            null,
            BaseKind.THING,
            new TokenString("description"),
            BaseKind.TEXT
        ));
    }
    public static final String PROPERTY_NAME_REPLACEMENT = "property_names";
    private final TokenPattern propertyNames = new Conjunction(new CombinedCollection<>(new MappedCollection<>(
        properties.values(),
        m -> new MappedCollection<>(m.keySet(), TokenPattern::quoteIgnoreCase)
    )));
    
    public static final String KIND_PROPERTY_NAME_REPLACEMENT = "kind_property_names";
    public static final String PROPERTY_NAME_REPLACEMENT_KIND_CAPTURE = "kind_name";
    private final TokenPattern kindPropertyNames = new CaptureReplacement(
        PROPERTY_NAME_REPLACEMENT_KIND_CAPTURE,
        l -> new Conjunction(
            new MappedCollection<>(
                new CombinedCollection<>(
                    properties.getOrDefault(l, Collections.emptyMap()).keySet(),
                    Optional.ofNullable(enums.get(IEnum.Category.PROPERTY).get(l))
                        .map(IEnum::streamValues)
                        .map(Stream::toList)
                        .orElseGet(Collections::emptyList)
                ),
                TokenPattern::quoteIgnoreCase
            )
        ),
        false
    );
    
    private final Map<TokenString, DeferringObject> objects = new HashMap<>();
    {
        addObject(new DeferringObject(this, null, new TokenString("release number"), BaseKind.NUMBER));
        addObject(new DeferringObject(this, null, new TokenString("story creation year"), BaseKind.NUMBER));
        addObject(new DeferringObject(this, null, new TokenString("maximum score"), BaseKind.NUMBER));
        addObject(new DeferringObject(this, null, new TokenString("flexiblestory"), BaseKind.FIGURE));
        addObject(new DeferringObject(this, null, new TokenString("title_graphic"), BaseKind.FIGURE));
    }
    public static final String OBJECT_NAME_REPLACEMENT = "object_names";
    private static final TokenString it_tokens = new TokenString(new Token(Token.Type.WORD, "it"));
    private final TokenPattern objectNames = new Conjunction(new MappedCollection<>(
        objects.keySet(),
        TokenPattern::quoteIgnoreCase
    ));
    
    public static final String OBJECT_PROPERTY_NAME_REPLACEMENT = "object_property_names";
    public static final String PROPERTY_NAME_REPLACEMENT_OBJECT_CAPTURE = "object_name";
    private final TokenPattern objectPropertyNames = new CaptureReplacement(
        PROPERTY_NAME_REPLACEMENT_OBJECT_CAPTURE,
        l -> {
            DeferringObject obj = objects.get(l);
            Stream<DeferringKind> kinds = Stream.iterate(
                    Optional.ofNullable(obj).map(DeferringObject::getType),
                    Optional::isPresent,
                    k -> k.flatMap(DeferringKind::superKind)
                )
                .map(Optional::orElseThrow);
            List<TokenString> names = kinds.map(DeferringKind::name).map(properties::get).filter(Objects::nonNull).map(
                Map::keySet).flatMap(Set::stream).toList();
            return new Conjunction(new MappedList<>(names, TokenPattern::quoteIgnoreCase));
        },
        false
    );
    
    private final Map<String, DeferringFunction> functions = new HashMap<>();
    private final Map<TokenString, Map<TokenString, DeferringValue>> values = new HashMap<>();
    private final Map<TokenString, DeferringValue> freeValues = new HashMap<>();
    private final Map<TokenString, DeferringDefault> defaults = new HashMap<>();
    private final Map<TokenString, Map<TokenString, DeferringDefault>> pdefaults = new HashMap<>();
    private final Map<TokenString, DeferringAction> actions = new HashMap<>();
    public static final String ACTION_NAME_REPLACEMENT = "action_names";
    private final TokenPattern actionNames = new Conjunction(new MappedCollection<>(
        actions.keySet(),
        TokenPattern::quoteIgnoreCase
    ));
    private final Map<TokenString, DeferringTable> tableNames = new HashMap<>();
    private final Map<TokenString, DeferringTable> tableNumbers = new HashMap<>();
    private final Map<TokenString, List<DeferringContinuation>> continuedNames = new HashMap<>();
    private final Map<TokenString, List<DeferringContinuation>> continuedNumbers = new HashMap<>();
    private final List<DeferringAlias> aliases = new LinkedList<>();
    private final Map<TokenString, List<DeferringActionRule>> actionRules = new HashMap<>();
    private final Map<TokenString, DeferringRule> namedRules = new HashMap<>();
    private final Map<ISimpleRule.SimpleTrigger, List<DeferringSimpleRule>> simpleRules = new EnumMap<>(ISimpleRule.SimpleTrigger.class);
    
    public TokenPattern replace(TokenPattern pat) {
        return pat.replace(s -> switch(s) {
            case KIND_NAME_REPLACEMENT -> kindNames;
            case ACTION_NAME_REPLACEMENT -> actionNames;
            case OBJECT_NAME_REPLACEMENT -> objectNames;
            case PROPERTY_NAME_REPLACEMENT -> propertyNames;
            case KIND_PROPERTY_NAME_REPLACEMENT -> kindPropertyNames;
            case OBJECT_PROPERTY_NAME_REPLACEMENT -> objectPropertyNames;
            default -> throw new NoSuchElementException("Unknown replacement: " + s);
        });
    }
    
    @Override
    public DeferringKind getKind(TokenString name) {
        return kinds.get(name);
    }
    
    @Override
    public DeferringKind getBaseKind(BaseKind bk) {
        return basekinds.get(bk);
    }
    
    @Override
    public Stream<? extends DeferringKind> streamKinds() {
        return kinds.values().stream();
    }
    
    @Override
    public Predicate<IKind> kindEquals(IKind k) {
        return k2 -> k2.name().equals(k.name());
    }
    
    public boolean addKind(DeferringKind kind) {
        Statistics.KINDS.prepareLog(log).log("Adding kind {}", kind.name);
        return kinds.putIfAbsent(kind.name, kind) != null;
    }
    
    @Override
    public DeferringEnum getEnum(IEnum.Category category, TokenString name) {
        return enums.get(category).get(name);
    }
    
    @Override
    public Stream<? extends DeferringEnum> streamEnums(IEnum.Category category) {
        return enums.get(category).values().stream();
    }
    
    @Override
    public Stream<? extends DeferringEnum> streamEnums() {
        return enums.values().stream().map(Map::values).flatMap(Collection::stream);
    }
    
    public boolean addEnum(DeferringEnum e) {
        Map<TokenString, DeferringEnum> en = enums.get(e.category());
        switch(e.category()) {
        case KIND:
            Statistics.KINDS.prepareLog(log).log(
                "Adding enum kind {} [ {} ]",
                e.name(),
                e.streamValues().map(TokenString::toString).collect(Collectors.joining(", "))
            );
            break;
        case OBJECT:
            Statistics.OBJECTS.prepareLog(log).log(
                "Adding enum variable {} [ {} ]",
                e.name(),
                e.streamValues().map(TokenString::toString).collect(Collectors.joining(", "))
            );
            break;
        case PROPERTY:
            Statistics.PROPERTIES.prepareLog(log).log(
                "Adding enum property {} [ {} ]",
                e.name(),
                e.streamValues().map(TokenString::toString).collect(Collectors.joining(", "))
            );
            break;
        }
        return en.putIfAbsent(e.name(), e) != null;
    }
    
    @Override
    public DeferringProperty getProperty(IKind owner, TokenString name) {
        Optional<? extends IKind> own = Optional.of(owner);
        DeferringProperty ret;
        do {
            ret = properties.getOrDefault(owner.name(), Collections.emptyMap()).get(name);
            own = own.flatMap(IKind::superKind);
        } while(ret == null && own.isPresent());
        return ret;
    }
    
    @Override
    public Stream<? extends DeferringProperty> streamProperties() {
        return properties.values().stream().map(Map::values).flatMap(Collection::stream);
    }
    
    public boolean addProperty(DeferringProperty property) {
        Statistics.PROPERTIES.prepareLog(log).log(
            "Adding property {} of {} of type {}",
            property.name,
            property.owner.key(),
            property.type.key()
        );
        return properties.computeIfAbsent(property.owner.key(), s -> new HashMap<>()).putIfAbsent(
            property.name,
            property
        ) != null;
    }
    
    private boolean addBuiltinProperty(DeferringProperty property, TokenString... aliases) {
        Map<TokenString, DeferringProperty> props = properties.computeIfAbsent(property.owner.key(), s -> new HashMap<>());
        for(TokenString a : aliases) {
            props.putIfAbsent(a, property);
        }
        return props.putIfAbsent(property.name, property) != null;
    }
    
    @Override
    public DeferringObject getObject(TokenString name) {
        return objects.get(name);
    }
    
    @Override
    public Stream<? extends DeferringObject> streamObjects() {
        return objects.values().stream();
    }
    
    public boolean addObject(DeferringObject object) {
        Statistics.OBJECTS.prepareLog(log).log("Adding object {} of type {}", object.name, object.type.key());
        boolean ret = objects.putIfAbsent(object.name, object) != null;
        if(!ret) objects.put(it_tokens, object);
        return ret;
    }
    
    @Override
    public DeferringFunction getFunction(IKind returnType, Stream<? extends IFunction.SignatureElement> name) {
        DeferringFunction ret = getFunction(name);
        if(ret != null && !ret.returnType().canAssignTo(returnType)) ret = null;
        return ret;
    }
    
    @Override
    public DeferringFunction getFunction(Stream<? extends IFunction.SignatureElement> name) {
        return functions.get(IFunction.computeSignature(name));
    }
    
    @Override
    public Stream<? extends DeferringFunction> streamFunctions() {
        return functions.values().stream();
    }
    
    public boolean addFunction(DeferringFunction func) {
        Statistics.FUNCTIONS.prepareLog(log).log("Adding function {}", func.getSignature());
        return functions.put(func.getSignature(), func) == null;
    }
    
    public boolean addPredicate(DeferringPredicate predicate) {
        return addFunction(predicate);
    }
    
    public boolean addRoutine(DeferringRoutine routine) {
        return addFunction(routine);
    }
    
    public boolean addPrint(DeferringPrint print) {
        return addRoutine(print);
    }
    
    @Override
    public DeferringValue getValue(IObject owner, IProperty name) {
        return values.getOrDefault(owner.getName(), Collections.emptyMap()).get(name.getPropertyName());
    }
    
    @Override
    public DeferringValue getValue(IObject var) {
        return freeValues.get(var.getName());
    }
    
    @Override
    public Stream<? extends DeferringValue> streamValues() {
        return Stream.concat(
            freeValues.values().stream(),
            values.values().stream().map(Map::values).flatMap(Collection::stream)
        );
    }
    
    public boolean addValue(DeferringValue value) {
        Optional<TokenString> name = value.name.map(LazyLookup::key);
        Statistics.VALUES.prepareLog(log).log(
            "Adding value {} of {} as {}",
            name.orElse(TokenString.EMPTY),
            value.owner.key(),
            value.value
        );
        return name.map(tokens -> values.computeIfAbsent(value.owner.get().name, s -> new HashMap<>())
                .putIfAbsent(tokens, value) != null
            )
            .orElseGet(() -> null != freeValues.putIfAbsent(
                value.owner.key(),
                value
            ));
    }
    
    @Override
    public DeferringDefault getDefault(IObject label) {
        return defaults.get(label.getName());
    }
    
    @Override
    public DeferringDefault getDefault(IProperty prop) {
        return pdefaults.getOrDefault(prop.getPropertyOwner().name(), Collections.emptyMap())
            .get(prop.getPropertyName());
    }
    
    @Override
    public Stream<? extends DeferringDefault> streamDefaults() {
        return Stream.concat(
            defaults.values().stream(),
            pdefaults.values().stream().map(Map::values).flatMap(Collection::stream)
        );
    }
    
    public boolean addDefault(DeferringDefault def) {
        if(def.PROPERTY.isPresent()) {
            log.debug("Adding default value {} of {} as {}", def.PROPERTY.get(), def.LABEL, def.VALUE);
            return pdefaults.computeIfAbsent(def.LABEL, s -> new HashMap<>()).putIfAbsent(
                def.PROPERTY.get(),
                def
            ) != null;
        }
        log.debug("Adding default value {} as {}", def.LABEL, def.VALUE);
        return defaults.putIfAbsent(def.LABEL, def) != null;
    }
    
    @Override
    public DeferringAction getAction(TokenString name) {
        return actions.get(name);
    }
    
    @Override
    public Stream<? extends DeferringAction> streamActions() {
        return actions.values().stream();
    }
    
    public boolean addAction(DeferringAction act) {
        Statistics.ACTIONS.prepareLog(log).log("Adding action {}", act.name);
        return actions.putIfAbsent(act.name, act) != null;
    }
    
    @Override
    public DeferringTable getTable(TokenString name, boolean number) {
        return (number ? tableNumbers : tableNames).get(name);
    }
    
    @Override
    public Stream<? extends DeferringTable> streamTables() {
        return Stream.concat(tableNumbers.values().stream(), tableNames.values().stream()).distinct();
    }
    
    public boolean addTable(DeferringTable t) {
        if(t.name.isEmpty() && t.number.isEmpty()) throw new IllegalArgumentException("Nameless and numberless Table");
        Statistics.TABLES.prepareLog(log).log(
            "Adding table {} - {}",
            t.number.map(TokenString::toString).orElse("N/A"),
            t.name.map(TokenString::toString).orElse("N/A")
        );
        boolean error = false;
        if(t.name.isPresent()) error = tableNames.putIfAbsent(t.name.get(), t) != null;
        if(t.number.isPresent()) error = error || tableNumbers.putIfAbsent(t.number.get(), t) != null;
        return error;
    }
    
    public Stream<? extends DeferringContinuation> getContinuedTable(ITable<?> table) {
        Optional<TokenString> name = table.name();
        Optional<TokenString> number = table.number();
        return Stream.concat(
            name.isEmpty() ? Stream.empty() : continuedNames.getOrDefault(name.get(), Collections.emptyList()).stream(),
            number.isEmpty()
            ? Stream.empty()
            : continuedNumbers.getOrDefault(number.get(), Collections.emptyList()).stream()
        );
    }
    
    public Stream<? extends DeferringContinuation> streamOrphanTables() {
        return Stream.concat(
            continuedNames.entrySet().stream().filter(e -> !tableNames.containsKey(e.getKey())).map(Entry::getValue),
            continuedNumbers.entrySet().stream().filter(e -> !tableNumbers.containsKey(e.getKey())).map(Entry::getValue)
        ).flatMap(List::stream);
    }
    
    public boolean addContinuation(DeferringContinuation con) {
        Statistics.CONTINUED_TABLES.prepareLog(log).log("Continuing table {}", con.tableName);
        return (con.number ? continuedNumbers : continuedNames).computeIfAbsent(con.tableName, s -> new LinkedList<>())
            .add(con);
    }
    
    @Override
    public <T extends Element> Stream<? extends IAlias<T>> getAliases(Class<T> clazz, Predicate<? super T> selector) {
        return aliases.stream().<Optional<? extends IAlias<T>>>map(a -> a.cast(clazz)).filter(Optional::isPresent).map(
            Optional::get).filter(a -> selector.test(a.original()));
    }
    
    @Override
    public Stream<? extends DeferringAlias> streamAliases() {
        return aliases.stream();
    }
    
    public boolean addAlias(DeferringAlias a) {
        for(TokenString alias : a.aliases) {
            Statistics.ALIASES.prepareLog(log).log("Adding alias {} for {}", alias, a.original);
        }
        return aliases.add(a);
    }
    
    @Override
    public Stream<? extends DeferringActionRule> getActionRules(IAction action) {
        return actionRules.getOrDefault(action.name(), Collections.emptyList()).stream();
    }
    
    @Override
    public DeferringRule getNamedRule(TokenString name) {
        return namedRules.get(name);
    }
    
    @Override
    public Stream<? extends DeferringSimpleRule> getSimpleRules(ISimpleRule.SimpleTrigger trigger) {
        return simpleRules.getOrDefault(trigger, Collections.emptyList()).stream();
    }
    
    @Override
    public Stream<? extends DeferringRule> streamRules() {
        return Stream.concat(
            Stream.concat(
                actionRules.values().stream(),
                simpleRules.values().stream()
            ).flatMap(List::stream),
            namedRules.values().stream()
        ).distinct();
    }
    
    public boolean addRule(DeferringRule def) {
        Statistics.RULES.prepareLog(log).log(
            "Adding rule {}",
            def.NAME.map(TokenString::toString).orElse("without a name")
        );
        boolean change = false;
        boolean pchange = false;
        if(def instanceof DeferringActionRule act) {
            change = actionRules.computeIfAbsent(act.action, s -> new LinkedList<>()).add(act);
            pchange = true;
        }
        if(def instanceof DeferringSimpleRule smp) {
            change = simpleRules.computeIfAbsent(smp.trigger, s -> new LinkedList<>()).add(smp);
            pchange = true;
        }
        Optional<TokenString> n = def.name();
        if(n.isPresent()) {
            change |= namedRules.put(n.get(), def) != null;
            pchange = true;
        }
        if(!pchange) throw new IllegalArgumentException("Unknown rule class: " + def.getClass());
        return change;
    }
}
