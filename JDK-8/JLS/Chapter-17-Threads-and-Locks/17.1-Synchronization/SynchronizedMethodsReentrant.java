/*
 * @test
 * @summary JLS 17.1 (Java SE 8): monitors are reentrant; a synchronized instance
 *          method locks this and a synchronized static method locks the Class
 *          object, and each may be re-entered by the same thread.
 * @run main SynchronizedMethodsReentrant
 */

public class SynchronizedMethodsReentrant {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static synchronized int staticReentrant() { return staticHelper(); }
    static synchronized int staticHelper() { return 42; }

    synchronized int instanceReentrant() { return instanceHelper(); }
    synchronized int instanceHelper() { return 7; }

    public static void main(String[] args) {
        SynchronizedMethodsReentrant s = new SynchronizedMethodsReentrant();
        check(staticReentrant() == 42, "static synchronized method reentrant on Class monitor");
        check(s.instanceReentrant() == 7, "instance synchronized method reentrant on this monitor");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
