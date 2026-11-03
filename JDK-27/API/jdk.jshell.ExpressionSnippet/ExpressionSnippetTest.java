/*
 * @test
 * @summary jdk.jshell.ExpressionSnippet conformance: expression snippet properties.
 * @run main ExpressionSnippetTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class ExpressionSnippetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.ExpressionSnippet");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("name")) f=true; check(f, "has name"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("typeName")) f=true; check(f, "has typeName"); }
    }

    static void testBlackBox() throws Exception {

        try (JShell js = JShell.create()) {
            js.eval("int x = 1;");
            SnippetEvent e = js.eval("x").get(0);
            ExpressionSnippet s = (ExpressionSnippet) e.snippet();
            check(s.kind() == Snippet.Kind.EXPRESSION, "kind");
            check(s.name().equals("x"), "name");
            check(s.typeName().equals("int"), "typeName");
            check("1".equals(e.value()), "value");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
