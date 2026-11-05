/*
 * @test
 * @summary jdk.jshell.MethodSnippet conformance: method snippet properties.
 * @run main/othervm MethodSnippetTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class MethodSnippetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.MethodSnippet");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("name")) f=true; check(f, "has name"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("parameterTypes")) f=true; check(f, "has parameterTypes"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("signature")) f=true; check(f, "has signature"); }
    }

    static void testBlackBox() throws Exception {

        try (JShell js = JShell.create()) {
            MethodSnippet m = (MethodSnippet) js.eval("int m(int a) { return a * 2; }").get(0).snippet();
            check(m.kind() == Snippet.Kind.METHOD, "kind");
            check(m.name().equals("m"), "name");
            check(m.parameterTypes().equals("int"), "parameterTypes");
            check(m.signature() != null && !m.signature().isEmpty(), "signature");
        }
    }

    // ---- deep contract ----
    static void testSignatureSemantics() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            MethodSnippet m = (MethodSnippet) js.eval("int mm2(int a, String b) { return a; }").get(0).snippet();
            check(m.parameterTypes().equals("int,String"), "parameterTypes comma list");
            check(m.signature().equals("(int,String)int"), "signature (params)return");
            check(m.name().equals("mm2"), "name");
            check(m.kind() == Snippet.Kind.METHOD && m.subKind() == Snippet.SubKind.METHOD_SUBKIND, "kind/subkind");
            check(m.toString().contains("mm2"), "toString contains name");
            // no-arg method: empty parameter list renders as ()
            MethodSnippet noarg = (MethodSnippet) js.eval("void mna() {}").get(0).snippet();
            check(noarg.parameterTypes().isEmpty(), "no-arg parameterTypes empty");
            check(noarg.signature().equals("()void"), "no-arg signature ()void");
            // generics and arrays in the signature are rendered with erasure
            MethodSnippet gen = (MethodSnippet) js.eval("<T> T mga(T[] a) { return a[0]; }").get(0).snippet();
            check(gen.parameterTypes().equals("T[]"), "generic param type");
            check(gen.signature().startsWith("(T[])"), "generic signature");
            // varargs
            MethodSnippet va = (MethodSnippet) js.eval("int mva(int... xs) { return xs.length; }").get(0).snippet();
            check(va.parameterTypes().equals("int..."), "varargs parameterTypes");
            // cross-snippet call exercises the declaration for real
            check(js.eval("mm2(3, \"s\")").get(0).value().equals("3"), "cross-snippet call");
            check(js.eval("mva(1,2,3)").get(0).value().equals("3"), "varargs call");
            // unresolved method keeps its signature while RECOVERABLE
            MethodSnippet unres = (MethodSnippet) js.eval("int mun() { return mundef(); }").get(0).snippet();
            check(js.status(unres) == Snippet.Status.RECOVERABLE_DEFINED, "unresolved method RECOVERABLE_DEFINED");
            check(unres.signature().equals("()int"), "signature available while recoverable");
            check(js.unresolvedDependencies(unres).toList().contains("method mundef()"), "unresolvedDependencies");
            // methods() stream only lists method snippets
            check(js.methods().allMatch(s -> s instanceof MethodSnippet), "methods() yields MethodSnippets");
            check(js.methods().noneMatch(s -> s.name().equals("nope")), "methods() names");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSignatureSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
