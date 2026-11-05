/*
 * @test
 * @summary jdk.jshell.VarSnippet conformance: var snippet properties.
 * @run main/othervm VarSnippetTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class VarSnippetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.VarSnippet");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("name")) f=true; check(f, "has name"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("typeName")) f=true; check(f, "has typeName"); }
    }

    static void testBlackBox() throws Exception {

        try (JShell js = JShell.create()) {
            VarSnippet v = (VarSnippet) js.eval("int x = 1;").get(0).snippet();
            check(v.kind() == Snippet.Kind.VAR, "kind");
            check(v.name().equals("x"), "name");
            check(v.typeName().equals("int"), "typeName");
        }
    }

    // ---- deep contract ----
    static void testVarSemantics() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            // declaration without initializer: default value readable
            VarSnippet nov = (VarSnippet) js.eval("int vno;").get(0).snippet();
            check(nov.subKind() == Snippet.SubKind.VAR_DECLARATION_SUBKIND, "no-init subkind");
            check(js.varValue(nov).equals("0"), "default value 0");
            // reference type default is null
            VarSnippet nos = (VarSnippet) js.eval("String vns;").get(0).snippet();
            check(js.varValue(nos).equals("null"), "default value null rendered");
            // typed forms and typeName renderings
            String[][] forms = {
                {"int vi = 1;", "int", "1"},
                {"double vd = 2.5;", "double", "2.5"},
                {"boolean vb = true;", "boolean", "true"},
                {"char vc = 'x';", "char", "'x'"},
                {"long vl = 10;", "long", "10"},
                {"String vs2 = \"s\";", "String", "\"s\""},
                {"int[] va = {1,2};", "int[]", "int[2] { 1, 2 }"},
                {"Object vo = null;", "Object", "null"},
            };
            for (String[] f : forms) {
                VarSnippet sn = (VarSnippet) js.eval(f[0]).get(0).snippet();
                check(sn.typeName().equals(f[1]), "typeName of " + f[0]);
                String val = js.varValue(sn);
                check(f[2].endsWith("...") ? val.startsWith(f[2].substring(0, f[2].length() - 3)) : val.equals(f[2]),
                      "value of " + f[0]);
            }
            // var re-declaration with same type overwrites without events for the old? (2 events)
            js.eval("int vover = 1;");
            List<SnippetEvent> evs = js.eval("int vover = 2;");
            check(evs.size() == 2, "var overwrite emits 2 events");
            check(js.eval("vover").get(0).value().equals("2"), "overwritten var value");
            // var type change replaces (also 2 events)
            evs = js.eval("String vover = \"t\";");
            check(evs.size() == 2, "var type change overwrites");
            check(js.eval("vover").get(0).value().equals("\"t\""), "type-changed var value");
            // final and generic vars
            VarSnippet fin = (VarSnippet) js.eval("final int vf = 9;").get(0).snippet();
            check(js.varValue(fin).equals("9"), "final var");
            VarSnippet box = (VarSnippet) js.eval("java.util.List<String> vgl = java.util.List.of(\"a\");").get(0).snippet();
            check(box.typeName().startsWith("java.util.List"), "generic typeName");
            // REPL relaxation: jshell permits assignment to a snippet-declared final var
            SnippetEvent finSet = js.eval("vf = 1;").get(0);
            check(finSet.status() == Snippet.Status.VALID, "final var assignment tolerated by jshell");
            check(js.varValue(fin).equals("1"), "final var value updated (recorded engine behavior)");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testVarSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
