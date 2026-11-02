/*
 * @test
 * @summary java.security.GuardedObject conformance: getObject through a Guard.
 * @run main GuardedObjectTest
 */

import java.security.Guard;
import java.security.GuardedObject;

public class GuardedObjectTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Object target = "secret";
        Guard allow = o -> {};
        GuardedObject go = new GuardedObject(target, allow);
        check(target == go.getObject(), "getObject allowed");

        Guard deny = o -> { throw new SecurityException("denied"); };
        GuardedObject go2 = new GuardedObject(target, deny);
        boolean threw = false;
        try {
            go2.getObject();
        } catch (SecurityException e) {
            threw = true;
        }
        check(threw, "getObject denied throws SecurityException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
