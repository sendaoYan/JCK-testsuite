/*
 * @test
 * @summary JLS 12.4.1: a class is initialized when a static method declared by it
 *          is invoked.
 * @run main StaticMethodInvocationTriggersInit
 */

public class StaticMethodInvocationTriggersInit {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean initialized;

    static class T {
        static { initialized = true; }
        static int m() { return 7; }
    }

    public static void main(String[] args) {
        check(!initialized, "T not initialized before method invocation");
        check(T.m() == 7, "static method invoked");
        check(initialized, "T initialized by static method invocation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
