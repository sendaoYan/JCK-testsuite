/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis.Highlight conformance: highlight record.
 * @run main HighlightTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class HighlightTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract: record semantics + live highlights ----
    static void testRecord() {
        Highlight h = new Highlight(2, 5, EnumSet.of(Attribute.KEYWORD));
        check(h.start() == 2, "start accessor");
        check(h.end() == 5, "end accessor");
        check(h.attributes().equals(EnumSet.of(Attribute.KEYWORD)), "attributes accessor");
        // value equality (record)
        check(h.equals(new Highlight(2, 5, EnumSet.of(Attribute.KEYWORD))), "value equals");
        check(!h.equals(new Highlight(2, 6, EnumSet.of(Attribute.KEYWORD))), "different end not equal");
        check(!h.equals(new Highlight(3, 5, EnumSet.of(Attribute.KEYWORD))), "different start not equal");
        check(!h.equals(new Highlight(2, 5, EnumSet.of(Attribute.DECLARATION))), "different attrs not equal");
        check(h.hashCode() == new Highlight(2, 5, EnumSet.of(Attribute.KEYWORD)).hashCode(), "hashCode consistent");
        check(h.toString() != null && h.toString().contains("KEYWORD"), "toString mentions attribute");
        check(h.toString().contains("2"), "toString mentions start");
        // record component list
        java.lang.Class<?> c = Highlight.class;
        check(c.isRecord(), "is record");
        check(c.getRecordComponents().length == 3, "3 record components");
        // record keeps the supplied set reference (records do not defensive-copy components)
        EnumSet<Attribute> mut = EnumSet.of(Attribute.KEYWORD);
        Highlight hh = new Highlight(0, 1, mut);
        mut.add(Attribute.DECLARATION);
        check(hh.attributes().size() == 2, "record stores the live set reference");
    }
    static void testLive() throws Exception {
        try (jdk.jshell.JShell js = jdk.jshell.JShell.builder().executionEngine("local").build()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            java.util.List<Highlight> hs = sca.highlights("int hl1 = 1;");
            check(hs.size() == 2, "two highlights");
            Highlight kw = hs.get(0), decl = hs.get(1);
            check(kw.start() == 0 && kw.end() == 3, "int keyword span 0..3");
            check(decl.start() == 4 && decl.end() == 7, "name span 4..7");
            check(kw.attributes().contains(Attribute.KEYWORD), "KEYWORD on first");
            check(decl.attributes().contains(Attribute.DECLARATION), "DECLARATION on second");
            check(!kw.attributes().contains(Attribute.DEPRECATED), "no DEPRECATED");
            // no highlights for pure statement
            check(sca.highlights("System.exit(-1);").isEmpty(), "no highlights for plain statement");
        }
    }

    public static void main(String[] args) throws Exception {
        Highlight h = new Highlight(0, 3, EnumSet.noneOf(Attribute.class));
        check(h.start() == 0, "start");
        check(h.end() == 3, "end");
        check(h.attributes() != null && h.attributes().isEmpty(), "attributes");
        testRecord();
        testLive();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
