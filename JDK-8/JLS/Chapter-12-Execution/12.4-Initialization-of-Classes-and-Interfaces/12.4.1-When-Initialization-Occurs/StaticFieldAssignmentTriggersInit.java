/*
 * @test
 * @summary JLS 12.4.1 (Java SE 8): a class is initialized when a static field
 *          declared by it is assigned.
 * @run main StaticFieldAssignmentTriggersInit
 */

public class StaticFieldAssignmentTriggersInit {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean initialized;

    static class T {
        static int x;
        static { initialized = true; }
    }

    public static void main(String[] args) {
        T.x = 1;
        check(T.x == 1, "static field assigned");
        check(initialized, "T initialized on static field assignment");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
