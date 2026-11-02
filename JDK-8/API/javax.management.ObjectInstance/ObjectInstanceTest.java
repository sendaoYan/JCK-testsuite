/*
 * @test
 * @summary javax.management.ObjectInstance conformance: object name + class name.
 * @run main ObjectInstanceTest
 */

import javax.management.*;
public class ObjectInstanceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        ObjectInstance oi = new ObjectInstance(new ObjectName("d:type=x"), "java.lang.String");
        check("d".equals(oi.getObjectName().getDomain()), "object name");
        check("java.lang.String".equals(oi.getClassName()), "class name");
        check(oi.equals(new ObjectInstance(new ObjectName("d:type=x"), "java.lang.String")), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
