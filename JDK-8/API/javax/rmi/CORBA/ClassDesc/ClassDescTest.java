/*
 * @test
 * @summary javax.rmi.CORBA.ClassDesc conformance: serializable marker.
 * @run main ClassDescTest
 */

import javax.rmi.CORBA.*;
public class ClassDescTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        ClassDesc cd = new ClassDesc();
        check(cd instanceof java.io.Serializable, "serializable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
