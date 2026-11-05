/*
 * @test
 * @summary jdk.jshell.spi.ExecutionControl.RunException conformance: run exception abstract.
 * @run main RunExceptionTest
 */

import java.lang.reflect.*;

public class RunExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- deep contract: closed subtype family ----
    static void testDeepSemantics() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.spi.ExecutionControl$RunException");
        // the constructor is private: user code cannot add RunException subtypes
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors())
            check(java.lang.reflect.Modifier.isPrivate(ct.getModifiers()), "RunException constructors are private");
        // the three shipped subtypes partition the run-failure channels
        Class<?> user = Class.forName("jdk.jshell.spi.ExecutionControl$UserException");
        Class<?> res = Class.forName("jdk.jshell.spi.ExecutionControl$ResolutionException");
        Class<?> stop = Class.forName("jdk.jshell.spi.ExecutionControl$StoppedException");
        check(user.getSuperclass().equals(c), "UserException extends RunException");
        check(res.getSuperclass().equals(c), "ResolutionException extends RunException");
        check(stop.getSuperclass().equals(c), "StoppedException extends RunException");
        // every subtype is constructible and reports a message
        check(new jdk.jshell.spi.ExecutionControl.UserException("m", "c", new StackTraceElement[0]).getMessage().equals("m"), "UserException message");
        check(new jdk.jshell.spi.ExecutionControl.ResolutionException(1, new StackTraceElement[0]).id() == 1, "ResolutionException id");
        check(new jdk.jshell.spi.ExecutionControl.StoppedException() instanceof jdk.jshell.spi.ExecutionControl.RunException, "StoppedException family");
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.spi.ExecutionControl$RunException");
        check(!c.isInterface() && java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass().getName().equals("jdk.jshell.spi.ExecutionControl$ExecutionControlException"), "extends ExecutionControlException");
        testDeepSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
