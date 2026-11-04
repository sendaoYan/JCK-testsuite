/*
 * @test
 * @summary java.rmi.server.Operation conformance: white-box structure + black-box behavior.
 * @run main OperationTest
 */

import java.lang.reflect.Modifier;
import java.rmi.server.Operation;

public class OperationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() {
        Class<?> c = Operation.class;
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(c.isAnnotationPresent(Deprecated.class), "deprecated");
    }

    static void testBlackBox() {
        Operation op = new Operation("echo");
        check("echo".equals(op.getOperation()), "getOperation");
        check("echo".equals(op.toString()), "toString returns operation");

        Operation empty = new Operation("");
        check("".equals(empty.getOperation()), "empty getOperation");
        check("".equals(empty.toString()), "empty toString");

        Operation nil = new Operation(null);
        check(nil.getOperation() == null, "null getOperation");
        check(nil.toString() == null, "null toString");

        // identity equality: Operation does not override equals/hashCode
        Operation o1 = new Operation("same");
        Operation o2 = new Operation("same");
        check(!o1.equals(o2), "identity equals: distinct instances not equal");
        check(o1.equals(o1), "equals self");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
