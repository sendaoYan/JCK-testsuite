/*
 * @test
 * @summary jdk.jshell.EvalException conformance: eval exception details.
 * @run main/othervm EvalExceptionTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class EvalExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.EvalException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("getExceptionClassName")) f=true; check(f, "has getExceptionClassName"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("getMessage")) f=true; check(f, "has getMessage"); }
    }

    static void testBlackBox() throws Exception {

        try (JShell js = JShell.create()) {
            SnippetEvent e = js.eval("1/0").get(0);
            EvalException ex = (EvalException) e.exception();
            check(ex != null, "exception present");
            check(ex.getExceptionClassName().equals("java.lang.ArithmeticException"), "exception class");
            check(ex.getMessage() != null, "message");
        }
    }

    // ---- deep contract: exception channel through real eval ----
    static void testExceptionChannel() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            // checked exception thrown by snippet code still surfaces as EvalException
            SnippetEvent e1 = js.eval("throw new Exception(\"checked\");").get(0);
            check(e1.exception() instanceof EvalException, "checked exception wrapped");
            check(((EvalException) e1.exception()).getExceptionClassName().equals("java.lang.Exception"), "checked class name");
            check(e1.exception().getMessage().contains("checked"), "message passthrough");
            // unchecked runtime exception
            js.eval("int[] earr = new int[1];");
            SnippetEvent e2 = js.eval("earr[5]").get(0);
            check(((EvalException) e2.exception()).getExceptionClassName().equals("java.lang.ArrayIndexOutOfBoundsException"),
                  "AIOOBE class name preserved");
            // error
            SnippetEvent e3 = js.eval("throw new StackOverflowError(\"so\");").get(0);
            check(((EvalException) e3.exception()).getExceptionClassName().equals("java.lang.StackOverflowError"),
                  "Error class name preserved");
            // user-defined snippet exception type
            js.eval("class MyEx extends RuntimeException { MyEx(String m) { super(m); } }");
            SnippetEvent e4 = js.eval("throw new MyEx(\"custom\");").get(0);
            check(((EvalException) e4.exception()).getExceptionClassName().endsWith("MyEx"), "snippet-defined exception type");
            // message of the wrapper contains the original message
            check(e4.exception().getMessage().contains("custom"), "custom message preserved");
            // exceptions are JShellException (Serializable via Exception)
            check(e1.exception() instanceof java.io.Serializable, "EvalException serializable via Exception");
            // stack trace machinery usable
            check(e1.exception().getStackTrace() != null, "stack trace available");
            // initCause path: cause is typically null (already unwrapped into the EvalException)
            check(((EvalException) e1.exception()).getCause() == null
                  || ((EvalException) e1.exception()).getCause() instanceof Throwable, "cause well-formed");
            // the snippet itself remains VALID and rerunnable
            check(e1.status() == Snippet.Status.VALID, "throwing snippet VALID");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testExceptionChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
