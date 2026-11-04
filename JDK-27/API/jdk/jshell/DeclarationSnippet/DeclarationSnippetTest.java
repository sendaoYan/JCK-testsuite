/*
 * @test
 * @summary jdk.jshell.DeclarationSnippet conformance: declaration snippet via var.
 * @run main DeclarationSnippetTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class DeclarationSnippetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        try (JShell js = JShell.create()) {
            VarSnippet v = (VarSnippet) js.eval("int x = 1;").get(0).snippet();
            check(v instanceof DeclarationSnippet, "is DeclarationSnippet");
            check(v.name().equals("x"), "name");
            check(v.typeName().equals("int"), "typeName");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
