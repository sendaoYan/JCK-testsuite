/*
 * @test
 * @summary jdk.jshell.PersistentSnippet conformance: persistent snippet via var.
 * @run main PersistentSnippetTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class PersistentSnippetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        try (JShell js = JShell.create()) {
            VarSnippet v = (VarSnippet) js.eval("int x = 1;").get(0).snippet();
            check(v instanceof PersistentSnippet, "is PersistentSnippet");
            check(v.name().equals("x"), "name");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
