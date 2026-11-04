/*
 * @test
 * @summary JLS 12.4.1 (Java SE 8): a class is initialized when an instance of it
 *          is created.
 * @run main InstanceCreationTriggersInit
 */

public class InstanceCreationTriggersInit {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean initialized;

    static class T {
        static { initialized = true; }
        T() {}
    }

    public static void main(String[] args) {
        check(!initialized, "T not initialized before new T()");
        T t = new T();
        check(t != null, "instance created");
        check(initialized, "T initialized by instance creation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
