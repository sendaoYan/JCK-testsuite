/*
 * @test
 * @summary jdk.jshell.PersistentSnippet conformance: persistent snippet via var.
 * @run main/othervm PersistentSnippetTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class PersistentSnippetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.PersistentSnippet");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("name")) f=true; check(f, "has name"); }
    }

    static void testBlackBox() throws Exception {

        try (JShell js = JShell.create()) {
            VarSnippet v = (VarSnippet) js.eval("int x = 1;").get(0).snippet();
            check(v instanceof PersistentSnippet, "is PersistentSnippet");
            check(v.name().equals("x"), "name");
        }
    }

    // ---- deep contract: which snippet classes are Persistent, and name() semantics ----
    static void testPersistenceMatrix() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            VarSnippet v = (VarSnippet) js.eval("int pv1 = 1;").get(0).snippet();
            check(v instanceof PersistentSnippet, "var is persistent");
            MethodSnippet m = (MethodSnippet) js.eval("int pm1() { return 1; }").get(0).snippet();
            check(m instanceof PersistentSnippet, "method is persistent");
            TypeDeclSnippet t = (TypeDeclSnippet) js.eval("class PT1 {}").get(0).snippet();
            check(t instanceof PersistentSnippet, "type is persistent");
            ImportSnippet i = (ImportSnippet) js.eval("import java.util.List;").get(0).snippet();
            check(i instanceof PersistentSnippet, "import is persistent");
            check(!(js.eval("pv1").get(0).snippet() instanceof PersistentSnippet), "expression not persistent");
            // a pure statement (no value) is not persistent; note pv1++ WOULD be (it yields a temp var)
            check(!(js.eval("if (pv1 > 0) ;").get(0).snippet() instanceof PersistentSnippet), "statement not persistent");
            check(!(js.eval("int pb = ;").get(0).snippet() instanceof PersistentSnippet), "erroneous not persistent");
            // every persistent snippet has a name, stable across status changes
            check(v.name().equals("pv1") && m.name().equals("pm1") && t.name().equals("PT1") && i.name().equals("List"),
                  "names of the four declaration families");
            js.drop(v);
            check(v.name().equals("pv1"), "name survives drop");
            // persistent kinds exactly match Kind.isPersistent()
            check(Snippet.Kind.VAR.isPersistent() && Snippet.Kind.METHOD.isPersistent()
                  && Snippet.Kind.TYPE_DECL.isPersistent() && Snippet.Kind.IMPORT.isPersistent(), "persistent kinds table");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testPersistenceMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
