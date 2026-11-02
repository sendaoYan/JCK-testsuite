/*
 * @test
 * @summary jdk.jshell.JShell.Builder conformance: builder build.
 * @run main BuilderTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class BuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        try (JShell js = JShell.builder().out(System.out).build()) {
            check(js != null, "built");
            check(js.eval("int x = 1;").get(0).status() == Snippet.Status.VALID, "eval");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
