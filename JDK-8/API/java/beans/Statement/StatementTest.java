/*
 * @test
 * @summary java.beans.Statement conformance: target/method/args/execute.
 * @run main StatementTest
 */

import java.beans.*;
public class StatementTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Statement s = new Statement("target", "toString", new Object[0]);
        check("target".equals(s.getTarget()), "target");
        check("toString".equals(s.getMethodName()), "method name");
        check(s.getArguments().length == 0, "arguments");
        s.execute();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
