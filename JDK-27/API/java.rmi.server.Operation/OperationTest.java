/*
 * @test
 * @summary java.rmi.server.Operation conformance: operation string.
 * @run main OperationTest
 */

import java.rmi.server.Operation;

public class OperationTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        Operation op = new Operation("echo");
        check("echo".equals(op.getOperation()), "getOperation");
        check(op.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
