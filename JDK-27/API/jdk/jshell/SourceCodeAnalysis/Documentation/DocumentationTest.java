/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis.Documentation conformance: documentation interface.
 * @run main DocumentationTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.lang.reflect.*;

public class DocumentationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract: real documentation entries ----
    static void testReal() throws Exception {
        try (jdk.jshell.JShell js = jdk.jshell.JShell.builder().executionEngine("local").build()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            java.util.List<SourceCodeAnalysis.Documentation> docs = sca.documentation("System.out.println(", 19, false);
            check(docs.size() == 10, "ten println overloads");
            boolean sawNoArg = false, sawString = false, sawObject = false;
            for (SourceCodeAnalysis.Documentation d : docs) {
                check(d.signature() != null && !d.signature().isEmpty(), "signature non-empty");
                String sig = d.signature();
                if (sig.endsWith("println()")) sawNoArg = true;
                if (sig.contains("println(String")) sawString = true;
                if (sig.contains("println(Object")) sawObject = true;
            }
            check(sawNoArg, "no-arg overload");
            check(sawString, "String overload");
            check(sawObject, "Object overload");
            check(docs.get(0).activeParameterIndex() == 0, "activeParameterIndex 0 for single param");
            // class documentation: no active parameter
            java.util.List<SourceCodeAnalysis.Documentation> cls = sca.documentation("java.util.ArrayList", 19, false);
            check(cls.size() == 1, "single doc for class");
            check(cls.get(0).signature().contains("ArrayList"), "class signature");
            check(cls.get(0).activeParameterIndex() == -1, "no active parameter for class doc");
        }
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.SourceCodeAnalysis$Documentation");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        testReal();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
