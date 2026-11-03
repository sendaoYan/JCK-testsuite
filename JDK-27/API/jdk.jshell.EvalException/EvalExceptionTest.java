/*
 * @test
 * @summary jdk.jshell.EvalException conformance: eval exception details.
 * @run main EvalExceptionTest
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
