/*
 * @test
 * @summary jdk.jshell.UnresolvedReferenceException conformance: unresolved reference exception.
 * @run main/othervm UnresolvedReferenceExceptionTest
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

    // ---- deep contract ----
    static void testUnresolvedChannel() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            // unresolved variable in method body
            MethodSnippet m1 = (MethodSnippet) js.eval("int um1() { return uz; }").get(0).snippet();
            check(js.status(m1) == Snippet.Status.RECOVERABLE_DEFINED, "um1 RECOVERABLE_DEFINED");
            SnippetEvent e1 = js.eval("um1()").get(0);
            check(e1.exception() instanceof UnresolvedReferenceException, "um1() throws URE");
            UnresolvedReferenceException u1 = (UnresolvedReferenceException) e1.exception();
            check(u1.getSnippet() == m1, "URE snippet is the offending method");
            check(u1.getMessage() != null && u1.getMessage().contains("um1"), "URE message names method");
            check(e1.value() == null, "no value");
            // unresolved method dependency
            MethodSnippet m2 = (MethodSnippet) js.eval("int um2() { return umMissing(); }").get(0).snippet();
            SnippetEvent e2 = js.eval("um2()").get(0);
            UnresolvedReferenceException u2 = (UnresolvedReferenceException) e2.exception();
            check(u2.getSnippet() == m2, "URE snippet for missing method");
            // URE is a JShellException subtype
            check(u2 instanceof JShellException, "URE extends JShellException");
            // hierarchy contract
            check(UnresolvedReferenceException.class.getSuperclass() == JShellException.class, "URE superclass");
            // resolving the dependency makes the call succeed (no URE)
            js.eval("int umMissing() { return 4; }");
            check(js.status(m2) == Snippet.Status.VALID, "um2 VALID after resolve");
            check(js.eval("um2()").get(0).value().equals("4"), "um2() callable after resolve");
            check(js.eval("um2()").get(0).exception() == null, "no URE after resolve");
            // var initializer with unresolved ref: REJECTED snippet (not URE)
            SnippetEvent vbad = js.eval("int uvb = umMissing2();").get(0);
            check(vbad.status() == Snippet.Status.REJECTED, "unresolved initializer REJECTED");
            check(vbad.exception() == null, "no URE for rejected initializer");
            // URE not thrown for type-level RECOVERABLE_NOT_DEFINED usage
            js.eval("class UC extends UMissing {}");
            SnippetEvent useBad = js.eval("new UC()").get(0);
            check(useBad.exception() == null, "no URE for undefined supertype usage");
            check(useBad.status() == Snippet.Status.REJECTED, "instantiating incomplete type REJECTED");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testUnresolvedChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
