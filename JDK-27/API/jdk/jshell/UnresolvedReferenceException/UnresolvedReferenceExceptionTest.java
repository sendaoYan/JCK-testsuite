/*
 * @test
 * @summary jdk.jshell.UnresolvedReferenceException conformance: unresolved reference exception.
 * @run main UnresolvedReferenceExceptionTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class UnresolvedReferenceExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.UnresolvedReferenceException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("getSnippet")) f=true; check(f, "has getSnippet"); }
    }

    static void testBlackBox() throws Exception {

        try (JShell js = JShell.create()) {
            MethodSnippet m = (MethodSnippet) js.eval("void mm() { int x = z; }").get(0).snippet();
            SnippetEvent e = js.eval("mm()").get(0);
            UnresolvedReferenceException ex = (UnresolvedReferenceException) e.exception();
            check(ex != null, "exception present");
            check(ex.getSnippet() == m, "snippet");
            check(ex.getMessage() != null, "message");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
