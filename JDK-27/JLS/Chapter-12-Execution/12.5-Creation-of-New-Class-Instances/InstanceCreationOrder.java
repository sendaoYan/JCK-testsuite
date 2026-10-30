/*
 * @test
 * @summary JLS 12.5: instance creation initializes fields to defaults, then runs
 *          the superclass constructor, then instance variable initializers and
 *          instance initializers, then the constructor body.
 * @run main InstanceCreationOrder
 */

public class InstanceCreationOrder {
    static final StringBuilder log = new StringBuilder();

    static class Base {
        Base() { log.append("Base() "); }
    }

    static class Sub extends Base {
        int x = init("x");
        { log.append("instance-init "); }
        Sub() { log.append("Sub() "); }
        static int init(String s) { log.append(s).append(' '); return 0; }
    }

    public static void main(String[] args) {
        new Sub();
        if (!"Base() x instance-init Sub() ".equals(log.toString()))
            throw new AssertionError("creation order: " + log);
        System.out.println("PASS");
    }
}
