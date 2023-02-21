import net.inform7j.transpiler.parser.CombinedParser;
import net.inform7j.transpiler.parser.provider.*;
import net.inform7j.transpiler.parser.provider.rule.*;

module inform7J {
    requires java.desktop;
    requires org.slf4j;
    requires static lombok;
    
    uses CombinedParser.Provider;
    
    provides CombinedParser.Provider with
        KindParserProvider,
        EnumParserProvider,
        PropertyParserProvider,
        ObjectParserProvider,
        PredicateParserProvider,
        FunctionParserProvider,
        PrintParserProvider,
        RoutineParserProvider,
        ActionParserProvider,
        TableParserProvider,
        AliasParserProvider,
        ConditionalActionRuleProvider,
        LegacyParserProvider;
}