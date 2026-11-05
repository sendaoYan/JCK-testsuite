/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis.Suggestion conformance: suggestion interface.
 * @run main/othervm SuggestionTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.lang.reflect.*;

public class SuggestionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract: interface shape + real suggestions ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.SourceCodeAnalysis$Suggestion");
        check(c.getMethods().length == 2, "exactly 2 abstract methods");
        for (String n : new String[]{"continuation", "matchesType"}) {
            boolean f = false;
            for (java.lang.reflect.Method m : c.getMethods()) if (m.getName().equals(n)) f = true;
            check(f, "has " + n);
        }
    }
    static void testReal() throws Exception {
        try (jdk.jshell.JShell js = jdk.jshell.JShell.builder().executionEngine("local").build()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            int[] anchor = new int[1];
            java.util.List<SourceCodeAnalysis.Suggestion> sugg = sca.completionSuggestions("Syst", 4, anchor);
            check(!sugg.isEmpty(), "suggestions non-empty");
            check(sugg.stream().anyMatch(s -> s.continuation().equals("System")), "System continuation");
            check(sugg.stream().allMatch(s -> s.continuation() != null), "continuation non-null");
            check(anchor[0] == 0, "anchor at identifier start");
            // anchor moves with qualified prefix
            sca.completionSuggestions("String.valueO", 12, anchor);
            check(anchor[0] == 7, "anchor after dot");
            // static member continuations carry an open paren for methods
            java.util.List<SourceCodeAnalysis.Suggestion> members = sca.completionSuggestions("String.value", 12, anchor);
            check(members.size() == 9, "nine valueOf overloads by prefix");
            check(members.stream().allMatch(s -> s.continuation().startsWith("valueOf(")), "method continuations with paren");
            // unfiltered members include the class keyword and static fields
            java.util.List<SourceCodeAnalysis.Suggestion> all = sca.completionSuggestions("String.", 7, anchor);
            check(all.stream().anyMatch(s -> s.continuation().equals("class")), "class keyword member");
            check(all.stream().anyMatch(s -> s.continuation().equals("CASE_INSENSITIVE_ORDER")), "static field member");
        }
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.SourceCodeAnalysis$Suggestion");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        testShape();
        testReal();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
