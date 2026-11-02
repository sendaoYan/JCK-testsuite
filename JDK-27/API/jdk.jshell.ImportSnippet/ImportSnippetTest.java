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
    public static void main(String[] args) throws Exception {
        try (JShell js = JShell.create()) {
            ImportSnippet im = (ImportSnippet) js.eval("import java.util.List;").get(0).snippet();
            check(im.kind() == Snippet.Kind.IMPORT, "kind");
            check(im.name().equals("List"), "name");
            check(im.fullname().equals("java.util.List"), "fullname");
            check(!im.isStatic(), "not static");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
