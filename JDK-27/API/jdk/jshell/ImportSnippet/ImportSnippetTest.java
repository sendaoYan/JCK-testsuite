/*
 * @test
 * @summary jdk.jshell.ImportSnippet conformance: import snippet properties.
 * @run main/othervm ImportSnippetTest
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

    // ---- deep contract ----
    static void testImportSemantics() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            // the four import forms and their name/fullname/isStatic table
            ImportSnippet s1 = (ImportSnippet) js.eval("import java.util.List;").get(0).snippet();
            check(s1.subKind() == Snippet.SubKind.SINGLE_TYPE_IMPORT_SUBKIND, "single type subkind");
            check(s1.name().equals("List") && s1.fullname().equals("java.util.List") && !s1.isStatic(), "single type fields");
            ImportSnippet s2 = (ImportSnippet) js.eval("import java.util.*;").get(0).snippet();
            check(s2.subKind() == Snippet.SubKind.TYPE_IMPORT_ON_DEMAND_SUBKIND, "on-demand subkind");
            check(s2.name().equals("java.util.*") && s2.fullname().equals("java.util.*") && !s2.isStatic(), "on-demand fields");
            ImportSnippet s3 = (ImportSnippet) js.eval("import static java.lang.Math.max;").get(0).snippet();
            check(s3.subKind() == Snippet.SubKind.SINGLE_STATIC_IMPORT_SUBKIND, "single static subkind");
            check(s3.name().equals("max") && s3.fullname().equals("java.lang.Math.max") && s3.isStatic(), "single static fields");
            ImportSnippet s4 = (ImportSnippet) js.eval("import static java.lang.Math.*;").get(0).snippet();
            check(s4.subKind() == Snippet.SubKind.STATIC_IMPORT_ON_DEMAND_SUBKIND, "static on-demand subkind");
            check(s4.name().equals("java.lang.Math.*") && s4.fullname().equals("java.lang.Math.*") && s4.isStatic(), "static on-demand fields");
            check(js.imports().count() == 4, "imports() counts all four");
            check(js.imports().allMatch(i -> i instanceof ImportSnippet), "imports() yields ImportSnippets");
            check(js.imports().allMatch(i -> i.kind() == Snippet.Kind.IMPORT), "imports() kind");
            // import takes effect for later snippets (closed loop)
            check(js.eval("List<String> l = new ArrayList<>();").get(0).status() == Snippet.Status.VALID, "type import effect");
            check(js.eval("max(2, 3)").get(0).value().equals("3"), "static import effect");
            // duplicate import overwrites (new valid import, old OVERWRITTEN)
            ImportSnippet dup = (ImportSnippet) js.eval("import java.util.List;").get(0).snippet();
            check(js.status(dup) == Snippet.Status.VALID, "duplicate import VALID");
            check(js.imports().filter(i -> i.fullname().equals("java.util.List")).count() == 1, "one active List import");
            // bogus import is rejected
            Snippet bad = js.eval("import java.nosuchpkg.Nosuch;").get(0).snippet();
            check(js.status(bad) == Snippet.Status.REJECTED, "unresolvable import REJECTED");
            check(bad.kind() == Snippet.Kind.IMPORT || bad.kind() == Snippet.Kind.ERRONEOUS, "bogus import kind recorded");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testImportSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
