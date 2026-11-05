/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis.CompletionInfo conformance: completion info interface.
 * @run main/othervm CompletionInfoTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.lang.reflect.*;

public class CompletionInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract: interface shape + real CompletionInfo from the engine ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.SourceCodeAnalysis$CompletionInfo");
        check(c.getMethods().length == 3, "exactly 3 abstract methods");
        for (String n : new String[]{"completeness", "remaining", "source"}) {
            boolean f = false;
            for (java.lang.reflect.Method m : c.getMethods()) if (m.getName().equals(n)) f = true;
            check(f, "has " + n);
        }
    }
    static void testReal() throws Exception {
        try (jdk.jshell.JShell js = jdk.jshell.JShell.builder().executionEngine("local").build()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            SourceCodeAnalysis.CompletionInfo ci = sca.analyzeCompletion("int ci1 = 1; int ci2 =");
            check(ci.completeness() == SourceCodeAnalysis.Completeness.COMPLETE, "first unit COMPLETE");
            check(ci.source().equals("int ci1 = 1;"), "source exact");
            check(ci.remaining().equals(" int ci2 ="), "remaining exact");
            // last incomplete unit: source null, everything stays in remaining
            SourceCodeAnalysis.CompletionInfo inc = sca.analyzeCompletion("int ci3 =");
            check(inc.completeness() == SourceCodeAnalysis.Completeness.DEFINITELY_INCOMPLETE, "incomplete");
            check(inc.source() == null, "null source while incomplete");
            check(inc.remaining().startsWith("int ci3 ="), "input preserved in remaining");
            // COMPLETE_WITH_SEMI: source gains the terminating semicolon
            SourceCodeAnalysis.CompletionInfo semi = sca.analyzeCompletion("int ci4 = 4");
            check(semi.completeness() == SourceCodeAnalysis.Completeness.COMPLETE_WITH_SEMI, "COMPLETE_WITH_SEMI");
            check(semi.source().equals("int ci4 = 4;"), "source auto-appends semicolon");
            check(semi.remaining().isEmpty(), "no remaining");
        }
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.SourceCodeAnalysis$CompletionInfo");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        testShape();
        testReal();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
