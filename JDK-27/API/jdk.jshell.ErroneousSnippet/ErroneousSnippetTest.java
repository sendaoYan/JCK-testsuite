/*
 * @test
 * @summary jdk.jshell.ErroneousSnippet conformance: erroneous snippet properties.
 * @run main ErroneousSnippetTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class ErroneousSnippetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.ErroneousSnippet");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("probableKind")) f=true; check(f, "has probableKind"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("source")) f=true; check(f, "has source"); }
    }

    static void testBlackBox() throws Exception {

        try (JShell js = JShell.create()) {
            ErroneousSnippet es = (ErroneousSnippet) js.eval("int = ;").get(0).snippet();
            check(es.kind() == Snippet.Kind.ERRONEOUS, "kind");
            check(es.probableKind() != null, "probableKind");
            check(es.source().contains("int"), "source");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
