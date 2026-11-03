/*
 * @test
 * @summary jdk.jshell.MethodSnippet conformance: method snippet properties.
 * @run main MethodSnippetTest
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
