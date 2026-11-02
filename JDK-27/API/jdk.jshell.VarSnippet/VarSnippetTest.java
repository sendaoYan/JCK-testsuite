/*
 * @test
 * @summary jdk.jshell.VarSnippet conformance: var snippet properties.
 * @run main VarSnippetTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class VarSnippetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        try (JShell js = JShell.create()) {
            VarSnippet v = (VarSnippet) js.eval("int x = 1;").get(0).snippet();
            check(v.kind() == Snippet.Kind.VAR, "kind");
            check(v.name().equals("x"), "name");
            check(v.typeName().equals("int"), "typeName");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
