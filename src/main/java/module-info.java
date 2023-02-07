import net.inform7j.transpiler.parser.CombinedParser;

module inform7J {
    requires java.desktop;
    requires org.slf4j;
    requires static lombok;
    
    uses CombinedParser.Provider;
    
    provides CombinedParser.Provider with
        net.inform7j.transpiler.parser.KindParserProvider,
        net.inform7j.transpiler.parser.LegacyParserProvider;
}