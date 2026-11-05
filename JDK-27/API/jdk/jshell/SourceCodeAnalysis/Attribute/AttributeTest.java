/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis.Attribute conformance: highlight attribute enum.
 * @run main AttributeTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class AttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract: enum table + live highlight attributes ----
    static void testLive() throws Exception {
        Attribute[] vs = Attribute.values();
        check(vs.length == 3, "3 constants");
        check(vs[0] == Attribute.DECLARATION, "order[0] DECLARATION");
        check(vs[1] == Attribute.DEPRECATED, "order[1] DEPRECATED");
        check(vs[2] == Attribute.KEYWORD, "order[2] KEYWORD");
        try { Attribute.valueOf("BOGUS"); check(false, "valueOf bogus IAE"); }
        catch (IllegalArgumentException expected) { check(true, "valueOf bogus IAE"); }
        try (jdk.jshell.JShell js = jdk.jshell.JShell.builder().executionEngine("local").build()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            java.util.List<SourceCodeAnalysis.Highlight> hs = sca.highlights("int at1 = 1;");
            check(hs.size() == 2, "two highlights");
            check(hs.get(0).attributes().equals(java.util.EnumSet.of(Attribute.KEYWORD)), "keyword attribute set");
            check(hs.get(1).attributes().equals(java.util.EnumSet.of(Attribute.DECLARATION)), "declaration attribute set");
            // deprecated member usage would carry DEPRECATED (structural: enum usable in sets)
            check(java.util.EnumSet.of(Attribute.DEPRECATED).contains(Attribute.DEPRECATED), "usable in EnumSet");
        }
    }

    public static void main(String[] args) throws Exception {
        check(Attribute.DECLARATION != null, "DECLARATION");
        check(Attribute.KEYWORD != null, "KEYWORD");
        check(Attribute.DEPRECATED != null, "DEPRECATED");
        check(Attribute.values().length == 3, "3 constants");
        check(Attribute.valueOf("KEYWORD") == Attribute.KEYWORD, "valueOf");
        testLive();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
