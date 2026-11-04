/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis conformance: analysis and completion.
 * @run main SourceCodeAnalysisTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class SourceCodeAnalysisTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.SourceCodeAnalysis");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("analyzeCompletion")) f=true; check(f, "has analyzeCompletion"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("completionSuggestions")) f=true; check(f, "has completionSuggestions"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("highlights")) f=true; check(f, "has highlights"); }
    }

    static void testBlackBox() throws Exception {

        try (JShell js = JShell.create()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            check(sca.analyzeCompletion("int x = 1;").completeness() == Completeness.COMPLETE, "analyzeCompletion");
            check(!sca.analyzeCompletion("int x = ").completeness().isComplete(), "incomplete");
            boolean hasSystem = false;
            for (Suggestion s : sca.completionSuggestions("Syst", 4, new int[1])) if (s.continuation().equals("System")) hasSystem = true;
            check(hasSystem, "suggests System");
            check(!sca.highlights("int x").isEmpty(), "highlights");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
