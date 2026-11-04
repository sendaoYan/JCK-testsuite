/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis.CompletionState conformance: completion state interface.
 * @run main CompletionStateTest
 */

import java.lang.reflect.*;

public class CompletionStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.SourceCodeAnalysis$CompletionState");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
