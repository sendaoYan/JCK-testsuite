/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis.CompletionContext conformance: completion context enum.
 * @run main CompletionContextTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class CompletionContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract: enum table ----
    static void testTable() {
        CompletionContext[] vs = CompletionContext.values();
        check(vs.length == 4, "4 constants");
        check(vs[0] == CompletionContext.ANNOTATION_ATTRIBUTE, "order[0] ANNOTATION_ATTRIBUTE");
        check(vs[1] == CompletionContext.NO_PAREN, "order[1] NO_PAREN");
        check(vs[2] == CompletionContext.TYPES_AS_ANNOTATIONS, "order[2] TYPES_AS_ANNOTATIONS");
        check(vs[3] == CompletionContext.QUALIFIED, "order[3] QUALIFIED");
        for (CompletionContext v : vs) check(CompletionContext.valueOf(v.name()) == v, "valueOf roundtrip " + v);
        try { CompletionContext.valueOf("BOGUS"); check(false, "valueOf bogus IAE"); }
        catch (IllegalArgumentException expected) { check(true, "valueOf bogus IAE"); }
        check(CompletionContext.class.getSuperclass() == java.lang.Enum.class, "extends Enum");
    }

    public static void main(String[] args) throws Exception {
        check(CompletionContext.ANNOTATION_ATTRIBUTE != null, "ANNOTATION_ATTRIBUTE");
        check(CompletionContext.NO_PAREN != null, "NO_PAREN");
        check(CompletionContext.TYPES_AS_ANNOTATIONS != null, "TYPES_AS_ANNOTATIONS");
        check(CompletionContext.QUALIFIED != null, "QUALIFIED");
        check(CompletionContext.values().length == 4, "4 constants");
        testTable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
