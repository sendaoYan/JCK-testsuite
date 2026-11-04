/*
 * @test
 * @summary java.rmi.Remote conformance: white-box structure + black-box behavior.
 * @run main RemoteTest
 */

import java.rmi.Remote;

public class RemoteTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.Remote");
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation() && !c.isEnum(), "not annotation/enum");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no super interfaces");
        check(c.getDeclaredMethods().length == 0, "marker interface (no methods)");

    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Remote r = new Remote() { };
        check(r != null, "anonymous impl non-null");
        check(r instanceof Remote, "instanceof Remote");
        Remote r2 = new Remote() { };
        check(r != r2, "distinct instances");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
