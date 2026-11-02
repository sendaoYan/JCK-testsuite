/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis.Suggestion conformance: suggestion interface.
 * @run main SuggestionTest
 */

import java.lang.reflect.*;

public class SuggestionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.SourceCodeAnalysis$Suggestion");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
