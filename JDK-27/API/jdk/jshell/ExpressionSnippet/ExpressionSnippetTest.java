/*
 * @test
 * @summary jdk.jshell.ExpressionSnippet conformance: expression snippet properties.
 * @run main/othervm ExpressionSnippetTest
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

    // ---- deep contract ----
    static void testExpressionSemantics() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            js.eval("int ex1 = 1;");
            SnippetEvent e = js.eval("ex1").get(0);
            ExpressionSnippet s = (ExpressionSnippet) e.snippet();
            check(s.subKind() == Snippet.SubKind.VAR_VALUE_SUBKIND, "bare name VAR_VALUE subkind");
            check(s.name().equals("ex1"), "name of var-value expression");
            check(s.typeName().equals("int"), "typeName");
            // assignment expression: name is the assigned variable, value is the new value
            SnippetEvent a = js.eval("ex1 = 7").get(0);
            check(a.snippet().subKind() == Snippet.SubKind.ASSIGNMENT_SUBKIND, "assignment subkind");
            check(((ExpressionSnippet) a.snippet()).name().equals("ex1"), "assignment name");
            check(((ExpressionSnippet) a.snippet()).typeName().equals("int"), "assignment typeName");
            check(a.value().equals("7"), "assignment value is new value");
            check(js.varValue((VarSnippet) js.variables().filter(v -> v.name().equals("ex1")).findFirst().get()).equals("7"),
                  "assignment side effect persisted");
            // compound assignment
            check(js.eval("ex1 += 3").get(0).value().equals("10"), "compound assignment value");
            // expression of reference type reports its toString-rendered value and type
            js.eval("String exS = \"abc\";");
            SnippetEvent se = js.eval("exS.toUpperCase()").get(0);
            check(se.value().equals("\"ABC\""), "method call value rendered with quotes");
            check(se.snippet().subKind() == Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND, "fresh expression becomes temp var");
            check(((VarSnippet) se.snippet()).typeName().equals("String"), "temp var typeName");
            // boolean/char/etc expressions
            check(js.eval("ex1 > 5").get(0).value().equals("true"), "boolean expression");
            check(js.eval("'a'").get(0).value().equals("'a'"), "char expression rendered with quotes");
            check(js.eval("\"s\"").get(0).value().equals("\"s\""), "string expression rendered with quotes");
            check(js.eval("1.5").get(0).value().equals("1.5"), "double expression");
            check(js.eval("1L").get(0).value().equals("1"), "long expression renders without suffix");
            check(js.eval("null").get(0).value().equals("null"), "null expression");
            // expression naming an unknown symbol is rejected
            check(js.eval("exUnknown").get(0).status() == Snippet.Status.REJECTED, "unknown name REJECTED");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testExpressionSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
