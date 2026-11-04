/*
 * @test
 * @summary JLS 12.4.1 / 12.4.2: when a class is initialized, its superinterfaces
 *          that declare at least one default method are initialized first.
 * @run main DefaultMethodSuperinterfaceInitialized
 */

public class DefaultMethodSuperinterfaceInitialized {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static StringBuilder log = new StringBuilder();

    interface I {
        default void m() {}
        int X = init();
        static int init() { log.append("I"); return 1; }
    }

    static class C implements I {}

    public static void main(String[] args) {
        new C();
        check(log.toString().equals("I"), "superinterface with default method initialized before class: " + log);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
