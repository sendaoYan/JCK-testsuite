/*
 * @test
 * @summary jdk.jshell.SnippetEvent conformance: snippet event fields.
 * @run main SnippetEventTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class SnippetEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.SnippetEvent");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("snippet")) f=true; check(f, "has snippet"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("status")) f=true; check(f, "has status"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("previousStatus")) f=true; check(f, "has previousStatus"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("exception")) f=true; check(f, "has exception"); }
    }

    static void testBlackBox() throws Exception {

        try (JShell js = JShell.create()) {
            SnippetEvent e = js.eval("int x = 1;").get(0);
            check(e.snippet() != null, "snippet");
            check(e.status() == Snippet.Status.VALID, "status");
            check(e.previousStatus() != null, "previousStatus");
            check(e.snippet().id() != null, "snippet id");
            check(e.causeSnippet() == null, "no cause snippet");
            check(e.exception() == null, "no exception");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
