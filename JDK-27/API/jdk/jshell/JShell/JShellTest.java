/*
 * @test
 * @summary jdk.jshell.JShell conformance: create eval drop inspect.
 * @run main JShellTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class JShellTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.JShell");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("create")) f=true; check(f, "has create"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("eval")) f=true; check(f, "has eval"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("snippets")) f=true; check(f, "has snippets"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("sourceCodeAnalysis")) f=true; check(f, "has sourceCodeAnalysis"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("drop")) f=true; check(f, "has drop"); }
    }

    static void testBlackBox() throws Exception {

        try (JShell js = JShell.create()) {
            check(js != null, "created");
            check(js.eval("int x = 1;").get(0).status() == Snippet.Status.VALID, "eval");
            check(js.snippets().count() >= 1, "snippets");
            check(js.sourceCodeAnalysis() != null, "sourceCodeAnalysis");
            VarSnippet v = (VarSnippet) js.eval("int y = 2;").get(0).snippet();
            check(js.drop(v).get(0).status() == Snippet.Status.DROPPED, "drop");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
