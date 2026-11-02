/*
 * @test
 * @summary java.rmi.server.Operation conformance: operation string.
 * @run main OperationTest
 */

import java.rmi.server.*;
public class OperationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Operation op = new Operation("doIt");
        check("doIt".equals(op.getOperation()), "operation");
        check(op.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
