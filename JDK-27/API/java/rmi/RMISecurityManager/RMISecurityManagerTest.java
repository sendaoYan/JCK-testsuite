/*
 * @test
 * @summary java.rmi.RMISecurityManager conformance: white-box structure + black-box behavior.
 * @run main RMISecurityManagerTest
 */

import java.rmi.RMISecurityManager;

public class RMISecurityManagerTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.RMISecurityManager");
        check(c.getSuperclass() == java.lang.SecurityManager.class, "extends SecurityManager");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !java.lang.reflect.Modifier.isFinal(c.getModifiers()), "concrete non-final");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getAnnotation(Deprecated.class) != null, "deprecated");
        int n = c.getDeclaredConstructors().length;
        check(n == 1, "single ctor");
        check(c.getDeclaredConstructors()[0].getParameterTypes().length == 0, "no-arg ctor");

    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RMISecurityManager sm = new RMISecurityManager();
        check(sm != null, "constructs");
        check(sm instanceof java.lang.SecurityManager, "instanceof SecurityManager");
        RMISecurityManager sm2 = new RMISecurityManager();
        check(sm != sm2, "distinct instances");
        check(!sm.equals(sm2), "not value-equal (identity)");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
