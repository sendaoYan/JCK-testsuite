/*
 * @test
 * @summary jdk.jshell.ImportSnippet conformance: import snippet properties.
 * @run main ImportSnippetTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class ImportSnippetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.ImportSnippet");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("name")) f=true; check(f, "has name"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("fullname")) f=true; check(f, "has fullname"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("isStatic")) f=true; check(f, "has isStatic"); }
    }

    static void testBlackBox() throws Exception {

        try (JShell js = JShell.create()) {
            ImportSnippet im = (ImportSnippet) js.eval("import java.util.List;").get(0).snippet();
            check(im.kind() == Snippet.Kind.IMPORT, "kind");
            check(im.name().equals("List"), "name");
            check(im.fullname().equals("java.util.List"), "fullname");
            check(!im.isStatic(), "not static");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
