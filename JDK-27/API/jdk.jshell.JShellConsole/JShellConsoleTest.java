/*
 * @test
 * @summary jdk.jshell.JShellConsole conformance: console interface reflection.
 * @run main JShellConsoleTest
 */

import java.lang.reflect.*;

public class JShellConsoleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.JShellConsole");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
