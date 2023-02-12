package net.inform7j.transpiler.language.impl.deferring;

import net.inform7j.transpiler.Source;
import net.inform7j.transpiler.language.ITable;
import net.inform7j.transpiler.tokenizer.TokenString;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DeferringTable extends DeferringImpl implements ITable<DeferringTable.DeferringColumn> {
    public record DeferringColumn(DeferringTable table, TokenString name, Optional<TokenString> kind)
        implements IColumn<DeferringColumn> {
        @Override
        public Optional<? extends DeferringKind> explicitKind() {
            return kind.map(table.story::getKind);
        }
    }
    
    public static class DeferringContinuation extends DeferringImpl {
        public record ContinuedColumn(TokenString name, Optional<TokenString> kind) {
        }
        
        public final TokenString tableName;
        public final boolean number;
        public final Map<TokenString, TokenString> kindSpec;
        private final List<Map<TokenString, TokenString>> rows;
        public DeferringContinuation(
            DeferringStory story, Source source, TokenString table, boolean number, Stream<ContinuedColumn> hdr,
            Stream<? extends Stream<TokenString>> rOWS
        ) {
            super(story, source);
            this.tableName = table;
            this.number = number;
            final List<ContinuedColumn> cols = hdr.toList();
            this.kindSpec = cols.stream().filter(c -> c.kind().isPresent()).collect(Collectors.toUnmodifiableMap(
                ContinuedColumn::name,
                c -> c.kind().get()
            ));
            this.rows = rOWS.map(Stream::toList).map(l -> {
                if(l.size() > cols.size()) {
                    throw new IllegalArgumentException("Row Mismatch: expected " + cols.size() + " got " + l.size() + ":\n" + cols + "\n" + l);
                }
                return IntStream.range(0, l.size())
                    .boxed()
                    .collect(Collectors.toUnmodifiableMap(
                        i -> cols.get(i).name(),
                        l::get
                    ));
            }).toList();
        }
        public DeferringTable table() {
            return story.getTable(tableName, number);
        }
        public Optional<? extends DeferringKind> kindSpec(DeferringColumn column) {
            TokenString spec = kindSpec.get(column.name);
            if(spec == null) return Optional.empty();
            return Optional.of(story.getKind(spec));
        }
        public Stream<Map<DeferringColumn, TokenString>> rows() {
            DeferringTable t = table();
            return rows.stream().map(m -> m.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                    e -> t.columns().filter(d -> d.name().equals(e.getKey())).findFirst().orElseThrow(),
                    Entry::getValue
                ))
            );
        }
    }
    
    public final Optional<TokenString> number;
    public final Optional<TokenString> name;
    public final List<DeferringColumn> columns;
    private final List<Map<DeferringColumn, TokenString>> rows;
    public DeferringTable(
        DeferringStory story,
        Source source,
        Optional<TokenString> number,
        Optional<TokenString> name,
        Stream<? extends Function<? super DeferringTable, DeferringColumn>> columns,
        Stream<? extends Stream<TokenString>> rows
    ) {
        super(story, source);
        this.number = number;
        this.name = name;
        this.columns = columns.map(f -> f.apply(this)).toList();
        this.rows = rows.map(Stream::toList).map(l -> {
            if(l.size() > this.columns.size()) {
                throw new IllegalArgumentException("Row Mismatch: expected " + this.columns.size() + " got " + l.size() + ":\n" + l.stream()
                    .map(TokenString::toString)
                    .collect(Collectors.joining("\n")));
            }
            return IntStream.range(0, l.size()).boxed()
                .collect(Collectors.toUnmodifiableMap(
                    this.columns::get,
                    l::get
                ));
        }).toList();
    }
    
    @Override
    public Optional<TokenString> number() {
        return number;
    }
    @Override
    public Optional<TokenString> name() {
        return name;
    }
    @Override
    public Stream<DeferringColumn> columns() {
        return columns.stream();
    }
    @Override
    public Stream<? extends Map<DeferringColumn, TokenString>> rows() {
        return Stream.concat(rows.stream(), story.getContinuedTable(this).flatMap(DeferringContinuation::rows));
    }
    
}
