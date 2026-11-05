/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis.ElementSuggestion conformance: element suggestion interface.
 * @run main ElementSuggestionTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.lang.reflect.*;

public class ElementSuggestionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract: interface shape + live elements ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.SourceCodeAnalysis$ElementSuggestion");
        for (String n : new String[]{"element", "keyword", "matchesType", "anchor", "documentation"}) {
            boolean f = false;
            for (java.lang.reflect.Method m : c.getMethods()) if (m.getName().equals(n)) f = true;
            check(f, "has " + n);
        }
    }
    static void testLive() throws Exception {
        try (jdk.jshell.JShell js = jdk.jshell.JShell.builder().executionEngine("local").build()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            java.util.List<SourceCodeAnalysis.ElementSuggestion> els =
                sca.completionSuggestions("Syst", 4, (st, list) -> java.util.List.copyOf(list));
            check(!els.isEmpty(), "element suggestions non-empty");
            SourceCodeAnalysis.ElementSuggestion es = els.get(0);
            check(es.element() != null, "element non-null");
            check(es.element().getSimpleName().contentEquals("System"), "System element name");
            check(es.element().getKind() == javax.lang.model.element.ElementKind.CLASS, "System element kind CLASS");
            check(es.keyword() == null, "member suggestions carry no keyword");
            check(es.matchesType(), "matchesType true for System");
            check(es.anchor() == 0, "anchor 0 for top-level");
            check(es.documentation() != null, "documentation supplier non-null");
            check(es.documentation().get() != null, "documentation text materializes");
        }
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.SourceCodeAnalysis$ElementSuggestion");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        testShape();
        testLive();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
