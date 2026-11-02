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
    public static void main(String[] args) throws Exception {
        try (JShell js = JShell.create()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            check(sca.analyzeCompletion("int x = 1;").completeness() == Completeness.COMPLETE, "analyzeCompletion");
            check(!sca.analyzeCompletion("int x = ").completeness().isComplete(), "incomplete");
            boolean hasSystem = false;
            for (Suggestion s : sca.completionSuggestions("Syst", 4, new int[1])) if (s.continuation().equals("System")) hasSystem = true;
            check(hasSystem, "suggests System");
            check(!sca.highlights("int x").isEmpty(), "highlights");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
