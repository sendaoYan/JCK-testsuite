/*
 * @test
 * @summary JLS 17.1: monitors are reentrant; a thread may lock a monitor multiple
 *          times, and synchronized methods lock this (instance) or the Class
 *          object (static).
 * @run main SynchronizedMethods
 */

public class SynchronizedMethods {
    static synchronized int staticReentrant() {
        return staticHelper();
    }

    static synchronized int staticHelper() {
        return 42;   // re-enters the Class monitor
    }

    synchronized int instanceReentrant() {
        return instanceHelper();
    }

    synchronized int instanceHelper() {
        return 7;    // re-enters the 'this' monitor
    }

    public static void main(String[] args) {
        SynchronizedMethods s = new SynchronizedMethods();
        if (staticReentrant() != 42) throw new AssertionError("static reentrant");
        if (s.instanceReentrant() != 7) throw new AssertionError("instance reentrant");

        System.out.println("PASS");
    }
}
