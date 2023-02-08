import net.inform7j.transpiler.parser.*;

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
        LegacyParserProvider;
}