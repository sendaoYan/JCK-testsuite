/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis.CompletionState conformance: completion state interface.
 * @run main CompletionStateTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.lang.reflect.*;

public class CompletionStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract: interface shape + live instance from the engine ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.SourceCodeAnalysis$CompletionState");
        check(c.getMethods().length == 5, "exactly 5 abstract methods");
        for (String n : new String[]{"availableUsingSimpleName", "completionContext", "selectorType", "elementUtils", "typeUtils"}) {
            boolean f = false;
            for (java.lang.reflect.Method m : c.getMethods()) if (m.getName().equals(n)) f = true;
            check(f, "has " + n);
        }
    }
    static void testLive() throws Exception {
        try (jdk.jshell.JShell js = jdk.jshell.JShell.builder().executionEngine("local").build()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            final SourceCodeAnalysis.CompletionState[] seen = {null};
            sca.completionSuggestions("Syst", 4, (st, list) -> { seen[0] = st; return java.util.List.copyOf(list); });
            check(seen[0] != null, "convertor receives CompletionState");
            check(seen[0].elementUtils() != null, "elementUtils available");
            check(seen[0].typeUtils() != null, "typeUtils available");
            check(seen[0].completionContext() != null, "completionContext set non-null");
            // element machinery is usable only INSIDE the convertor callback (task closes after)
            final boolean[] resolved = {false};
            sca.completionSuggestions("Syst", 4, (st, l) -> {
                resolved[0] = st.elementUtils() != null
                    && st.elementUtils().getTypeElement("java.lang.String") != null
                    && st.elementUtils().getTypeElement("no.such.Type") == null;
                return java.util.List.of();
            });
            check(resolved[0], "elements resolvable within callback (String yes, bogus null)");
        }
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.SourceCodeAnalysis$CompletionState");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        testShape();
        testLive();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
