/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis.ElementSuggestionConvertor conformance: element suggestion convertor interface.
 * @run main ElementSuggestionConvertorTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.lang.reflect.*;

public class ElementSuggestionConvertorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract: convert() contract via the engine channel ----
    static void testConvert() throws Exception {
        try (jdk.jshell.JShell js = jdk.jshell.JShell.builder().executionEngine("local").build()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            // identity conversion returns the same element suggestions
            java.util.List<SourceCodeAnalysis.ElementSuggestion> ids =
                sca.completionSuggestions("Syst", 4, (st, list) -> java.util.List.copyOf(list));
            check(!ids.isEmpty(), "identity convert yields elements");
            // custom mapping: project to simple-name strings
            java.util.List<String> names =
                sca.completionSuggestions("Syst", 4, (st, list) ->
                    list.stream().map(e -> e.element().getSimpleName().toString()).toList());
            check(names.contains("System"), "mapped name System");
            check(!names.isEmpty(), "mapped list non-empty");
            // the convertor fully controls the output type: empty conversion is honored
            java.util.List<String> empty =
                sca.completionSuggestions("Syst", 4, (st, list) -> java.util.List.of());
            check(empty.isEmpty(), "convertor may return empty list");
            // convertor sees a non-null CompletionState and suggestion list
            final boolean[] ok = {false};
            sca.completionSuggestions("Syst", 4, (st, list) -> { ok[0] = st != null && list != null && !list.isEmpty(); return java.util.List.of(); });
            check(ok[0], "state and list non-null in convertor");
        }
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.SourceCodeAnalysis$ElementSuggestionConvertor");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        testConvert();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
